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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import org.medipi.concentrator.controllers.PatientMessagingServiceController;
import org.medipi.concentrator.controllers.PatientUploadServiceController;
import org.medipi.concentrator.dao.PatientCertificateDAOImpl;
import org.medipi.concentrator.dao.PatientDAOImpl;
import org.medipi.concentrator.dao.PatientDownloadableDAOImpl;
import org.medipi.security.UploadEncryptionAdapter;
import org.medipi.concentrator.entities.Patient;
import org.medipi.concentrator.entities.PatientCertificate;
import org.medipi.concentrator.entities.PatientDownloadable;
import org.medipi.concentrator.exception.BadRequest400Exception;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.utilities.Utilities;
import org.medipi.security.CertificateDefinitions;
import org.medipi.model.EncryptedAndSignedUploadDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to allow 3rd party clinical systems to send encrypted and
 * signed text based messages to individual MediPi patient devices for specific
 * patients
 *
 * @author rick@robinsonhq.com
 */
@Service
public class PatientMessagingService {

    @Autowired
    private MediPiLogger logger;

    @Autowired
    private PatientDAOImpl patientDAOImpl;

    @Autowired
    private Utilities utils;

    @Autowired
    private PatientCertificateDAOImpl patientCertificateDAOImpl;

    @Autowired
    PatientDownloadableDAOImpl patientDownloadableDAOImpl;

    @Value("${medipi.concentrator.alertmessagedir}")
    private String alertmessagedir;

    @Value("${medipi.concentrator.simplemessagedir}")
    private String simplemessagedir;

    /**
     * Direct Message interface. This interface simply saves and makes available
     * the encrypted and signed message to the specified patient. It does not
     * decrypt the message as it has no access to the private keys of the
     *
     * TODO - Currently not sure what should be done with the version field?
     *
     * @param patientUuid incoming patientUuid parameter from RESTful message
     * @param content Encrypted and Signed Data object representation of the
     * message and its meta-data
     * @return Encrypted and Signed Response
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<EncryptedAndSignedUploadDO> persistDirectMessage(String patientUuid, EncryptedAndSignedUploadDO content, int messageType) {
        try {
            logger.log(PatientMessagingService.class.getName(), new Date().toString() + " Encrypted Direct Message Payload with uuid: " + content.getUploadUuid() + " has been sucessfully decrypted. Patient uuid: " + patientUuid);
            //validate that the patient is registered with this DB
            Patient patient = patientDAOImpl.findByPrimaryKey(patientUuid);
            if (patient == null) {
                throw new BadRequest400Exception("The patient is not registered on the concentrator when clinical system attempting to send a message to the patient");
            }
            //validate that the concentrator serves a certificate for this patient
            PatientCertificate patientCertificate = null;
            try {
                patientCertificate = patientCertificateDAOImpl.findByPatientUuid(patientUuid);
            } catch (EmptyResultDataAccessException e) {
                throw new BadRequest400Exception("The patient certificate is not registered on the concentrator when clinical system attempting to send a message to the patient");
            } catch (Exception e) {
                throw new InternalServerError500Exception("Internal Server Error " + e.getLocalizedMessage());
            }

            try {
                FileOutputStream fop = null;
                try {
                    Date messageDate = new Date();
                    StringBuilder fnb = new StringBuilder();
                    switch (messageType) {
                        case PatientMessagingServiceController.ALERT:
                            fnb.append(alertmessagedir);
                            fnb.append(System.getProperty("file.separator"));
                            fnb.append(messageDate.getTime());
                            fnb.append("-Alert");
                            break;
                        case PatientMessagingServiceController.SIMPLEMESSAGE:
                            fnb.append(simplemessagedir);
                            fnb.append(System.getProperty("file.separator"));
                            fnb.append(messageDate.getTime());
                            fnb.append("-SimpleMessage");
                            break;
                        default:
                            break;
                    }
                    fnb.append(".txt");
                    File file = new File(fnb.toString());
                    fop = new FileOutputStream(file);
                    // if file doesnt exists, then create it
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    writeJSON(content, fop);
                    fop.flush();
                    fop.close();

                    PatientDownloadable pd = new PatientDownloadable(UUID.randomUUID().toString());
                    pd.setPatientUuid(patient);
                    pd.setScriptLocation(fnb.toString());
                    pd.setVersion("1.0");
                    pd.setVersionAuthor("concentrator-created");
                    pd.setVersionDate(messageDate);
                    pd.setSignature(createSignature(pd, file.getName()));

                    patientDownloadableDAOImpl.save(pd);

                    logger.log(PatientUploadServiceController.class.getName(), new Date().toString() + " Written Direct Message for Patient: " + patientUuid);
                } catch (IOException e) {
                    logger.log(PatientUploadServiceController.class.getName() + ".error", "Cannot save outbound Direct Message message payload to local drive - check the configured directory: " + alertmessagedir);
                    throw new InternalServerError500Exception("Unable to save Direct Message to file and persist to DB: " + e.getLocalizedMessage());
                } finally {
                    try {
                        if (fop != null) {
                            fop.close();
                        }
                    } catch (IOException e) {
                        logger.log(PatientUploadServiceController.class.getName() + ".error", "Cannot save outbound Direct Message message payload to local drive - check the configured directory: " + alertmessagedir);
                    }
                }

                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                throw new BadRequest400Exception("Unable to save Direct Message exception: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            throw new BadRequest400Exception("Direct Message Decryption exception: " + e.getLocalizedMessage());
        }
    }

    private void writeJSON(EncryptedAndSignedUploadDO encryptedAndSignedUploadDO, FileOutputStream f) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(f, encryptedAndSignedUploadDO);

    }

    private String createSignature(PatientDownloadable d, String fileName) throws NoSuchAlgorithmException, Exception {
        // THIS IS AS A STAND IN FOR WHEN AN INTERFACE IDS CREATED WHICH WILL SIGN THE DB ENTRY
        UploadEncryptionAdapter uploadEncryptionAdapter = new UploadEncryptionAdapter();
        CertificateDefinitions cd;
        cd = fetchClinicianSigningCerts();
        String error = uploadEncryptionAdapter.init(cd, UploadEncryptionAdapter.SIGNMODE);
        if (error != null) {
            throw new Exception("Signing initailisation failed - " + error);
        }
        StringBuilder digestSubject = new StringBuilder();
        digestSubject.append(d.getDownloadableUuid())
                .append(fileName)
                .append(d.getVersion())
                .append(d.getVersionAuthor())
                .append(d.getVersionDate().getTime());
        String signature = uploadEncryptionAdapter.signPayload(digestSubject.toString().getBytes());
        return signature;
    }

    private CertificateDefinitions fetchClinicianSigningCerts() throws Exception {
        CertificateDefinitions cd = new CertificateDefinitions(utils.getProperties());
        cd.setSIGNKEYSTORELOCATION("medipi.json.sign.keystore.clinician.location", CertificateDefinitions.INTERNAL);
        cd.setSIGNKEYSTOREALIAS("medipi.json.sign.keystore.clinician.alias", CertificateDefinitions.INTERNAL);
        cd.setSIGNKEYSTOREPASSWORD("medipi.json.sign.keystore.clinician.password", CertificateDefinitions.INTERNAL);
        return cd;
    }
}
