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
package org.medipi.messaging.rest;

import java.util.HashMap;
import java.util.UUID;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.medipi.devices.Transmitter;
import org.medipi.logging.MediPiLogger;
import org.medipi.messaging.vpn.VPNServiceManager;
import org.medipi.model.EncryptedAndSignedUploadDO;

/**
 * Concrete class to call the Restful Transmitter and return the outcome.
 *
 * The Restful Transmitter instance accepts an EncryptedAndSignedDO and will
 * transmit it to the concentrator using the restful interface on there
 *
 * @author rick@robinsonhq.com
 */
public class RESTTransmitter extends Transmitter {

    private static final String MEDIPITRANSMITRESOURCEPATH = "medipi.transmit.resourcepath";

    private RESTfulMessagingEngine rme;

    private String resourcePath;
    private String transmissionResponse = "";

    /**
     * Constructor for RESTTransmitter
     *
     */
    public RESTTransmitter() {
    }

    /**
     * Initiation method called for this Element.
     *
     * Successful initiation of the this class results in a null return. Any
     * other response indicates a failure with the returned content being a
     * reason for the failure
     *
     * @return populated or null for whether the initiation was successful
     * @throws java.lang.Exception
     */
    @Override
    public String init() throws Exception {
        resourcePath = medipi.getProperties().getProperty(MEDIPITRANSMITRESOURCEPATH);
        if (resourcePath == null || resourcePath.trim().equals("")) {
            MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + "constructor", "MediPi resource base path is not set");
            medipi.makeFatalErrorMessage(resourcePath + " - MediPi resource base path is not set", null);
        }

        String[] params = {"{deviceId}", "{patientId}"};
        rme = new RESTfulMessagingEngine(resourcePath + "patientupload", params);

        return super.init();
    }

    /**
     * Transmit the message using the chosen method
     *
     * @param message - Encrypted and signed payload to be transmitted
     * @return String - if transmission was successful return null
     */
    @Override
    public Boolean transmit(EncryptedAndSignedUploadDO message) {
        UUID uuid = UUID.randomUUID();
        VPNServiceManager vpnm = null;
        try {
            //Collect patient and hardware device names to be used as part of the restful path
            String patientCertName = System.getProperty("medipi.patient.cert.name");
            if (patientCertName == null || patientCertName.trim().length() == 0) {
                transmissionResponse = "Patient identity not set";
                MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + ".error", "Patient identity not set");
                return false;
            }
            String deviceCertName = System.getProperty("medipi.device.cert.name");
            if (deviceCertName == null || deviceCertName.trim().length() == 0) {
                transmissionResponse = "Device identity not set";
                MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + ".error", "Device identity not set");
                return false;
            }
            vpnm = VPNServiceManager.getInstance();
            if (vpnm.isEnabled()) {
                vpnm.VPNConnection(VPNServiceManager.OPEN, uuid);
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("deviceId", deviceCertName);
            params.put("patientId", patientCertName);

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Data-Format", "MediPiNative");

            MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + ".info", "New Patient Upload started - MediPiUploadEnvelope UUID: " + message.getUploadUuid());

            Response postResponse = rme.executePut(params, Entity.json(message), headers);

            if (postResponse != null) {
                System.out.println("PatientUpload returned status = " + postResponse.getStatus());
                //POSITIVE RESPONSE
                if (postResponse.getStatus() == Response.Status.OK.getStatusCode() || postResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
                    MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + ".info", "New Patient Upload successfully sent - MediPiUploadEnvelope UUID: " + message.getUploadUuid());
                    transmissionResponse = "Thank you! Your recordings have been sent to your clinician.";
                    return true;
                } else {
                    //ERROR RESPONSE
                    switch (postResponse.getStatus()) {
                        // BAD REQUEST
                        case 400:
                        // ***************** DO SOMETHING WITH 400 *******************
                        // NOT FOUND
                        case 404:
                        // This is returned when the hardware name and patientId do not match
                        // ***************** DO SOMETHING WITH 404 *******************
                        // NOT ACCEPTABLE
                        case 406:
                        // ***************** DO SOMETHING WITH 406 *******************
                        case 417:
                        // UPDATE REQUIRED
                        case 426:
                        // ***************** DO SOMETHING WITH 426 *******************
                        // INTERNAL SERVER ERROR    
                        case 500:
                        default:
                            transmissionResponse = postResponse.readEntity(String.class);
                            return false;
                    }
                }
            }
        } catch (ProcessingException pe) {
            MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + ".error", "Attempt to send data failed - MediPi Concentrator is not available - please try again later. " + pe.getLocalizedMessage());
            transmissionResponse = "Attempt to send data failed - MediPi Concentrator is not available - please try again later.";
            return false;
        } catch (Exception ex) {
            MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + ".error", "Error transmitting message to recipient: " + ex.getLocalizedMessage());
            transmissionResponse = "Error transmitting message to recipient: " + ex.getLocalizedMessage();
            return false;
        } finally {
            if (vpnm != null && vpnm.isEnabled()) {
                try {
                    vpnm.VPNConnection(VPNServiceManager.CLOSE,uuid);
                } catch (Exception ex) {
                    MediPiLogger.getInstance().log(RESTTransmitter.class.getName(), ex);
                }
            }
        }
        return false;
    }

    @Override
    public String getTransmissionResponse() {
        return transmissionResponse;
    }

}
