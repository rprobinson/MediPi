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
package org.medipi.devices.drivers;

import javafx.scene.image.ImageView;

import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.Thermometer;

/**
 * An implementation of a specific device - BraunPro6000

 TODO; this class need refactoring in asscociation with the Thermometer class.
 * This class is essentially a dummy to provide some formatting data to the
 * "generic" device class
 *
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public class BraunPro6000 extends Thermometer {

    private static final String MAKE = "Braun";
    private static final String MODEL = "Pro";
    private static final String DISPLAYNAME = "Braun Pro 6000 Tympanic Thermometer";
    // The number of increments of the progress bar - a value of 0 removes the progBar
    private ImageView graphic;

    /**
     * Constructor for BraunPro6000
     */
    public BraunPro6000() {
    }

    // initialise and load the configuration data
    @Override
    public String init() throws Exception {
        String deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        columns.add("iso8601time");
        format.add("DATE");
        units.add("NONE");
        columns.add("temperature");
        format.add("DOUBLE");
        units.add("CELSIUS");
        return super.init();
    }

    /**
     * method to get the Make of the device
     *
     * @return make of device
     */
    @Override
    public String getMake() {
        return MAKE;
    }

    /**
     * method to get the Model of the device
     *
     * @return model of device
     */
    @Override
    public String getModel() {
        return MODEL;
    }

    /**
     * method to get the Display Name of the device
     *
     * @return displayName of device
     */
    @Override
    public String getSpecificDeviceDisplayName() {
        return DISPLAYNAME;
    }

    @Override
    public void resetDevice() {
        super.resetDevice();
    }

    /**
     * Method to to download the data from the device. This data is digested by
     * the generic device class
     */
    @Override
    protected void downloadData() {
        try {
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Download of data unsuccessful", ex);
        }
    }
}
