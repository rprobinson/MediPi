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
package org.medipi.concentrator.dataformat;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityExistsException;
import org.apache.commons.io.IOUtils;
import org.medipi.concentrator.MediPiProperties;
import org.medipi.concentrator.dao.RecordingDeviceDataDAOImpl;
import org.medipi.concentrator.dao.RecordingDeviceAttributeDAOImpl;
import org.medipi.concentrator.dao.RecordingDeviceTypeDAOImpl;
import org.medipi.concentrator.entities.RecordingDeviceData;
import org.medipi.concentrator.entities.Patient;
import org.medipi.concentrator.entities.RecordingDeviceAttribute;
import org.medipi.concentrator.entities.RecordingDeviceType;
import org.medipi.concentrator.exception.BadRequest400Exception;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.utilities.Utilities;
import org.medipi.model.DeviceDataDO;
import org.medipi.model.DevicesPayloadDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete instance for the MediPi "Native" data formats. A class to parse
 * incoming MediPi Native format. This data format has been developed in the
 * first instance to allow rapid development so that it can be shown that data
 * can be successfully passed from a patient unit to a concentrator. This is not
 * necessarily intended to be a final messaging format
 *
 * @author rick@robinsonhq.com
 */
@Service
public class MediPiNativeFormat extends PatientUploadDataFormat {

    private static final String SUCCESSFULLYPROCESSEDSUBMISSIONSCRIPT = "medipi.concentrator.successfullyprocessedsubmissionscript";
    private String classToken;
    private final MediPiLogger logger = MediPiLogger.getInstance();
    private String trackingId;
    private String successfullyProcessedSubmission;

    @Autowired
    private RecordingDeviceTypeDAOImpl recordingDeviceTypeDAO;

    @Autowired
    private RecordingDeviceAttributeDAOImpl recordingDeviceAttributeDAO;

    @Autowired
    private RecordingDeviceDataDAOImpl recordingDeviceDataDAO;

    @Autowired
    private Utilities utils;

    @Override
    public void setClassToken(String classToken) {
        this.classToken = classToken;
    }

