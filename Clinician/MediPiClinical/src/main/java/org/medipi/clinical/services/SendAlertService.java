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

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import org.medipi.clinical.logging.MediPiLogger;
import org.medipi.clinical.utilities.Utilities;
import org.medipi.model.DirectPatientMessage;
import org.medipi.security.CertificateDefinitions;
import org.medipi.model.EncryptedAndSignedUploadDO;
import org.medipi.security.UploadEncryptionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author rick@robinsonhq.com
 */
@Component
public class SendAlertService {

    @Autowired
    private Utilities utils;

    //Path for posting alerts for patients
    @Value("${medipi.clinical.patientcertificate.resourcepath}")
    private String patientCertificateResourcePath;

    public void resendDirectMessages(Tester tester, RestTemplate restTemplate) {

        List<DirectPatientMessage> retryDirectMessageList = tester.findDirectPatientMessagesToResend();
        for (DirectPatientMessage dpm : retryDirectMessageList) {
            if (!sendDirectMessage(tester, dpm, dpm.getPatientUuid(), restTemplate)) {
                tester.updateDirectPatientMessageTableWithFail(dpm);

            }
        }
    }

    public boolean sendDirectMessage(Tester tester, DirectPatientMessage directPatientMessage, String patientUuid, RestTemplate restTemplate) {

        // create a URL of the concentrator inclusing the patient group uuid and the last sync time
        URI targetUrl = UriComponentsBuilder.fromUriString(patientCertificateResourcePath)
                .path("/")
                .path(patientUuid)
                .build()
                .toUri();
        byte[] response;
        try {
            // transmit request to concentrator
            response = restTemplate.getForObject(targetUrl, byte[].class);
            System.out.println(response);

        } catch (HttpClientErrorException hcee) {
            if (hcee.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                //The patient certificate has not been loaded onto the concentrator therefore ignore sending direct message
                tester.failDirectPatientMessageTable(directPatientMessage);
            }
            return false;
        } catch (Exception e) {
            MediPiLogger.getInstance().log(SendAlertService.class.getName() + "error", "Error in connectiong to target system using SSL: " + e.getLocalizedMessage());
            System.out.println("Error in connectiong to target system using SSL: " + e.getLocalizedMessage());
            tester.failDirectPatientMessageTable(directPatientMessage);
            return false;
        }
        // Send the alert to the concentrator
        try {
            //Set up the encryption and signing adaptor
            UploadEncryptionAdapter uploadEncryptionAdapter = new UploadEncryptionAdapter();
            CertificateDefinitions cd = new CertificateDefinitions(utils.getProperties());
            cd.setSIGNKEYSTORELOCATION("medipi.json.sign.keystore.clinician.location", CertificateDefinitions.INTERNAL);
            cd.setSIGNKEYSTOREALIAS("medipi.json.sign.keystore.clinician.alias", CertificateDefinitions.INTERNAL);
            cd.setSIGNKEYSTOREPASSWORD("medipi.json.sign.keystore.clinician.password", CertificateDefinitions.INTERNAL);
            cd.setEncryptTruststorePEM(response);
            String error = uploadEncryptionAdapter.init(cd, UploadEncryptionAdapter.CLIENTMODE);
            if (error != null) {
                throw new Exception(error);
            }
            EncryptedAndSignedUploadDO encryptedMessage = uploadEncryptionAdapter.encryptAndSign((Serializable) directPatientMessage);
            HttpEntity<EncryptedAndSignedUploadDO> request = new HttpEntity<>(encryptedMessage);
            // create a URL of the concentrator Alert url including the patient uuid
            URI uri = UriComponentsBuilder.fromUriString(tester.getDirectPatientMessageResourcePath())
                    .path("/")
                    .path(patientUuid)
                    .build()
                    .toUri();

            ResponseEntity<?> x = restTemplate.postForEntity(uri, request, ResponseEntity.class);
            try {
                if (x.getStatusCode() == HttpStatus.OK) {
                    // update the alert DB when it has been sucessfully sent to the concentrator
                    // Note: The only thing this proves is that it was sucessfully transmitted and persisted to
                    // the concentrator and makes no claim on its progress to the patient unit per se
                    return tester.updateDirectPatientMessageTableWithSuccess(directPatientMessage);
                } else {
                    return false;
                }
            } catch (IllegalArgumentException iae) {
                MediPiLogger.getInstance().log(SendAlertService.class.getName() + "error", "Error - Cant update the alert with a transmit successful date as it no longer exists in the DB: " + iae.getLocalizedMessage());
                System.out.println("Error - Cant update the alert with a transmit successful date as it no longer exists in the DB: " + iae.getLocalizedMessage());
                return false;
            } catch (HttpServerErrorException hsee) {
                MediPiLogger.getInstance().log(SendAlertService.class.getName() + "error", "Error - Concentrator server has thrown a 500 server error: " + hsee.getLocalizedMessage());
                System.out.println("Error - Concentrator server has thrown a 500 server error: " + hsee.getLocalizedMessage());
                return false;
            } catch (RestClientException rce) {
                MediPiLogger.getInstance().log(SendAlertService.class.getName() + "error", "Error - A rest client error has been thrown: " + rce.getLocalizedMessage());
                System.out.println("Error - A rest client error has been thrown: " + rce.getLocalizedMessage());
                return false;
            }
        } catch (Exception ex) {
            MediPiLogger.getInstance().log(SendAlertService.class.getName() + "error", "Error - Unable to encrypt and sign the ALERT: " + ex.getLocalizedMessage());
            System.out.println("Error - Unable to encrypt and sign the ALERT: " + ex.getLocalizedMessage());
            return false;
        }
    }

}
