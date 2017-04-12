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
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.BloodPressure;
import org.medipi.devices.Guide;
import org.medipi.devices.drivers.domain.DeviceTimestampChecker;
import org.medipi.logging.MediPiLogger;
import org.medipi.devices.drivers.domain.DeviceTimestampUpdateInterface;

/**
 * A concrete implementation of a specific device - Omron708BT Blood Pressure
 * meter
 *
 * The class uses a python script using the Continua HDP protocol to retrieve
 * the data via the stdout of the script.
 *
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 * In the event that the data is found to be outside the timestamp checker's
 * threshold then the class also is able to guide the user to update the
 * device's internal clock
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public class Omron708BT extends BloodPressure implements DeviceTimestampUpdateInterface {

    private static final String MAKE = "Omron";
    private static final String MODEL = "708-BT";
    private static final String DISPLAYNAME = "Omron 708-BT Blood Pressure Meter";
    private static final String STARTBUTTONTEXT = "Start";
    // The number of increments of the progress bar - a value of 0 removes the progBar
    private static final Double PROGBARRESOLUTION = 60D;
    private ImageView stopImg;
    private String deviceNamespace;
    private Task<String> task = null;
    private DeviceTimestampChecker deviceTimestampChecker;
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
        //find the python script location
        deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        pythonScript = medipi.getProperties().getProperty(deviceNamespace + ".python");
        if (pythonScript == null || pythonScript.trim().length() == 0) {
            String error = "Cannot find python script for driver for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".python";
            MediPiLogger.getInstance().log(Omron708BT.class.getName(), error);
            return error;
        }

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
        format.add("DATE");
        format.add("INTEGER");
        format.add("INTEGER");
        format.add("INTEGER");
        format.add("INTEGER");
        units.add("NONE");
        units.add("mmHg");
        units.add("mmHg");
        units.add("BPM");
        units.add("NONE");
        //Initialise the timestamp checker
        deviceTimestampChecker = new DeviceTimestampChecker(medipi, Omron708BT.this);
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
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
            });

        }
    }

    protected void processData() {
        task = new Task<String>() {
            ArrayList<ArrayList<String>> data = new ArrayList<>();

            @Override
            protected String call() throws Exception {
                try {
                    setButton2Name("Stop", stopImg);
                    // input datastream from the device driver
                    BufferedReader stdInput = callHDPPython();
                    if (stdInput != null) {
                        String readData = new String();
                        while ((readData = stdInput.readLine()) != null) {
                            System.out.println(readData);
                            //Digest the incoming read data
                            if (readData.startsWith("START")) {
                                setB2Label("Press Upload");
                                updateProgress(0D, progressBarResolution);
                            } else if (readData.startsWith("METADATA")) {
                                metadata.add(readData.substring(readData.indexOf(":") + 1));
                            } else if (readData.startsWith("DATA")) {
                                setB2Label("Downloading");
                                String dataStream = readData.substring(readData.indexOf(":") + 1);
                                String[] line = dataStream.split(Pattern.quote(separator));
                                // add the data to the data array
                                final ArrayList<String> values = new ArrayList<>();
                                // compensate for the date/time change
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                                LocalDateTime localtDateAndTime = LocalDateTime.parse(line[0], formatter);
                                ZonedDateTime zDate = ZonedDateTime.of(localtDateAndTime, ZoneId.systemDefault());
                                values.add(zDate.toInstant().toString());
                                values.add(line[1]);
                                values.add(line[2]);
                                values.add(line[3]);
                                values.add(line[4]);
                                data.add(values);
                            } else if (readData.startsWith("END")) {
                                updateProgress(progressBarResolution, progressBarResolution);
                                // Check to see if the time is within the definable threshold
                                return "SUCCESS";
                            } else {
                                return readData;
                            }
                        }
                    }

                } catch (IOException | NumberFormatException ex) {
                    return ex.getLocalizedMessage();
                }
                return "Unknown error connecting to Meter";
            }

            // the measure of completion and success is returning "SUCCESS"
            // all other outcomes indicate failure and pipe the failure
            // reason given from the device to the error message box
            @Override
            protected void succeeded() {
                super.succeeded();
                setButton2Name(STARTBUTTONTEXT, graphic);
                setB2Label(null);
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
                setButton2Name(STARTBUTTONTEXT, graphic);
                setB2Label(null);
                MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
            }

            @Override
            protected void cancelled() {
                super.failed();
                setButton2Name(STARTBUTTONTEXT, graphic);
                setB2Label(null);
            }

        };        // Set up the bindings to control the UI elements during the running of the task
        if (progressBarResolution > 0D) {
            downProg.progressProperty().bind(task.progressProperty());
            downProg.visibleProperty().bind(task.runningProperty());
        }

        progressIndicator.visibleProperty().bind(task.runningProperty());
        button3.disableProperty().bind(Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
        button1.disableProperty().bind(Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
        //Last measurement taken large display
//        meterVBox.visibleProperty().bind(Bindings.when(task.valueProperty().isEqualTo("SUCCESS")).then(true).otherwise(false));
        new Thread(task).start();
    }

    public BufferedReader callHDPPython() {
        try {
            System.out.println(pythonScript);
            String[] callAndArgs = {"python", pythonScript};
            process = Runtime.getRuntime().exec(callAndArgs);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return stdInput;
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Download of data unsuccessful", ex);
            return null;
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
        if (measurementContext != null) {
            return DISPLAYNAME + " (" + measurementContext + ")";
        } else {
            return DISPLAYNAME;
        }
    }

    @Override
    public Node getDeviceTimestampUpdateMessageBoxContent() {
        try {
            Guide guide = new Guide(deviceNamespace + ".timeset");
            return guide.getGuide();
        } catch (Exception ex) {
            return new Label("Cant find the appropriate action for setting the timestamp for " + deviceNamespace);
        }

    }
}
