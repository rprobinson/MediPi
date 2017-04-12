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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Class to poll the WIFI connection to display to the user
 *
 * @author rick@robinsonhq.com
 */
public class WIFIConnectionMonitor
        implements Runnable {

    private static final String ALERTBANNERMESSAGE = "No WIFI - Transmit not available";
    private static final String CLASSKEY = "vpnConnectionManager";
    private IntegerProperty wifiProperty = new SimpleIntegerProperty(-1);
    private static String OS = null;
    private MediPi medipi = null;

    /**
     * Constructor for PollIncomingMessage class
     *
     * @throws java.lang.Exception
     */
    public WIFIConnectionMonitor(MediPi m) throws Exception {
        medipi = m;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            OS = "WIN";
            // NO CODE YET TO DETECT WIFI CONNECTION FOR Windows systems therefore throw exception
            throw new Exception("Detection of WIFI connection not currently supported for Windows systems - see class: " + this.getClass().getName());
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            OS = "UNIX";
        } else {
            throw new Exception("The MediPi WIFI Manager does not currently support " + os);
        }
    }

    @Override
    public void run() {
        System.out.println("WIFIConnection run at: " + Instant.now());
        if (OS.equals("WIN")) {
        } else if (OS.equals("UNIX")) {
            try {
                Process wifiConnectionProcess = Runtime.getRuntime().exec("iwgetid -r");
                InputStreamReader wifiIn = new InputStreamReader(wifiConnectionProcess.getInputStream());
                String line = null;
                BufferedReader br = new BufferedReader(wifiIn);
                if ((line = br.readLine()) != null) {
                    System.out.println(line);
                    AlertBanner.getInstance().removeAlert(CLASSKEY);
                    wifiProperty.set(MediPi.WIFICONNECTED);
                    medipi.wifiSync.set(true);
                    
                } else {
                    System.out.println(ALERTBANNERMESSAGE);
                    AlertBanner.getInstance().addAlert(CLASSKEY, ALERTBANNERMESSAGE);
                    wifiProperty.set(MediPi.WIFINOTCONNECTED);
                    medipi.wifiSync.set(false);
                }
            } catch (IOException ex) {
                Logger.getLogger(WIFIConnectionMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected IntegerProperty getWIFIProperty() {
        return wifiProperty;
    }
}
