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

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import org.medipi.MediPiMessageBox;
import org.medipi.devices.BloodPressure;
import org.medipi.devices.drivers.domain.ContinuaBloodPressure;
import org.medipi.devices.drivers.domain.ContinuaData;
import org.medipi.devices.drivers.domain.ContinuaManager;
import org.medipi.devices.drivers.domain.ContinuaMeasurement;

/**
 * A concrete implementation of a specific device - Omron708BT Blood Pressure
 * meter
 *
 * The class uses the Continua Manager class which uses Antidote IEEE 11073 Library to retrieve
 * the data via the stdout of the script.
 *
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public class Omron708BT extends BloodPressure {

    private static final String MAKE = "Omron";
    private static final String MODEL = "708-BT";
    private static final String DISPLAYNAME = "Omron 708-BT Blood Pressure Meter";
    private static final String STARTBUTTONTEXT = "Start";
    // The number of increments of the progress bar - a value of 0 removes the progBar
    private static final Double PROGBARRESOLUTION = 60D;
    private ImageView stopImg;
    private Task<String> task = null;
    private Process process = null;

    String pythonScript;

    private ImageView graphic;

    /**
     * Constructor for BeurerBM55
     */
    public Omron708BT() {
    }

    // initialise and load the configuration data
    @Override
    public String init() throws Exception {

        progressBarResolution = PROGBARRESOLUTION;
        stopImg = medipi.utils.getImageView("medipi.images.no", 20, 20);
        graphic = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        graphic.setRotate(90);
        initialGraphic = graphic;
        initialButtonText = STARTBUTTONTEXT;
// define the data to be collected
        columns.add("iso8601time");
        columns.add("systol");
        columns.add("diastol");
        columns.add("pulserate");
        columns.add("MAP");
        columns.add("irregularHeartBeat");
        format.add("DATE");
        format.add("INTEGER");
        format.add("INTEGER");
        format.add("INTEGER");
        format.add("INTEGER");
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
    public void downloadData(VBox v) {
        if (task == null || !task.isRunning()) {
            resetDevice();
            processData();
        } else {
            Platform.runLater(() -> {
                task.cancel();
            });

        }
    }

    protected void processData() {
        task = new Task<String>() {
            ArrayList<ArrayList<String>> data = new ArrayList<>();
            ContinuaManager continuaManager = null;

            @Override
            protected String call() throws Exception {
                try {
                    continuaManager = ContinuaManager.getInstance();
                    continuaManager.reset();
                    setButton2Name("Stop", stopImg);
                    // input datastream from the device driver
                    BufferedReader stdInput = continuaManager.callIEEE11073Agent("0x1007");
                    if (stdInput != null) {
                        String readData = new String();
                        while ((readData = stdInput.readLine()) != null) {
                            System.out.println(readData);

                            if (continuaManager.parse(readData)) {
                                ContinuaData cd = continuaManager.getData();
                                if (!cd.getManufacturer().equals("OMRON HEALTHCARE")) {
                                    return "Expected device manufacturer is OMRON HEALTHCARE but returned device manufacturer is: "+cd.getManufacturer();
                                }
                                if (!cd.getModel().equals("HEM-7081-IT")) {
                                    return "Expected device model is HEM-7081-IT but returned device model is: "+cd.getModel();
                                }

                                int setId = 0;
                                while (cd.getDataSetCounter() >= setId) {
                                    String sys = null;
                                    String dia = null;
                                    String mean = null;
                                    String heartRate = null;
                                    String irregular = null;
                                    String time = null;
                                    for (ContinuaMeasurement cm : cd.getMeasurements()) {
                                        if (cm.getMeasurementSetID() == setId) {
                                            if (cm.getReportedIdentifier() == ContinuaBloodPressure.MMHG) {
                                                sys = cm.getDataValue()[0];
                                                dia = cm.getDataValue()[1];
                                                mean = cm.getDataValue()[2];
                                                time = cm.getTime();
                                            } else if (cm.getReportedIdentifier() == ContinuaBloodPressure.BPM) {
                                                heartRate = cm.getDataValue()[0];
                                                time = cm.getTime();
                                            } else if (cm.getReportedIdentifier() == ContinuaBloodPressure.DIMENTIONLESS) {
                                                irregular = cm.getDataValue()[0];
                                                time = cm.getTime();
                                            }

                                        }
                                        if (sys != null && dia != null && mean != null && heartRate != null && irregular != null && time != null) {
                                            data.add(new ArrayList<>(Arrays.asList(time, sys, dia, heartRate, mean, irregular)));
                                            setId++;
                                            break;
                                        }
                                    }
                                }
                                if (!data.isEmpty()) {
                                    return "SUCCESS";
                                } else {
                                    return "There are no measurements available on the Blood Pressure device for download - please retake your blood pressure and try again";
                                }

                            }
                            switch (continuaManager.getStatus()) {
                                case WAITING:
                                    setB2Label("Press Upload");
                                    break;
                                case ATTRIBUTES:
                                    setB2Label("Connecting...");
                                    break;
                                case CONFIGURATION:
                                    setB2Label("Reading Device");
                                    break;
                                case MEASUREMENT:
                                    setB2Label("Downloading");

                                    break;
                                default:
                                    break;
                            }

                        }
                    }

                } catch (Exception ex) {
                    return ex.getLocalizedMessage();
                }
                return getSpecificDeviceDisplayName()+" - Unknown error connecting to Meter";
            }

            // the measure of completion and success is returning "SUCCESS"
            // all other outcomes indicate failure and pipe the failure
            // reason given from the device to the error message box
            @Override
            protected void succeeded() {
                super.succeeded();
                setButton2Name(STARTBUTTONTEXT, graphic);
                setB2Label(null);
                if (getValue() == null) {
                    MediPiMessageBox.getInstance().makeErrorMessage("Failed to get Data from Device", null);
                } else if (getValue().equals("SUCCESS")) {
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
                setButton2Name(STARTBUTTONTEXT, graphic);
                setB2Label(null);
                if (getValue() == null) {
                    MediPiMessageBox.getInstance().makeErrorMessage("Failed to get Data from Device", null);
                } else {
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
                }
            }

            @Override
            protected void cancelled() {
                super.failed();
                setButton2Name(STARTBUTTONTEXT, graphic);
                setB2Label(null);
                continuaManager.stopIEEE11073Agent();
            }

        };        // Set up the bindings to control the UI elements during the running of the task
        if (progressBarResolution
                > 0D) {
            downProg.progressProperty().bind(task.progressProperty());
            downProg.visibleProperty().bind(task.runningProperty());
        }

        progressIndicator.visibleProperty()
                .bind(task.runningProperty());
        button3.disableProperty()
                .bind(Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
        button1.disableProperty()
                .bind(Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
        //Last measurement taken large display
//        meterVBox.visibleProperty().bind(Bindings.when(task.valueProperty().isEqualTo("SUCCESS")).then(true).otherwise(false));
        new Thread(task)
                .start();
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
            return DISPLAYNAME + " (" + measurementContext + ")";
        } else {
            return DISPLAYNAME;
        }
    }

}
