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

import org.warlock.spine.messaging.SendSpineMessage;
import org.medipi.MediPiMessageBox;
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
public class SpineTransmitter extends Transmitter {

    private static final String SENDERADDRESS = "org.warlock.itk.router.senderaddress";
    private static final String RECIPIENTADDRESS = "medipi.recipientaddress";
    private static final String AUDITIDENTITY = "org.warlock.itk.router.auditidentity";

    private SendSpineMessage ssm;
    private String transmissionResponse = "";

    /**
     * Constructor for SpineTransmitter
     *
     */
    public SpineTransmitter() {

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

        senderAddress = System.getProperty(SENDERADDRESS);
        recipientAddress = System.getProperty(RECIPIENTADDRESS);
        auditIdentity = System.getProperty(AUDITIDENTITY);

        // Call the class which controls SpineTools in order to transmit data 
        try {
            ssm = new SendSpineMessage();
        } catch (Exception e) {
            String error = "Failed to initialise the connection to Spine";
            transmitButton.setDisable(true);
            return error;
        }

        return super.init();
    }


    /**
     * Transmit the message using the chosen method
     *
     * @param message - payload to be wrapped and transmitted
     * @return boolean - success of the transmission
     */
    @Override
    public Boolean transmit(DistributionEnvelope message) {
        try {
            if(ssm.sendSpineMessage(message.toString())){
                transmissionResponse = "Message transmitted sucessully";
                return true;
            } else{
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
