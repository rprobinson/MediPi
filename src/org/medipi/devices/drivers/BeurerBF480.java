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
package org.medipi.devices.drivers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.medipi.logging.MediPiLogger;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.devices.Scale;

/**
 * A concrete implementation of a specific device - Beurer BF480 Diagnostic Scale.
 *
 * This class calls a python script retrieves data via the standard output from the calling CLI
 *
 * @author rick@robinsonhq.com
 */
public class BeurerBF480 extends Scale {

    private static final String MAKE = "Beurer";
    private static final String MODEL = "BF-480";
    // The number of increments of the progress bar - a value of 0 removes the progBar
    private static final Double PROGBARRESOLUTION = 64D;
    String pythonScript;
    String user;

    /**
     * Constructor for BeurerBF480
     */
    public BeurerBF480() {
    }

    // load the python script to read
    @Override
    public String init() throws Exception {
        //find the python script location
        String deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        pythonScript = medipi.getProperties().getProperty(deviceNamespace + ".python");
        if (pythonScript == null || pythonScript.trim().length() == 0) {
            String error = "Cannot find python script for driver for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".python";
            MediPiLogger.getInstance().log(BeurerBF480.class.getName(), error);
            return error;
        }
        //find the user which the data should be collected from (1-10)
        user = medipi.getProperties().getProperty(deviceNamespace + ".user").trim();
        if (user == null || user.length() == 0) {
            String error = "Cannot find user for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".user";
            MediPiLogger.getInstance().log(BeurerBF480.class.getName(), error);
            return error;
        }
        try {
            int i = Integer.parseInt(user);
            if (i > 10 || i < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            String error = "Cannot find valid user number for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".user = " + user.trim();
            MediPiLogger.getInstance().log(BeurerBF480.class.getName(), error);
            return error;
        }
        progressBarResolution = PROGBARRESOLUTION;
        return super.init();

    }

    /**
     * method to get the Make and Model of the device
     *
     * @return make and model of device
     */
    @Override
    public String getName() {
        return MAKE + ":" + MODEL;
    }

    /**
     * Method to call a python script to download the data from the device. This
     * data is passed to the standard output and read into the buffered reader
     * to be digested by the generic device class
     *
     * @return data read from device
     */
    @Override
    public BufferedReader downloadData() {
        try {
            if (medipi.getDebugMode() == MediPi.DEBUG) {
                System.out.println(pythonScript);
            }
            String[] callAndArgs = {"python", pythonScript, user, separator};
            Process p = Runtime.getRuntime().exec(callAndArgs);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return stdInput;
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Download of data unsuccessful", ex, Thread.currentThread());
            return null;
        }

    }


}
