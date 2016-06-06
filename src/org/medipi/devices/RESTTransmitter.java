/*
 Copyright 2016  Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>

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
package org.medipi.devices;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.medipi.logging.MediPiLogger;
import org.warlock.itk.distributionenvelope.DistributionEnvelope;

/**
 * Concrete class to call the Spine Transmitter and return the outcome.
 *
 * The SpineTransmitter uses SpineTools. The message which has been used is the
 * ITK Trunk message. This is because it can take as a 3rd MIME part a
 * DistributionEnvelope. This is a forward express message which uses
 * asynchronously transmitted ebXML acknowledgements. However this isn't
 * appropriate for a device which has no ports left open for asynchronous
 * response. Currently the message is successfully sent but the acknowledgement
 * and any subsequent actions mandated by the ITK Trunk forward express pattern
 * is ignored. This transmission method must be changed for a production version
 * as there is no reliability
 *
 * There is some functionality left in here to be used in the future but is not
 * currently employed: Transmit Status
 *
 *
 * @author rick@robinsonhq.com
 */
public class RESTTransmitter extends Transmitter {

    private static final String SENDERADDRESS = "medipi.distributionenvelope.senderaddress";
    private static final String RECIPIENTADDRESS = "medipi.distributionenvelope.recipientaddress";
    private static final String AUDITIDENTITY = "medipi.distributionenvelope.auditidentity";
    private static final String MEDIPITRANSMITRESOURCEPATH = "medipi.transmit.resourcepath";

    private WebTarget baseTarget;
    private WebTarget patientUpdateTarget;
    private WebTarget deviceTarget;
    private WebTarget trackingTarget;

    private String resourcePath;
    private String transmissionResponse = "";

    /**
     * Constructor for SpineTransmitter
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
        //Set up DistibutionEnevelope properties
        senderAddress = medipi.getProperties().getProperty(SENDERADDRESS);
        if (senderAddress == null || senderAddress.trim().equals("")) {
            MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + "constructor", "MediPi DistributionEnvelope senderAddress is not set");
            medipi.makeFatalErrorMessage(senderAddress + " - MediPi DistributionEnvelope senderAddress is not set", null);
        }
        recipientAddress = medipi.getProperties().getProperty(RECIPIENTADDRESS);
        if (recipientAddress == null || recipientAddress.trim().equals("")) {
            MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + "constructor", "MediPi DistributionEnvelope recipientAddress is not set");
            medipi.makeFatalErrorMessage(recipientAddress + " - MediPi DistributionEnvelope recipientAddress is not set", null);
        }
        auditIdentity = medipi.getProperties().getProperty(AUDITIDENTITY);
        if (auditIdentity == null || auditIdentity.trim().equals("")) {
            MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + "constructor", "MediPi DistributionEnvelope auditIdentity is not set");
            medipi.makeFatalErrorMessage(auditIdentity + " - MediPi DistributionEnvelope auditIdentity is not set", null);
        }
        resourcePath = medipi.getProperties().getProperty(MEDIPITRANSMITRESOURCEPATH);
        if (resourcePath == null || auditIdentity.trim().equals("")) {
            MediPiLogger.getInstance().log(RESTTransmitter.class.getName() + "constructor", "MediPi resource base path is not set");
            medipi.makeFatalErrorMessage(resourcePath + " - MediPi resource base path is not set", null);
        }

        Client client = ClientBuilder.newClient();

        baseTarget = client.target(resourcePath);
        patientUpdateTarget = baseTarget.path("patientupload");
        deviceTarget = patientUpdateTarget.path("{deviceId}");
        trackingTarget = deviceTarget.path("{patientId}");

        return super.init();
    }

    /**
     * Transmit the message using the chosen method
     *
     * @param message - payload to be wrapped and transmitted
     * @return String - if transmission was successful return null
     */
    @Override
    public Boolean transmit(DistributionEnvelope message) {
        try {
            String patientCertName = System.getProperty("medipi.patient.cert.name");
            if (patientCertName == null || patientCertName.trim().length() == 0) {
                transmissionResponse = "Patient identity not set";
                return false;
            }
            String deviceCertName = System.getProperty("medipi.device.cert.name");
            if (deviceCertName == null || deviceCertName.trim().length() == 0) {
                transmissionResponse = "Device identity not set";
                return false;
            }
            Response postResponse = trackingTarget
                    .resolveTemplate("deviceId", deviceCertName)
                    .resolveTemplate("patientId", patientCertName)
                    .request()
                    .header("Data-Format", "MediPiNative")
                    .put(Entity.xml(message.toString()));
            System.out.println("returned status = " + postResponse.getStatus());
//            System.out.println("returned body = " + postResponse.readEntity(String.class));
            

            switch (postResponse.getStatus()) {
                case 200:
                case 202:
                    transmissionResponse = postResponse.readEntity(String.class);
                    return true;
                case 400:
                case 406:
                case 417:
                case 426:
                case 500:
                    ErrorMessage err = postResponse.readEntity(ErrorMessage.class);
                    System.out.println(err.getErrorMessage());
                    transmissionResponse = err.getErrorMessage();
                    return false;
                default:
                    transmissionResponse = "Error transmitting message to recipient";
                    return false;
            }
        } catch (Exception ex) {
            transmissionResponse = "Error transmitting message to recipient";
            return false;
        }
    }

    @Override
    public String getTransmissionResponse() {
        return transmissionResponse;
    }

}
