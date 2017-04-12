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
import java.util.Collections;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbPipe;

import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.Scale;
import org.medipi.devices.drivers.domain.BF480Measurement;
import org.medipi.devices.drivers.service.BF480USBService;
import org.medipi.devices.drivers.service.USBService;
import org.medipi.logging.MediPiLogger;
import org.medipi.utilities.BytesManipulator;

/**
 * A concrete implementation of a specific device - Beurer BF480 Diagnostic
 * Scale.
 *
 * This class retrieves the data from Beurer BF480 by communicating over USB.
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public class BeurerBF480 extends Scale {

    private static final String MAKE = "Beurer";
    private static final String MODEL = "BF-480";
    private static final String DISPLAYNAME = "Beurer BF-480 Scales";
    private static final String STARTBUTTONTEXT = "Start";
    // The number of increments of the progress bar - a value of 0 removes the progBar
    private static final Double PROGBARRESOLUTION = 64D;
    private String user;
    private final USBService usbService;

    private static final short VENDOR_ID = (short) 0x04d9;
    private static final short PRODUCT_ID = (short) 0x8010;
    private ImageView graphic;

    final byte requestType = 33;
    final byte request = 0x09;
    final short value = 521;
    final short index = 0;

    /**
     * Constructor for BeurerBF480
     */
    public BeurerBF480() {
        this.usbService = new BF480USBService();
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
        graphic = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        graphic.setRotate(90);
        initialGraphic = graphic;
        initialButtonText = STARTBUTTONTEXT;
// define the data to be collected
        columns.add("iso8601time");
        columns.add("weight");
        columns.add("bodyfat");
        columns.add("water");
        columns.add("muscle");
        format.add("DATE");
        format.add("DOUBLE");
        format.add("DOUBLE");
        format.add("DOUBLE");
        format.add("DOUBLE");
        units.add("NONE");
        units.add("kg");
        units.add("%");
        units.add("%");
        units.add("%");

        return super.init();

    }

    /**
     * Method to to download the data from the device. This data is digested by
     * the generic device class
     */
    @Override
    public void downloadData() {
        try {
            Task<String> task = new Task<String>() {
                ArrayList<ArrayList<String>> data = new ArrayList<>();

                @Override
                protected String call() throws Exception {
                    String operationStatus = "Unknown error connecting to Scale";
                    UsbPipe connectionPipe = null;
                    try {
                        // input datastream from the device driver
                        updateProgress(Double.parseDouble("0"), progressBarResolution);

                        int userNumber = Integer.valueOf(user);
                        int readingStartByteNumber = (userNumber - 1) * 6;
                        List<BF480Measurement> measurements = new ArrayList<>();
                        UsbDevice device = usbService.getUSBDevice(VENDOR_ID, PRODUCT_ID);

                        connectionPipe = usbService.getUSBConnection(device, 0, -127);
                        UsbControlIrp usbControl = usbService.getUSBControl(device, requestType, request, value, index);
                        connectionPipe.open();

                        usbService.initialiseDevice(device, usbControl, connectionPipe);

                        //Read 128 x 64 data from serial interface
                        byte[][] rawReadings = new byte[BF480USBService.MAX_NUMBER_OF_READINGS][BF480USBService.BYTE_ARRAY_LENGTH_128];
                        for (int readingsCounter = 0; readingsCounter < BF480USBService.MAX_NUMBER_OF_READINGS; readingsCounter++) {
                            rawReadings[readingsCounter] = usbService.readData(connectionPipe, 128);
                            this.updateProgress(readingsCounter, progressBarResolution);
                        }

                        //Convert 128 x 64 data to 64 x 64
                        int[][] readings = new int[BF480USBService.MAX_NUMBER_OF_READINGS][BF480USBService.BYTE_ARRAY_LENGTH_128 / 2];
                        for (int readingsCounter = 0; readingsCounter < BF480USBService.MAX_NUMBER_OF_READINGS; readingsCounter++) {
                            readings[readingsCounter] = BytesManipulator.convertBytesToIntegers(rawReadings[readingsCounter]);
                        }

                        //Transpose the data matrix to retrieve each patients information
                        int[][] userReadings = BytesManipulator.transpose(readings);

                        //convert all 64 readings to an object by iterating over rows of the matrix
                        for (int readingsCounter = 0; readingsCounter < BF480USBService.MAX_NUMBER_OF_READINGS; readingsCounter++) {
                            if (userReadings[readingsCounter][readingStartByteNumber + 4] == 0) {
                                break;
                            }
                            final BF480Measurement measurement = new BF480Measurement(userReadings[readingsCounter], readingStartByteNumber);
                            measurements.add(measurement);
                        }

                        //Sort the readings with the readings timestamp
                        Collections.sort(measurements);

                        for (BF480Measurement measurement : measurements) {
                            System.out.println("\n" + measurement);
                            // add the data to the data array
                            data.add(measurement.getAllValues());
                            // add the data to the screen display - this might be a graph/table
                            // or just a simple result of the last measure
                        }

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
                        // take the time of downloading the data
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
            button3.disableProperty().bind(Bindings.when(task.runningProperty().and(this.isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
            button1.disableProperty().bind(Bindings.when(task.runningProperty().and(this.isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
            //Last measurement taken large display
            weightHBox.visibleProperty().bind(Bindings.when(task.valueProperty().isEqualTo("SUCCESS")).then(true).otherwise(false));
            new Thread(task).start();
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Download of data unsuccessful", ex);
        }
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
}
