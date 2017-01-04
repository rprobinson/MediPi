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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ma.glasnost.orika.MapperFacade;
import org.medipi.concentrator.dao.PatientDAOImpl;
import org.medipi.concentrator.dao.RecordingDeviceDataDAOImpl;
import org.medipi.concentrator.entities.Patient;
import org.medipi.concentrator.entities.RecordingDeviceData;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.model.PatientDataRequestDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to enable 3rd party systems to request data for patients from a
 * patient group from a date in the past and have it delivered to the requesting system.
 *
 * @author rick@robinsonhq.com
 */
@Service
public class RequestDataService {

    @Autowired
    private RecordingDeviceDataDAOImpl recordingDeviceDataDAOImpl;

    @Autowired
    private PatientDAOImpl patientDAOImpl;

    @Autowired
    private MapperFacade mapperFacade;

    /**
     * A date query parameter is passed to the interface with a requesting patient group parameter.
     * This defines at what point the requesting system last had any data for these patients
     *
     * @param patientGroupUuid patient group UUID to be requested
     * @param lastDownloadDate last download date 
     * @return Response list of data for patients requested
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<List<PatientDataRequestDO>> getData(String patientGroupUuid, Date lastDownloadDate) {
        try {
            List<PatientDataRequestDO> responsePayload = new ArrayList<>();

            List<Patient> pList = patientDAOImpl.findByGroup(patientGroupUuid);
            for (Patient p : pList) {
                PatientDataRequestDO responsePdr = null;
                List<RecordingDeviceData> rddList = recordingDeviceDataDAOImpl.findByPatientAndDownloadedTime(p.getPatientUuid(), lastDownloadDate);
                if (rddList != null && !rddList.isEmpty()) {
                    //create a new patient data request to return
                    responsePdr = new PatientDataRequestDO(p.getPatientUuid());
                    for (RecordingDeviceData rdd : rddList) {
                        RecordingDeviceData rddMapped = this.mapperFacade.map(rdd, RecordingDeviceData.class);

                        responsePdr.addRecordingDeviceData(rddMapped);
                    }
                    responsePayload.add(responsePdr);
                }
            }
            if (responsePayload.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(responsePayload, HttpStatus.OK);

            }
        } catch (Exception ex) {
            throw new InternalServerError500Exception(ex.getLocalizedMessage());
        }
    }

}
