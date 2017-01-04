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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.services.PatientUploadService;
import org.medipi.concentrator.utilities.Utilities;
import org.medipi.model.EncryptedAndSignedUploadDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping("MediPiConcentrator/webresources/patientupload")
public class PatientUploadServiceController {

    @Autowired
    private PatientUploadService patientUploadService;

    @Autowired
    private MediPiLogger logger;

    @Value("${medipi.concentrator.savemessagestofile}")
    private boolean savemessagestofile;

    @Value("${medipi.concentrator.inboundsavedmessagedir}")
    private String inboundsavedmessagedir;

    /**
     * Controller for Patient Upload of data from MediPi Patient units.
     *
     * This method:
     *
     * 1.saves the incoming message to file, if configured and passes the
     * incoming message to the service layer for processing
     *
     * @param deviceId incoming deviceId parameter from RESTful message
     * @param patientUuid incoming patientUuid parameter from RESTful message
     * @param dataFormat incoming Data-Format HTTP header parameter from RESTful
     * message
     * @param easu incoming message in an encrypted and signed data object
     * @return Response to the request
     */
    @RequestMapping(value = "/{deviceId}/{patientUuid}", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateMessage(@PathVariable("deviceId") String deviceId, @PathVariable("patientUuid") String patientUuid, @RequestHeader(value = "Data-Format") String dataFormat, @RequestBody EncryptedAndSignedUploadDO easu) {
        logger.log(PatientUploadServiceController.class.getName(), new Date().toString() + " Called by patientUuid: " + patientUuid + " using deviceId: " + deviceId);
        EncryptedAndSignedUploadDO content = easu;

        if (savemessagestofile) {
            byte[] contentInBytes = null;

            FileOutputStream fop = null;
            try {
                StringBuilder fnb = new StringBuilder(inboundsavedmessagedir);
                fnb.append(System.getProperty("file.separator"));
                fnb.append("uuid_");
                fnb.append(content.getUploadUuid());
                fnb.append("_device_");
                fnb.append(deviceId);
                fnb.append("_at_");
                fnb.append(Utilities.INTERNAL_SPINE_FORMAT.format(new Date()));
                fnb.append(".log");
                File file = new File(fnb.toString());
                fop = new FileOutputStream(file);
                // if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }

                // get the content in bytes
                writeJSON(content, fop);
                fop.flush();
                fop.close();
                logger.log(PatientUploadServiceController.class.getName(), new Date().toString() + " Encrypted payload uuid: " + easu.getUploadUuid());
            } catch (IOException e) {
                logger.log(PatientUploadServiceController.class.getName() + ".error", "Cannot save outbound message payload to local drive - check the configured directory: " + inboundsavedmessagedir);
            } finally {
                try {
                    if (fop != null) {
                        fop.close();
                    }
                } catch (IOException e) {
                    logger.log(PatientUploadServiceController.class.getName() + ".error", "Cannot save outbound message payload to local drive - check the configured directory: " + inboundsavedmessagedir);
                }
            }
        }
        return this.patientUploadService.uploadRecordingDeviceData(deviceId, patientUuid, dataFormat, content);
    }

    private void writeJSON(EncryptedAndSignedUploadDO encryptedAndSignedUploadDO, FileOutputStream f) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(f, encryptedAndSignedUploadDO);

    }

}
