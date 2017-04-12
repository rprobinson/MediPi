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
package org.medipi.concentrator.services;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ma.glasnost.orika.MapperFacade;
import org.medipi.concentrator.dao.PatientDAOImpl;
import org.medipi.concentrator.dao.RecordingDeviceDataDAOImpl;
import org.medipi.concentrator.entities.Patient;
import org.medipi.concentrator.entities.RecordingDeviceData;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.model.PatientDataRequestDO;
import org.medipi.concentrator.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to enable 3rd party systems to request data for patients from a
 * patient group from a date in the past and have it delivered to the requesting
 * system.
 *
 * @author rick@robinsonhq.com
 */
@Service
public class RequestDataService {

    private static final String MEDIPICONCENTRATORDATABASEBACKOFFPERIOD = "medipi.concentrator.database.backoffperiod";

    @Autowired
    private RecordingDeviceDataDAOImpl recordingDeviceDataDAOImpl;

    @Autowired
    private PatientDAOImpl patientDAOImpl;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private Utilities utils;

    /**
     * A date query parameter is passed to the interface with a requesting
     * patient group parameter. This defines at what point the requesting system
     * last had any data for these patients
     *
     * @param patientGroupUuid patient group UUID to be requested
     * @param lastDownloadDate last download date
     * @return Response list of data for patients requested
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<List<PatientDataRequestDO>> getData(String patientGroupUuid, Date lastDownloadDate) {
        String backoffPeriodString = utils.getProperties().getProperty(MEDIPICONCENTRATORDATABASEBACKOFFPERIOD);
        int backoffPeriod;
        if (backoffPeriodString == null || backoffPeriodString.trim().length() == 0) {
            backoffPeriod = 10000;
        } else {
            try {
                backoffPeriod = Integer.parseInt(backoffPeriodString);
            } catch (NumberFormatException numberFormatException) {
                MediPiLogger.getInstance().log(RequestDataService.class.getName() + "error", "Error - Cant read the back off period from the properties file: " + numberFormatException.getLocalizedMessage());
                System.out.println("Error - Cant read the back off period from the properties file: " + numberFormatException.getLocalizedMessage());
                backoffPeriod = 10000;
            }
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS zzz");
            List<PatientDataRequestDO> responsePayload = new ArrayList<>();
            List<Patient> pList = patientDAOImpl.findByGroup(patientGroupUuid);
            // to allow the DB to settle to any data not yet arrived do not attempt to pull any data within the last x seconds
            Instant nowInstant = Instant.now();
            Instant endInstant = nowInstant.minusMillis(backoffPeriod);
            Date endTime = Date.from(endInstant);

            if (lastDownloadDate.toInstant().plusMillis(backoffPeriod).isAfter(nowInstant)) {
                System.out.println("-------");
                System.out.println("request time of last data item downloaded is too soon: " + sdf.format(lastDownloadDate));
                System.out.println("now time: " + sdf.format(new Date()));
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            for (Patient p : pList) {
                PatientDataRequestDO responsePdr = null;
                List<RecordingDeviceData> rddList = recordingDeviceDataDAOImpl.findByPatientAndDownloadedTime(p.getPatientUuid(), lastDownloadDate, endTime);
                if (rddList != null && !rddList.isEmpty()) {
                    //create a new patient data request to return
                    responsePdr = new PatientDataRequestDO(p.getPatientUuid());
                    for (RecordingDeviceData rdd : rddList) {
                        RecordingDeviceData rddMapped = this.mapperFacade.map(rdd, RecordingDeviceData.class);

                        responsePdr.addRecordingDeviceData(rddMapped);
                    }
                    responsePayload.add(responsePdr);
                    System.out.println("-------");
                    System.out.println("request time of last data item downloaded: " + sdf.format(lastDownloadDate));
                    System.out.println("now time: " + sdf.format(new Date()));
                    System.out.println("patient:" + p.getPatientUuid() + " data items:" + rddList.size());
                }
            }
            if (responsePayload.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(responsePayload, HttpStatus.OK);

            }
        } catch (Exception ex) {
            System.out.println("500 exception ");
            throw new InternalServerError500Exception(ex.getLocalizedMessage());
        }
    }

}
