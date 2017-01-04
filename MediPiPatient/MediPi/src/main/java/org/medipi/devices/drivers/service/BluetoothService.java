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
package org.medipi.devices.drivers.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.StreamConnection;
import org.medipi.MediPiProperties;

/**
 * Abstract Service class to bring together common elements for classes
 * communicating with the bluetooth serial devices
 *
 * @author rick@robinsonhq.com
 */
public abstract class BluetoothService {

    protected StreamConnection c = null;
    protected InputStream is = null;
    protected OutputStream os = null;
    protected int btConnectionAttemptPeriod = 15;
    protected String originalMessage = "Start";
    protected String searchingMessage = "Searching";
    protected String connectionMessage = "Connecting";
    protected String downloadingMessage = "Downloading";

    public BluetoothService() throws Exception {
        String connAttempt = MediPiProperties.getInstance().getProperties().getProperty("medipi.bluetooth.connectionattemptperiod");
        if (connAttempt != null) {
            btConnectionAttemptPeriod = Integer.parseInt(connAttempt);
            if (btConnectionAttemptPeriod < 0) {
                throw new Exception("The Bluetooth Connection Attempt period is not a positive integer");
            }
        }

    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public void setSearchingMessage(String searchingMessage) {
        this.searchingMessage = searchingMessage;
    }

    public void setConnectionMessage(String connectionMessage) {
        this.connectionMessage = connectionMessage;
    }

    public void setDownloadingMessage(String downloadingMessage) {
        this.downloadingMessage = downloadingMessage;
    }

    public void closeConnection() {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                //do nothing if it fails to close connection as it'is already down
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException ex) {
                //do nothing if it fails to close connection as it'is already down
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                //do nothing if it fails to close connection as it'is already down
            }
        }
    }

}
