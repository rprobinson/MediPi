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

import java.util.ArrayList;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbPipe;

import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.BloodPressure;
import org.medipi.devices.drivers.domain.BM55Measurement;
import org.medipi.devices.drivers.domain.BM55User;
import org.medipi.devices.drivers.service.BM55USBService;
import org.medipi.devices.drivers.service.USBService;
import org.medipi.logging.MediPiLogger;

/**
 * A concrete implementation of a specific device - Beurer BM55 Blood Pressure
 * Meter
 *
 * This class retrieves the data from Beurer BM55 by communicating over USB
 *
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public class BeurerBM55 extends BloodPressure {

    private static final String MAKE = "Beurer";
    private static final String MODEL = "BM-55";
    private static final String DISPLAYNAME = "Beurer BM-55 Blood Pressure Meter";
    private static final String STARTBUTTONTEXT = "Start";
    // The number of increments of the progress bar - a value of 0 removes the progBar
    private static final Double PROGBARRESOLUTION = 60D;

    private static final short VENDOR_ID = (short) 0x0c45;
    private static final short PRODUCT_ID = (short) 0x7406;
    private String user;

    private ImageView graphic;

    final byte requestType = 33;
    final byte request = 0x09;
    final short value = 521;
    final short index = 0;

    private final USBService usbService;

    /**
     * Constructor for BeurerBM55
     */
    public BeurerBM55() {
        this.usbService = new BM55USBService();
    }

    // initialise and load the configuration data
    @Override
    public String init() throws Exception {
        String deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        //find the user which the data should be collected from (1-10)
        user = medipi.getProperties().getProperty(deviceNamespace + ".user").trim();
        if (user == null || user.length() == 0) {
            String error = "Cannot find user for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".user";
            MediPiLogger.getInstance().log(BeurerBF480.class.getName(), error);
            return error;
        }
        if (!user.toUpperCase().equals("A") && !user.toUpperCase().equals("B")) {
            String error = "Cannot find valid user for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".user = " + user.trim();
            MediPiLogger.getInstance().log(BeurerBF480.class.getName(), error);
            return error;
        }
        progressBarResolution = PROGBARRESOLUTION;

        graphic = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        graphic.setRotate(90);
        initialGraphic = graphic;
        initialButtonText = STARTBUTTONTEXT;
// define the data to be collected
        columns.add("iso8601time");
        columns.add("systol");
        columns.add("diastol");
        columns.add("pulserate");
        columns.add("rest");
        columns.add("arrhythmia");
        format.add("DATE");
        format.add("INTEGER");
        format.add("INTEGER");
        format.add("INTEGER");
        format.add("BOOLEAN");
        format.add("BOOLEAN");
        units.add("NONE");
        units.add("mmHg");
        units.add("mmHg");
        units.add("BPM");
        units.add("NONE");
        units.add("NONE");
        return super.init();

    }

    /**
     * Method to to download the data from the device. This data is digested by
     * the generic device class
     */
    @Override
    public void downloadData(final VBox meterVBox) {
        Task<String> task = new Task<String>() {
            ArrayList<ArrayList<String>> data = new ArrayList<>();

            @Override
            protected String call() throws Exception {
                String operationStatus = "Unknown error connecting to Meter";
                UsbPipe connectionPipe = null;
                try {

                    BM55User readingsUser = BM55User.valueOf(BeurerBM55.this.user);
                    UsbDevice device = usbService.getUSBDevice(VENDOR_ID, PRODUCT_ID);

                    connectionPipe = usbService.getUSBConnection(device, 0, -127);
                    UsbControlIrp usbControl = usbService.getUSBControl(device, requestType, request, value, index);
                    connectionPipe.open();
                    usbService.initialiseDevice(device, usbControl, connectionPipe);
                    int numberOfReadings = usbService.getNumberOfReadings(device, usbControl, connectionPipe);

                    progressBarResolution = (double) numberOfReadings;

                    updateProgress(0D, progressBarResolution);

                    byte[] dataByte;
                    for (int readingsCounter = 1; readingsCounter < numberOfReadings; readingsCounter++) {
                        usbService.writeDataToInterface(device, usbControl, new byte[]{(byte) 0xA3, (byte) readingsCounter}, USBService.DEFAULT_BYTE_ARRAY_LENGTH_8, USBService.PADDING_BYTE_0xF4);
                        dataByte = usbService.readData(connectionPipe, 8);
                        final BM55Measurement measurement = new BM55Measurement(dataByte);
                        if (measurement.getUser().equals(readingsUser)) {
                            // add the data to the data array
                            data.add(measurement.getAllValues());

                        }
                        updateProgress(readingsCounter, BeurerBM55.this.progressBarResolution);
                    }
                    usbService.terminateDeviceCommunication(device, usbControl, connectionPipe);
                    updateProgress(progressBarResolution, progressBarResolution);
                    operationStatus = "SUCCESS";
                } catch (Exception ex) {
                    operationStatus = ex.getLocalizedMessage();
                } finally {
                    if (connectionPipe != null && connectionPipe.isOpen()) {
                        try {
                            connectionPipe.close();
                            connectionPipe.getUsbEndpoint().getUsbInterface().release();
                        } catch (UsbException e) {
                            //Do nothing
                        }
                    }
                }
                return operationStatus;
            }

            // the measure of completion and success is returning "SUCCESS"
            // all other outcomes indicate failure and pipe the failure
            // reason given from the device to the error message box
            @Override
            protected void succeeded() {
                super.succeeded();
                if (getValue().equals("SUCCESS")) {
                    setData(data);
                } else {
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
                }
            }

            @Override
            protected void scheduled() {
                super.scheduled();
            }

            @Override
            protected void failed() {
                super.failed();
                MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
            }

            @Override
            protected void cancelled() {
                super.failed();
                MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
            }
        };

        // Set up the bindings to control the UI elements during the running of the task
        if (progressBarResolution > 0D) {
            downProg.progressProperty().bind(task.progressProperty());
            downProg.visibleProperty().bind(task.runningProperty());
        }

        // Disabling Button control
        downloadButton.disableProperty().bind(task.runningProperty());
        progressIndicator.visibleProperty().bind(task.runningProperty());
        button3.disableProperty().bind(Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
        button1.disableProperty().bind(Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
        //Last measurement taken large display
        meterVBox.visibleProperty().bind(Bindings.when(task.valueProperty().isEqualTo("SUCCESS")).then(true).otherwise(false));
        new Thread(task).start();
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
        if (measurementContext != null) {
            return DISPLAYNAME + " (" + measurementContext+")";
        } else {
            return DISPLAYNAME;
        }    }

}
