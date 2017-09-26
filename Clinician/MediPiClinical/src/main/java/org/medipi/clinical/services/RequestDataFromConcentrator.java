/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.clinical.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.medipi.clinical.dao.PatientDAOImpl;
import org.medipi.clinical.dao.PatientGroupDAOImpl;
import org.medipi.clinical.dao.RecordingDeviceAttributeDAOImpl;
import org.medipi.clinical.dao.RecordingDeviceDataDAOImpl;
import org.medipi.clinical.dao.RecordingDeviceTypeDAOImpl;
import org.medipi.clinical.entities.Patient;
import org.medipi.clinical.entities.PatientGroup;
import org.medipi.clinical.entities.RecordingDeviceAttribute;
import org.medipi.clinical.entities.RecordingDeviceData;
import org.medipi.clinical.entities.RecordingDeviceType;
import org.medipi.clinical.exception.InternalServerError500Exception;
import org.medipi.clinical.logging.MediPiLogger;
import org.medipi.clinical.model.PatientDataRequestDO;
import org.medipi.model.AlertListDO;
import org.medipi.model.SimpleMessageDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author rick@robinsonhq.com
 */
@Component
public class RequestDataFromConcentrator {

    @Autowired
    private PatientDAOImpl patientDAOImpl;
    @Autowired
    private PatientGroupDAOImpl patientGroupDAOImpl;
    @Autowired
    private RecordingDeviceDataDAOImpl recordingDeviceDataDAOImpl;
    @Autowired
    private RecordingDeviceTypeDAOImpl recordingDeviceTypeDAOImpl;
    @Autowired
    private RecordingDeviceAttributeDAOImpl recordingDeviceAttributeDAOImpl;
    @Autowired
    private SSLClientHttpRequestFactory requestFactory;
    @Autowired
    private SendAlertService sendAlertService;
    @Autowired
    private DataThresholdTester dataThresholdTester;
    @Autowired
    private LinkedSubmissionsTester linkedSubmissionsTester;
    @Autowired
    private MediPiLogger logger;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    //Path for posting requests for synchronising patient data
    @Value("${medipi.clinical.syncdata.resourcepath}")
    private String syncDataResourcePath;

