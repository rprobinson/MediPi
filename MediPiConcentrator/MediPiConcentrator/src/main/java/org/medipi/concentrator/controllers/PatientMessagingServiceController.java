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

import java.util.Date;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.services.PatientMessagingService;
import org.medipi.model.EncryptedAndSignedUploadDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class to forward on text based messages from an external clinical system to individual MediPi patient
 * units.
 *
 *
 * @author rick@robinsonhq.com
 */
@RestController
@RequestMapping("MediPiConcentrator/webresources/patientmessages")
public class PatientMessagingServiceController {

    @Autowired
    private PatientMessagingService patientMessagingService;

    @Autowired
    private MediPiLogger logger;

    public static final int ALERT = 0;
    public static final int SIMPLEMESSAGE = 1;
    /**
     * Controller for uploading an encrypted and signed alert message to a patient
     * device. This method saves the incoming message to file, if configured and
     * passes the incoming message to the service layer for processing
     *
     * @param easu Encrypted and signed alert data object
     * @param patientUuid patientUuid of the intended recipient 
     * @return Response to the request
     */
    @RequestMapping(value = "/alert/{patientUuid}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<EncryptedAndSignedUploadDO> sendAlert(@PathVariable("patientUuid") String patientUuid, @RequestBody EncryptedAndSignedUploadDO easu) {
        logger.log(PatientMessagingServiceController.class.getName(), new Date().toString() + " Alert received for patientUuid: " + patientUuid);
        EncryptedAndSignedUploadDO encryptedContent = easu;

        return this.patientMessagingService.persistDirectMessage(patientUuid, encryptedContent, ALERT);
    }

    /**
     * Controller for uploading an encrypted and signed simple message to a patient
     * device. This method saves the incoming message to file, if configured and
     * passes the incoming message to the service layer for processing
     *
     * @param easu Encrypted and signed alert data object
     * @param patientUuid patientUuid of the intended recipient 
     * @return Response to the request
     */
    @RequestMapping(value = "/simplemessage/{patientUuid}", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<EncryptedAndSignedUploadDO> sendSimpleMessage(@PathVariable("patientUuid") String patientUuid, @RequestBody EncryptedAndSignedUploadDO easu) {
        logger.log(PatientMessagingServiceController.class.getName(), new Date().toString() + " Simple Message received for patientUuid: " + patientUuid);
        EncryptedAndSignedUploadDO encryptedContent = easu;

        return this.patientMessagingService.persistDirectMessage(patientUuid, encryptedContent, SIMPLEMESSAGE);
    }
}
