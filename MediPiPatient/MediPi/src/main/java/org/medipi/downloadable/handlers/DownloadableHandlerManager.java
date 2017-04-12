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
package org.medipi.downloadable.handlers;

import java.util.HashMap;
import org.medipi.MediPiMessageBox;
import org.medipi.logging.MediPiLogger;
import org.medipi.messaging.rest.RESTfulMessagingEngine;
import org.medipi.model.DownloadableDO;

/**
 * Class to manage and keep track of which Downloadable handlers are required
 * and called for an incoming download
 *
 * @author rick@robinsonhq.com
 */
public class DownloadableHandlerManager {

    private HashMap<String, DownloadableHandler> handlers = null;
    private final Exception bootException = null;

    /**
     * Constructor
     */
    public DownloadableHandlerManager() {
        handlers = new HashMap<>();
    }

    /**
     * Method to add a new handler
     *
     * @param name handler name
     * @param instance Handler instance
     */
    public void addHandler(String name, DownloadableHandler instance) {
        if (handlers.get(name) == null) {
            handlers.put(name, instance);
        } else{
            System.out.println("Handler named "+ name + " already registered with DownloadableHandlerManager");
            MediPiLogger.getInstance().log(DownloadableHandlerManager.class.getName() + "error", "Handler named "+ name + " already registered with DownloadableHandlerManager");
        }
    }

    /**
     * Method to find if there are any handlers registered to determine if MediPi needs to pool the concentrator
     *
     * @return boolean if there have been any handlers registered
     */
    public boolean hasHandlers(){
        return !handlers.isEmpty();
    }
    /**
     * Method to call handle on the downloadable object handler
     *
     * @param downloadable
     */
    public void handle(DownloadableDO downloadable) {
        if (bootException != null) {
            MediPiMessageBox.getInstance().makeErrorMessage("There has been an issue starting the DownloadableHandlerManager: ", bootException);
            MediPiLogger.getInstance().log(DownloadableHandlerManager.class.getName() + "error", "There has been an issue starting the DownloadableHandlerManager: " + bootException.getLocalizedMessage());
        }

        DownloadableHandler h = handlers.get(downloadable.getDownloadType());
        if (h == null) {
            MediPiMessageBox.getInstance().makeErrorMessage("An incoming update has failed: No handler found: " + downloadable.getDownloadType(), null);
            MediPiLogger.getInstance().log(DownloadableHandlerManager.class.getName() + "error", "An incoming update has failed: No handler found: " + downloadable.getDownloadType());
        } else {
            h.handle(downloadable);
        }
    }

}
