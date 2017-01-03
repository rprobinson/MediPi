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

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.medipi.clinical.dao.AlertDAOImpl;
import org.medipi.clinical.entities.Alert;
import org.medipi.clinical.entities.Patient;
import org.medipi.clinical.logging.MediPiLogger;
import org.medipi.clinical.utilities.Utilities;
import org.medipi.model.AlertDO;
import org.medipi.model.AlertListDO;
import org.medipi.security.CertificateDefinitions;
import org.medipi.model.EncryptedAndSignedUploadDO;
import org.medipi.security.UploadEncryptionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

    private static final String MEDIPICLINICALMAXNUMBEROFRETRIES = "medipi.clinical.alert.maxnumberofretries";

    @Autowired
    private AlertDAOImpl alertDAOImpl;
    @Autowired
    private Utilities utils;

    //Path for posting alerts for patients
    @Value("${medipi.clinical.alert.resourcepath}")
    private String alertPatientResourcePath;
    //Path for posting alerts for patients
    @Value("${medipi.clinical.patientcertificate.resourcepath}")
    private String patientCertificateResourcePath;

    public void resendAlerts(RestTemplate restTemplate) {
        String maxRetriesString = utils.getProperties().getProperty(MEDIPICLINICALMAXNUMBEROFRETRIES);
        int maxRetries;
        if (maxRetriesString == null || maxRetriesString.trim().length() == 0) {
            maxRetries = 3;
        } else {
            try {
                maxRetries = Integer.parseInt(maxRetriesString);
            } catch (NumberFormatException numberFormatException) {
                MediPiLogger.getInstance().log(SendAlertService.class.getName() + "error", "Error - Cant read the max number of retires for an alert from the properties file: " + numberFormatException.getLocalizedMessage());
                System.out.println("Error - Cant read the max number of retires for an alert from the properties file: " + numberFormatException.getLocalizedMessage());
                maxRetries = 3;
            }
        }
        List<Alert> aList = alertDAOImpl.findByNullTransmitSuccessDate(maxRetries);
        if (aList != null && !aList.isEmpty()) {
            for (Alert alert : aList) {
                //create the alert data object to be serialised to the concentrator
                AlertDO alertDO = new AlertDO(alert.getPatientUuid().getPatientUuid());
                alertDO.setAlertId(alert.getAlertId());
                alertDO.setAlertText(alert.getAlertText());
                alertDO.setAlertTime(alert.getAlertTime());
                alertDO.setType(alert.getDataId().getAttributeId().getTypeId().getType());
                alertDO.setAttributeName(alert.getDataId().getAttributeId().getAttributeName());
                alertDO.setDataValue(alert.getDataId().getDataValue());
                alertDO.setDataValueTime(alert.getDataId().getDataValueTime());
                List<AlertDO> adoList = new ArrayList<>();
                adoList.add(alertDO);
                AlertListDO aldo = new AlertListDO(alert.getPatientUuid().getPatientUuid(), adoList);
                if (!sendAlert(aldo, alert.getPatientUuid(), restTemplate)) {
                    for (AlertDO ado : aldo.getAlert()) {
                        Alert a = alertDAOImpl.findByPrimaryKey(ado.getAlertId());
                        a.setRetryAttempts(a.getRetryAttempts() + 1);
                        alertDAOImpl.update(a);
                    }

                }
            }
        }

    }

    public boolean sendAlert(AlertListDO alertListDO, Patient patient, RestTemplate restTemplate) {

        // create a URL of the concentrator inclusing the patient group uuid and the last sync time
        URI targetUrl = UriComponentsBuilder.fromUriString(patientCertificateResourcePath)
                .path("/")
                .path(patient.getPatientUuid())
                .build()
                .toUri();
        byte[] response;
        try {
            // transmit request to concentrator
            response = restTemplate.getForObject(targetUrl, byte[].class);
            System.out.println(response);

        } catch (Exception e) {
            MediPiLogger.getInstance().log(SendAlertService.class.getName() + "error", "Error in connectiong to target system using SSL: " + e.getLocalizedMessage());
            System.out.println("Error in connectiong to target system using SSL: " + e.getLocalizedMessage());
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
            EncryptedAndSignedUploadDO encryptedMessage = uploadEncryptionAdapter.encryptAndSign(alertListDO);
            HttpEntity<EncryptedAndSignedUploadDO> alertRequest = new HttpEntity<>(encryptedMessage);
            // create a URL of the concentrator Alert url including the patient uuid
            URI alertUrl = UriComponentsBuilder.fromUriString(alertPatientResourcePath)
                    .path("/")
                    .path(patient.getPatientUuid())
                    .build()
                    .toUri();

            ResponseEntity<?> x = restTemplate.postForEntity(alertUrl, alertRequest, ResponseEntity.class);
            try {
                if (x.getStatusCode() == HttpStatus.OK) {
                    // update the alert DB when it has been sucessfully sent to the concentrator
                    // Note: The only thing this proves is that it was sucessfully transmitted and persisted to
                    // the concentrator and makes no claim on its progress to the patient unit per se
                    boolean doneSomething = false;
                    for (AlertDO ado : alertListDO.getAlert()) {
                        Alert a = alertDAOImpl.findByPrimaryKey(ado.getAlertId());
                        a.setTransmitSuccessDate(new Date());
                        alertDAOImpl.update(a);
                        doneSomething = true;
                    }
                    if (doneSomething) {
                        return true;
                    } else {
                        return false;
                    }
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
