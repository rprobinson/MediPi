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
package org.medipi;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.medipi.logging.MediPiLogger;
import org.medipi.messaging.rest.RESTfulMessagingEngine;
import org.medipi.model.DownloadableDO;

/**
 * Class to poll the MediPi Concentrator and request any downloads for the user
 * or device
 *
 * This class polls the concentrator receives the list of responses and calls
 * the appropriate handler
 *
 * @author rick@robinsonhq.com
 */
public class PollDownloads
        implements Runnable {

    private static final String MEDIPITRANSMITRESOURCEPATH = "medipi.transmit.resourcepath";
    private static final String MEDIPIDEVICECERTNAME = "medipi.device.cert.name";
    private static final String MEDIPIPATIENTCERTNAME = "medipi.patient.cert.name";
    private String patientCertName;
    private final String deviceCertName;
    private final String resourcePath;
    private final MediPi medipi;
    private RESTfulMessagingEngine rme;

    /**
     * Constructor for PollIncomingMessage class
     *
     * @param medipi
     */
    public PollDownloads(MediPi medipi) throws Exception {
        this.medipi = medipi;
        resourcePath = medipi.getProperties().getProperty(MEDIPITRANSMITRESOURCEPATH);
        if (resourcePath == null || resourcePath.trim().equals("")) {
            MediPiLogger.getInstance().log(PollDownloads.class.getName() + ".error", "MediPi resource base path is not set");
            medipi.makeFatalErrorMessage(resourcePath + " - MediPi resource base path is not set", null);
        }
        // Get the device Cert
        deviceCertName = System.getProperty(MEDIPIDEVICECERTNAME);
        if (deviceCertName == null || deviceCertName.trim().length() == 0) {
            medipi.makeFatalErrorMessage("MediPi device cert not found", null);
        }
        String[] params = {"{deviceId}", "{patientId}"};
        rme = new RESTfulMessagingEngine(resourcePath + "download", params);
    }

    @Override
    public void run() {

        try {
            // get the patient cert - this is only available after the first login 
            // and therefore no downloads are attempted before the first login
            patientCertName = System.getProperty(MEDIPIPATIENTCERTNAME);
            if (patientCertName == null || patientCertName.trim().length() == 0) {
                // Do not try and download anything before the user password is input for the first time
            } else {
                HashMap<String, Object> hs = new HashMap<>();
                hs.put("deviceId", deviceCertName);
                hs.put("patientId", patientCertName);
                Response listResponse = rme.executeGet(hs);
                //
                if (listResponse != null) {
                    System.out.println("Poll Download returned status = " + listResponse.getStatus());
                    //POSITIVE RESPONSE
                    if (listResponse.getStatus() == Response.Status.OK.getStatusCode()) {
                        List<DownloadableDO> ld = listResponse.readEntity(new GenericType<List<DownloadableDO>>() {
                        });
                        for (DownloadableDO d : ld) {
                            MediPiLogger.getInstance().log(PollDownloads.class.getName() + ".info", "New Downloadable List detected - Downloadable UUID: " + d.getDownloadableUuid());
                            try {
                                medipi.getDownloadableHandlerManager().handle(d);
                            } catch (Exception e) {
                                MediPiMessageBox.getInstance().makeErrorMessage("Error in attempting to download an incoming message/update ", e);
                            }
                        }
                        // Remember that list may be empty - therefore no action
                    } else {
                        //ERROR RESPONSE
                        String err = listResponse.readEntity(String.class);
                        switch (listResponse.getStatus()) {
                            // NOT FOUND
                            case 404:
                            // This is returned when the hardware name and patientId do not match
                            // ***************** DO SOMETHING WITH 404 *******************
                            // UPDATE REQUIRED
                            case 426:
                            // ***************** DO SOMETHING WITH 426 *******************
                            // INTERNAL SERVER ERROR    
                            case 500:
                            default:
                                // ***************** DO SOMETHING WITH EVERY OTHER STATUS CODE *******************
                                System.out.println(err);
                        }
                        MediPiLogger.getInstance().log(PollDownloads.class.getName() + ".error", "Error code: " + listResponse.getStatus() + " detected when trying to return downloadable list");
                    }
                }

            }
        } catch (Exception e) {
            MediPiLogger.getInstance().log(PollDownloads.class.getName() + ".error", "Error detected when attempting to poll the Concentrator: "+ e.getLocalizedMessage());
            MediPiMessageBox.getInstance().makeErrorMessage("Error detected when attempting to poll the Concentrator: "+ e.getLocalizedMessage(), e);

        }

    }
}