    @Override
    public String init() {
        successfullyProcessedSubmission = utils.getProperties().getProperty(SUCCESSFULLYPROCESSEDSUBMISSIONSCRIPT);
        return null;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean process(DevicesPayloadDO content, Patient patient) {
        HashMap<String, String> persistentMetadata = new HashMap<>();
        List<DeviceDataDO> p = content.getPayload();

        if (p != null) {
            if (p.isEmpty()) {
                //if there is an issue with the Payload
                throwBadRequest400("The request has no payload: " + content);
            }
            // Added data to the database - the design of the DB is that these are individual data points NOT rows of data
            int totalRowsWrittenToDB = 0;
            // Loop through each of the data Payloads
            for (DeviceDataDO pay : p) {
                if (pay == null) {
                    //Unable to parse device's content
                    throwBadRequest400("Unable to decrypt the content of the payload");

                }
                int rowsWrittenToDBPerPayload = 0;
                String make = null;
                String model = null;
                String displayName = null;
                String datadelimeter = null;
                String[] columnsArray = null;
                String[] formatArray = null;
                String[] unitsArray = null;
                Date scheduleeffectivedate = null;
                Date scheduleexpirydate = null;
                // get the device type e.g. Oximeter
                String type = null;
                try {
                    type = pay.getProfileId().substring(pay.getProfileId().lastIndexOf(":") + 1, pay.getProfileId().length());
                    if (type == null || type.trim().length() == 0) {
                        throwBadRequest400("Unable to parse the content from the payload");
                    }
                } catch (IndexOutOfBoundsException e) {
                    throwBadRequest400("Unable to parse the content from the payload");
                }
                BufferedReader br = null;
                try {
                    //Read the Device's content
                    br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pay.getPayload().getBytes())));
                } catch (Exception e) {
                    //Unable to parse device's content
                    throwBadRequest400("Unable to parse the content from the payload with profile Id: " + pay.getProfileId());
                }
                logger.log(MediPiNativeFormat.class.getName(), new Date().toString() + " Payload device: " + type + ". Device data uuid:" + pay.getDeviceDataUuid());
                try {
                    String line;
                    Boolean readAllMetadata = false;
                    RecordingDeviceType rdt = null;
                    while ((line = br.readLine()) != null) {

                        String[] metaSplit = line.split("->");
                        if (metaSplit[0].equals("metadata")) {
                            // Found metadata
                            if (metaSplit[1].equals("persist")) {
                                //Data to be stored againast the delta of the whole downloaded dataset
                                persistentMetadata.put(metaSplit[2], metaSplit[3]);
                                if (metaSplit.length > 4) {
                                    throwBadRequest400("Failed to parse metadata->persist: " + line + "in payload: " + pay.getProfileId());
                                }
                            } else {
                                //Other metadata
//                                System.out.println(metaSplit[1]);
                                switch (metaSplit[1]) {
                                    case "datadelimiter":
                                        datadelimeter = metaSplit[2];
                                        // if delimeter is a special regex charater then prpend with a double //
                                        if ("\\.[]{}()*+-?^$|".contains(datadelimeter)) {
                                            datadelimeter = "\\" + datadelimeter;
                                        }
                                        break;
                                    case "make":
                                        make = metaSplit[2];
                                        break;
                                    case "model":
                                        model = metaSplit[2];
                                        break;
                                    case "displayname":
                                        displayName = metaSplit[2];
                                        break;
                                    case "columns":
                                        columnsArray = metaSplit[2].split(datadelimeter);
                                        if (columnsArray == null || !columnsArray[0].equals("iso8601time")) {
                                            throwBadRequest400("Failed to parse metadata in payload: " + pay.getProfileId() + " iso8601date field is not the first column");
                                        }
                                        break;
                                    case "format":
                                        formatArray = metaSplit[2].split(datadelimeter);
                                        if (formatArray == null || !formatArray[0].equals("DATE")) {
                                            throwBadRequest400("Failed to parse metadata in payload: " + pay.getProfileId() + " iso8601date field is not in the correct format");
                                        }
                                        break;
                                    case "units":
                                        unitsArray = metaSplit[2].split(datadelimeter);
                                        if (unitsArray == null) {
                                            throwBadRequest400("Failed to parse metadata in payload: " + pay.getProfileId() + " units field is not in the correct format");
                                        }
                                        break;
                                    case "scheduleeffectivedate":
                                        try {
                                            scheduleeffectivedate = new ISO8601DateFormat().parse(metaSplit[2]);
                                        } catch (ParseException ex) {
                                            throwBadRequest400("scheduleeffectivedate for device: " + displayName + " metatdata->scheduleeffectivedate is in an invalid format");
                                        }
                                        break;
                                    case "scheduleexpirydate":
                                        try {
                                            scheduleexpirydate = new ISO8601DateFormat().parse(metaSplit[2]);
                                        } catch (ParseException ex) {
                                            throwBadRequest400("scheduleexpirydate for device: " + displayName + " metatdata->scheduleexpirydate is in an invalid format");
                                        }
                                        break;
                                    default:
                                        // Fail - bad data
                                        throwBadRequest400("Failed to parse metadata in payload: " + pay.getProfileId());
                                }
                            }

                        } else if (readAllMetadata || checkMetadata(make, model, displayName, datadelimeter, columnsArray, formatArray, unitsArray)) {
                            readAllMetadata = true;
                            // It's data
                            if (rdt == null) {
                                // need to find the type - only needs to be done once per device type
                                // Check to find the device in the device_type table
                                try {
                                    rdt = this.recordingDeviceTypeDAO.findByTypeMakeModelDisplayName(type, make, model, displayName);
                                } catch (EmptyResultDataAccessException e) {
                                    // Device does NOT exist in the database
                                    // if not in db add it
                                    rdt = updateRecordingDeviceType(rdt, type, make, model, displayName);
                                }
                            }
                            String[] dataArray = line.split(datadelimeter);
                            int columnNo = 0;
                            Date dataPointTime = null;
                            for (String data : dataArray) {
                                if (columnNo == 0) {
                                    try {
                                        // The concentrator expects the incoming string representation of the time to be UTC in ISO
                                        dataPointTime = new ISO8601DateFormat().parse(data);
                                    } catch (ParseException ex) {
                                        throwBadRequest400("Datapoint time for device: " + displayName + " is in an invalid format: " + data);
                                    }
                                } else {
                                    RecordingDeviceAttribute rda = null;
                                    try {
                                        rda = this.recordingDeviceAttributeDAO.findByTypeUnitsFormatAndAttributeName(rdt, columnsArray[columnNo], unitsArray[columnNo], formatArray[columnNo]);
                                    } catch (EmptyResultDataAccessException e) {
                                        // Attribute Name does NOT exist in the database
                                        // add new entry to DB
                                        rda = updateRecordingDeviceAttribute(rda, rdt, columnsArray[columnNo], unitsArray[columnNo], formatArray[columnNo]);
                                    }
                                    //First check for duplicates - this is only to record the delta on machines with storage
                                    boolean writeData = false;
                                    List<RecordingDeviceData> dd = null;
                                    try {
                                        dd = this.recordingDeviceDataDAO.isAlreadyStored(rda, patient, data, dataPointTime);
                                        if (dd.isEmpty()) {
                                            writeData = true;
                                        } else {
                                            System.out.println("Duplicate data: " + data + " @ " + dataPointTime.getTime());
                                            break;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("exception thrown when finding if data is already stored");
                                        break;
                                    }

                                    //Now that the attribute id is found write device data
                                    if (rda != null && writeData) {
                                        RecordingDeviceData d = new RecordingDeviceData();
                                        d.setAttributeId(rda);
                                        d.setPatientUuid(patient);
                                        d.setDataValue(data);
                                        d.setDataValueTime(dataPointTime);
                                        // Set the timedownloaded value in order to mark 
                                        //(using a trusted, recently synchronised timestamp 
                                        // for clinical systems to guage if data has been downloaded)
//                                        d.setDownloadedTime(new Date());
                                        d.setScheduleEffectiveTime(scheduleeffectivedate);
                                        d.setScheduleExpiryTime(scheduleexpirydate);

                                        try {
                                            this.recordingDeviceDataDAO.save(d);
                                            rowsWrittenToDBPerPayload++;
                                            totalRowsWrittenToDB++;
                                        } catch (Exception e) {
                                            logger.log(MediPiNativeFormat.class.getName() + ".dbIssue", "Attempt to write data for " + type + " to DB failed");
                                            throw new InternalServerError500Exception("Attempt to write data for " + type + " to DB failed");

                                        }
                                    }

                                }
                                columnNo++;

                            }
                        } else {
                            throwBadRequest400("Unable to parse the content from the payload with profile Id: " + pay.getProfileId());
                        }
                    }
                    // failure in reading bufferedReader
                } catch (IOException e) {
                    throwBadRequest400("Unable to parse the content from the payload with profile Id: " + pay.getProfileId());
                }
                logger.log(MediPiNativeFormat.class.getName() + ".dbInfo", rowsWrittenToDBPerPayload + " rows of data written to the DB for payload: " + type);

            }
            if (successfullyProcessedSubmission != null && totalRowsWrittenToDB > 0) {
                logger.log(MediPiNativeFormat.class.getName() + ".dbInfo", totalRowsWrittenToDB + " rows of data written to the DB in total for transaction covered by trackingID: " + trackingId);
                System.out.println("Patient " + patient.getPatientUuid() + " has submitted " + totalRowsWrittenToDB + " pieces of data at " + new Date());
                Runtime runtime = Runtime.getRuntime();
                try {
                    String script = successfullyProcessedSubmission.replace("__PATIENT_UUID__", patient.getPatientUuid());
                    Process process = runtime.exec(script);
                    int resultCode = process.waitFor();
                    if (resultCode == 0) {
                        // all is good
                    }
                } catch (Throwable ex) {
                    logger.log(MediPiNativeFormat.class.getName() + ".curlIssue", "Attempt to curl notification for " + patient.getPatientUuid() + " failed @" + new Date() + " because " + ex.getLocalizedMessage());
                    System.out.println("Attempt to curl notification for " + patient.getPatientUuid() + " failed @" + new Date() + " because " + ex.getLocalizedMessage());
                }
            }
            if (totalRowsWrittenToDB == 0) {
                // should any particular response be made for no data added to db for any payload?
            }

        } else {
            //if there is an issue with the Payload
            throwBadRequest400("Failed to parse DistributionEnvelope Payload" + content);

        }
        return true;
    }

    @Transactional
    private RecordingDeviceType updateRecordingDeviceType(RecordingDeviceType rdt, String type, String make, String model, String displayName) {
        try {
            rdt = new RecordingDeviceType();
            rdt.setType(type);
            rdt.setMake(make);
            rdt.setModel(model);
            rdt.setDisplayName(displayName);
            recordingDeviceTypeDAO.save(rdt);
        } catch (EntityExistsException e) {
            throwBadRequest400("Device: " + type + " " + make + " " + model + " " + displayName + " already exists in RECORDING_DEVICE_TYPE table in the DB");
        }
        return rdt;

    }

    @Transactional
    private RecordingDeviceAttribute updateRecordingDeviceAttribute(RecordingDeviceAttribute rda, RecordingDeviceType rdt, String attributeName, String attributeUnits, String attributeType) {
        try {
            rda = new RecordingDeviceAttribute();
            rda.setAttributeName(attributeName);
            rda.setAttributeType(attributeType);
            rda.setAttributeUnits(attributeUnits);
            rda.setTypeId(rdt);
            recordingDeviceAttributeDAO.save(rda);
        } catch (EntityExistsException e) {
            throwBadRequest400("Attribute: " + attributeName + attributeUnits + attributeType + " already exists for device " + attributeName + " " + attributeUnits + " " + attributeType + " in RECORDING_DEVICE_ATTRIBUTE table in the DB");
        }
        return rda;
    }

    private void throwBadRequest400(String message) throws BadRequest400Exception {
        throwBadRequest400(message, "");
    }

    private void throwBadRequest400(String message, String loggingAdditional) throws BadRequest400Exception {
        logger.log(MediPiNativeFormat.class.getName() + ".dataValidationIssue", message + loggingAdditional);
        throw new BadRequest400Exception(message);
    }

    private boolean checkMetadata(String make, String model, String displayName, String datadelimiter, String[] columns, String[] format, String[] units) {
        //what is the minimum metadata required?
        StringBuilder nullContent = new StringBuilder();
        if (make == null) {
            nullContent.append("metadata->make ");
        }
        if (model == null) {
            nullContent.append("metadata->model ");
        }
        if (displayName == null) {
            nullContent.append("metadata->displayname ");
        }
        if (datadelimiter == null) {
            nullContent.append("metadata->datadelimiter ");
        }
        if (columns == null) {
            nullContent.append("metadata->columns ");
        }
        if (format == null) {
            nullContent.append("metadata->format ");
        }
        if (units == null) {
            nullContent.append("metadata->units ");
        }
        if (columns != null && format != null && columns.length != format.length) {
            nullContent.append("metadata->columns and metadata->format have different number of elements ");
        }

        if (nullContent.length() != 0) {
            throwBadRequest400("Insufficient Metadata - Metadata in error: " + nullContent);
        } else {
            return true;
        }
        return false;
    }
}
