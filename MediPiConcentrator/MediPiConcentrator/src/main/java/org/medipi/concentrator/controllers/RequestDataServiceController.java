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
package org.medipi.concentrator.controllers;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import java.util.Date;
import java.util.List;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.model.PatientDataRequestDO;
import org.medipi.concentrator.services.RequestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class to receive incoming RESTful message containing data from MediPi patient
 * units.
 *
 *
 * @author rick@robinsonhq.com
 */
@RestController
@RequestMapping("MediPiConcentrator/webresources")
public class RequestDataServiceController {

    @Autowired
    private RequestDataService requestDataService;

    @Autowired
    private MediPiLogger logger;

    /**
     * Constructor
     */
    public RequestDataServiceController() {
        System.out.println("called once when the first transaction is received");

    }

    /**
     * Controller for synchronising all data from all patients within a patient
     * group with the MediPi Concentrator Database to requesting clinical
     * systems.
     *
     * @param patientGroupUuid the patient group for which to synchronise
     * @param lastDownloadEpochMillis This is the date in the format of Unix
     * epoch time (millis after January 1, 1970, 00:00:00 GMT) when this data
     * was last synchronised. A value of 0 will return all data for all patients
     * in the patient group
     * @return Response to the request
     */
    @RequestMapping(value = "/requestdata/allData/{patientGroupUuid}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<List<PatientDataRequestDO>> requestNewData(@PathVariable("patientGroupUuid") String patientGroupUuid, @RequestParam("date") Long lastDownloadEpochMillis) {

        if (lastDownloadEpochMillis < 0) {
            logger.log(PatientUploadServiceController.class.getName(), new Date().toString() + " new data requested from Patient Group: " + patientGroupUuid + " Invalid Unix epoch representation of date");
            throw new InternalServerError500Exception("Invalid Unix epoch representation of date since last synchronisation");
        }
        Date lastDownloadDate = new Date(lastDownloadEpochMillis);
//Removed to Reduce Logs size        logger.log(PatientUploadServiceController.class.getName(), new Date().toString() + " new data requested from Patient Group: " + patientGroupUuid + " since the last download at: " + new ISO8601DateFormat().format(lastDownloadDate));
        return this.requestDataService.getData(patientGroupUuid, lastDownloadDate);
    }
}