    //FIX - Connection refused or any unexpected results
    @Scheduled(fixedRateString = "${medipi.clinical.dataRefreshRate}")
    @Transactional(rollbackFor = RuntimeException.class)
    public void requestDataFromConcentrator() {
        System.out.println("The time is now " + dateFormat.format(new Date()));

        // create a restful template using the the SSL request factory
        RestTemplate restTemplate;
        try {
            restTemplate = new RestTemplate(requestFactory.getClientHttpRequestFactory());
        } catch (Exception e) {
            MediPiLogger.getInstance().log(RequestDataFromConcentrator.class.getName() + "error", "Error in connectiong using SSL: " + e.getLocalizedMessage());
            System.out.println("Error in connectiong using SSL: " + e.getLocalizedMessage());
            return;
        }
        dataThresholdTester.init();
        linkedSubmissionsTester.init();
        // Check for any unsent alerts and send on any found until they reach the upper resend limit
        if (dataThresholdTester.isEnabled()) {
            sendAlertService.resendDirectMessages(dataThresholdTester, restTemplate);
        }
        if (linkedSubmissionsTester.isEnabled()) {
            sendAlertService.resendDirectMessages(linkedSubmissionsTester, restTemplate);
        }

        // get List of all the patient groups to retreive data for 
        List<PatientGroup> patGrpList = patientGroupDAOImpl.getAllGroups();

        // Loop through the patient group list and retreive the latest date of retreival
        if (!patGrpList.isEmpty() && !patGrpList.isEmpty()) {
            for (PatientGroup pg : patGrpList) {
                Date d = recordingDeviceDataDAOImpl.dateOfLatestPatientGroupSync(pg);
                Date lastMeasurementDate;
                if (d == null) {
                    // if there is no date found use epoch date
                    lastMeasurementDate = new Date(0L);
                } else {
                    lastMeasurementDate = d;
                }

                // create a URL of the concentrator inclusing the patient group uuid and the last sync time
                URI targetUrl = UriComponentsBuilder.fromUriString(syncDataResourcePath)
                        .path("/")
                        .path(pg.getPatientGroupUuid())
                        .queryParam("date", lastMeasurementDate.getTime())
                        .build()
                        .toUri();

                try {
                    // transmit request to concentrator
                    String responseString = restTemplate.getForObject(targetUrl, String.class);

                    if (responseString != null) {
                        ObjectMapper mapper = new ObjectMapper();
// TO DO - THIS RESPONSE SHOULD BE ENCRYPTED AND SIGNED AND THERFORE WILL NEED DECRYTING AND VERIFIYING
                        List<PatientDataRequestDO> response;
                        try {
                            response = mapper.readValue(responseString, new TypeReference<List<PatientDataRequestDO>>() {
                            });
                            //Loop through each patient in the response and persist any returned data
                            for (PatientDataRequestDO pdr : response) {
                                if (pdr != null) {
                                    //find the patient in the clinical db
                                    Patient patient = patientDAOImpl.findByPrimaryKey(pdr.getPatientUuid());
                                    if (patient != null) {
                                        // Create the container for any possible alerts
                                        AlertListDO alertListDO = new AlertListDO(patient.getPatientUuid());
                                        // loop through all the individual datapoints, persiste them to the DB and test for any thresholds
                                        System.out.println("patient:" + patient.getPatientUuid() + " data items:" + pdr.getRecordingDeviceDataList().size());
                                        for (RecordingDeviceData rdd : pdr.getRecordingDeviceDataList()) {
                                            updateRecordingDeviceData(rdd, patient, alertListDO);
                                        }

                                        if (!alertListDO.getAlert().isEmpty() && dataThresholdTester.isEnabled()) {
                                            sendAlertService.sendDirectMessage(dataThresholdTester, alertListDO, patient.getPatientUuid(), restTemplate);
                                        }

                                        if (linkedSubmissionsTester.isEnabled()) {
                                            SimpleMessageDO simpleMessageDO = new SimpleMessageDO(patient.getPatientUuid());
                                            linkedSubmissionsTester.testNewData(patient, simpleMessageDO);
                                            sendAlertService.sendDirectMessage(linkedSubmissionsTester, simpleMessageDO, patient.getPatientUuid(), restTemplate);
                                        }

                                    } else {
                                        MediPiLogger.getInstance().log(RequestDataFromConcentrator.class.getName() + "error", "Error - Can't find a returned patient in the local DB: " + pdr.getPatientUuid());
                                        System.out.println("Error - Can't find a returned patient in the local DB: " + pdr.getPatientUuid());
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            MediPiLogger.getInstance().log(RequestDataFromConcentrator.class.getName() + "error", "Error mapping values from host response onto the PatientDataRequestDO: " + ex.getLocalizedMessage());
                            System.out.println("Error mapping values from host response onto the PatientDataRequestDO: " + ex.getLocalizedMessage());
                        }
                    }
                } catch (ResourceAccessException e) {
                    MediPiLogger.getInstance().log(RequestDataFromConcentrator.class.getName() + "error", "Error in connectiong to target system using SSL: " + e.getLocalizedMessage());
                    System.out.println("Error in connectiong to target system using SSL: " + e.getLocalizedMessage());
                } catch (Exception e) {
                    MediPiLogger.getInstance().log(RequestDataFromConcentrator.class.getName() + "error", "Error occurred: " + e.getLocalizedMessage());
                    System.out.println("Error occurred: " + e.getLocalizedMessage());
                }
            }
        } else {
            System.out.println("Warning: No patient Groups defined in DB found to request data for");
        }
    }

    @Transactional
    private void updateRecordingDeviceData(RecordingDeviceData rdd, Patient patient, AlertListDO alertListDO) {
        RecordingDeviceType rdt = null;
        try {
            rdt = recordingDeviceTypeDAOImpl.findByTypeMakeModelDisplayName(
                    rdd.getAttributeId().getTypeId().getType(),
                    rdd.getAttributeId().getTypeId().getMake(),
                    rdd.getAttributeId().getTypeId().getModel(),
                    rdd.getAttributeId().getTypeId().getDisplayName());
        } catch (EmptyResultDataAccessException emptyType) {
            rdt = updateRecordingDeviceType(rdt, rdd);
        }
        RecordingDeviceAttribute rda = null;
        try {
            rda = recordingDeviceAttributeDAOImpl.findByAttributeNameTypeUnitsTypeId(
                    rdd.getAttributeId().getAttributeName(),
                    rdd.getAttributeId().getAttributeType(),
                    rdd.getAttributeId().getAttributeUnits(),
                    rdt.getTypeId());
        } catch (EmptyResultDataAccessException emptyAttribute) {
            rda = updateRecordingDeviceAttribute(rda, rdd, rdt);
        }

        RecordingDeviceData rddSet = new RecordingDeviceData();
        rddSet.setAttributeId(rda);
        rddSet.setDataValue(rdd.getDataValue());
        rddSet.setDataValueTime(rdd.getDataValueTime());
        rddSet.setDownloadedTime(rdd.getDownloadedTime());
        rddSet.setPatientUuid(patient);
        rddSet.setScheduleEffectiveTime(rdd.getScheduleEffectiveTime());
        rddSet.setScheduleExpiryTime(rdd.getScheduleExpiryTime());

        try {
            this.recordingDeviceDataDAOImpl.save(rddSet);
            if (dataThresholdTester.isEnabled()) {
                dataThresholdTester.testNewData(rda, patient, rddSet, alertListDO);
            }
        } catch (EmptyResultDataAccessException e) {
            // Nothing to do if there is no Attribute threshold for the reading
        } catch (Exception e) {
            logger.log(RequestDataFromConcentrator.class.getName() + ".dbIssue", "Attempt to write data for " + rdd.getAttributeId().getAttributeName() + " to DB failed");
            throw new InternalServerError500Exception("Attempt to write data for " + rdd.getAttributeId().getAttributeName() + " to DB failed");

        }
    }

    @Transactional
    private RecordingDeviceAttribute updateRecordingDeviceAttribute(RecordingDeviceAttribute rda, RecordingDeviceData rdd, RecordingDeviceType rdt) {
        rda = new RecordingDeviceAttribute();
        rda.setAttributeName(rdd.getAttributeId().getAttributeName());
        rda.setAttributeType(rdd.getAttributeId().getAttributeType());
        rda.setAttributeUnits(rdd.getAttributeId().getAttributeUnits());
        rda.setTypeId(rdt);
        recordingDeviceAttributeDAOImpl.save(rda);
        return rda;
    }

    @Transactional
    private RecordingDeviceType updateRecordingDeviceType(RecordingDeviceType rdt, RecordingDeviceData rdd) {
        rdt = new RecordingDeviceType();
        rdt.setType(rdd.getAttributeId().getTypeId().getType());
        rdt.setMake(rdd.getAttributeId().getTypeId().getMake());
        rdt.setModel(rdd.getAttributeId().getTypeId().getModel());
        rdt.setDisplayName(rdd.getAttributeId().getTypeId().getDisplayName());
        recordingDeviceTypeDAOImpl.save(rdt);
        return rdt;
    }

}
