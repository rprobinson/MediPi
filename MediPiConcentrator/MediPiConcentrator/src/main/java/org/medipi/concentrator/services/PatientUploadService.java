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

import java.util.Date;
import javax.servlet.ServletContext;
import org.medipi.concentrator.dataformat.PatientUploadDataFormat;
import org.medipi.security.UploadEncryptionAdapter;
import org.medipi.concentrator.entities.Patient;
import org.medipi.concentrator.exception.BadRequest400Exception;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.utilities.Utilities;
import org.medipi.model.DevicesPayloadDO;
import org.medipi.model.EncryptedAndSignedUploadDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to assess that the incoming request is for a valid device and
 * patient then pass the payload for parsing
 *
 * 1. checks that the incoming parameters are present exist in the database and
 * relate to each other. If not then a new patient may be created and registered
 * to the device being used
 *
 * 2.depending on the header Data-Format then the appropriate data parsing class
 * is chosen
 *
 * @author rick@robinsonhq.com
 */
@Service
public class PatientUploadService {

    @Autowired
    private MediPiLogger logger;

    @Autowired
    ServletContext servletCtx;

    @Autowired
    private PatientDeviceValidationService patientDeviceValidationService;

    @Autowired
    private UploadEncryptionAdapter patientEncryptionAdapter;

    /**
     * uploadRecordingDevice - Method to decide that data is to be uploaded and
     * based upon the configuration choose a data format of the uploaded data.
     * The data format that is currently implemented is a first pass example and
     * can be (and should be) improved upon but works
     *
     * @param hardware_name incoming deviceId parameter from RESTful message
     * @param patientUuid incoming patientUuid parameter from RESTful message
     * @param dataFormat incoming Data-Format HTTP header parameter from RESTful
     * message
     * @param content incoming message contents
     * @return status only response
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<?> uploadRecordingDeviceData(String hardware_name, String patientUuid, String dataFormat, EncryptedAndSignedUploadDO content) {
        // OK device and patient are now veried as being registered with each other
        ResponseEntity<?> r = this.patientDeviceValidationService.validate(hardware_name, patientUuid);
        if (r.getStatusCode() == HttpStatus.ACCEPTED || r.getStatusCode() == HttpStatus.OK) {
            // using the HTTP header Data-Format choose the incoming message data format
            if (servletCtx.getAttribute(dataFormat) != null) {
                PatientUploadDataFormat patientUploadFormat = (PatientUploadDataFormat) servletCtx.getAttribute(dataFormat);
                DevicesPayloadDO payload = null;
                try {
                    payload = (DevicesPayloadDO) patientEncryptionAdapter.decryptAndVerify(content);
                    logger.log(PatientUploadService.class.getName(), new Date().toString() + " Encrypted Payload with uuid: " + content.getUploadUuid() + " has been sucessfully decrypted. Upload date: " + payload.getUploadedDate() + "decrypted uuid: " + payload.getUploadUuid());
                } catch (Exception e) {
                    throw new BadRequest400Exception("Decryption exception: " + e.getLocalizedMessage());
                }
                if (patientUploadFormat.process(payload, new Patient(patientUuid))) {
                    return r;
                } else {
                    throw new InternalServerError500Exception("Internal Server Error");
                }

            } else {
                //The Data-Format HTTP header in the incoming request is missing or not a supported format 
                logger.log(PatientUploadService.class.getName() + ".dataValidationIssue", "The Data-Format HTTP header in the incoming request is missing or not a supported format: " + dataFormat);
                throw new BadRequest400Exception("The Data-Format HTTP header in the incoming request is missing or not a supported format: " + dataFormat);

            }
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
