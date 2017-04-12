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
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.Scale;
import org.medipi.devices.drivers.domain.M430Measurement;
import org.medipi.devices.drivers.service.BTStreamFixedLength;
import org.medipi.devices.drivers.service.BluetoothPropertiesDO;
import org.medipi.devices.drivers.service.BluetoothPropertiesService;

/**
 * A concrete implementation of a specific device - Marsden M430 scale
 *
 * The class uses Marsden's freely available/simple serial port protocol
 *
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public class MarsdenM430 extends Scale {

    private static final String MAKE = "Marsden";
    private static final String MODEL = "M-430";
    private static final String DISPLAYNAME = "Marsden M-430 Scales";
    private static final String STARTBUTTONTEXT = "Start";
    private static final String TURN_ON_SCALES = "Turn on the scales";
    private static final String NOW_STAND_ON_SCALES = "Now stand on scales";
    private static final String DOWNLOADING = "Downloading data";
    // The number of increments of the progress bar - a value of 0 removes the progBar
    private static final Double PROGBARRESOLUTION = 1D;
    private BluetoothPropertiesService bluetoothPropertiesService;
    private ImageView graphic;
    private ImageView stopImg;
    private String deviceNamespace;
    private Task<String> task = null;

    /**
     * Constructor for BeurerBF480
     */
    public MarsdenM430() {
    }

    // initialise and load the configuration data
    @Override
    public String init() throws Exception {
        deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        progressBarResolution = PROGBARRESOLUTION;
        stopImg = medipi.utils.getImageView("medipi.images.no", 20, 20);
        graphic = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        graphic.setRotate(90);
        initialGraphic = graphic;
        initialButtonText = STARTBUTTONTEXT;
        bluetoothPropertiesService = BluetoothPropertiesService.getInstance();
        bluetoothPropertiesService.register(MarsdenM430.this);
        return super.init();

    }

    @Override
    public void resetDevice() {
        columns.clear();
        format.clear();
        units.clear();
        super.resetDevice();
    }

    /**
     * Method to to download the data from the device. This data is digested by
     * the generic device class
     */
    @Override
    protected void downloadData() {
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
        try {
            task = new Task<String>() {
                BTStreamFixedLength bluetoothService = null;
                ArrayList<ArrayList<String>> data = new ArrayList<>();

                @Override
                protected String call() throws Exception {
                    setButton2Name("Stop", stopImg);
                    String operationStatus = "Unknown error connecting to Scale";
                    try {
                        // input datastream from the device driver
                        updateProgress(Double.parseDouble("0"), progressBarResolution);
                        bluetoothService = new BTStreamFixedLength(MarsdenM430.this);
                        bluetoothService.setSearchingMessage(TURN_ON_SCALES);
                        bluetoothService.setConnectionMessage(NOW_STAND_ON_SCALES);
                        bluetoothService.setDownloadingMessage(DOWNLOADING);
                        BluetoothPropertiesDO btp = bluetoothPropertiesService.getBluetoothPropertyDOByMedipiDeviceName(getClassTokenName());
                        //This is a blocking method until data arrives (or not)
                        byte[] result = bluetoothService.getFixedLengthStream(btp.getUrl(), 217);
                        if (result == null) {
                            return null;
                        } else if (result[0] == -1) {
                            operationStatus = "Could not find " + getSpecificDeviceDisplayName() + " scales. Are they switched on and in range? (within ~3 metres)";
                            return operationStatus;
                        }
                        M430Measurement measurement = new M430Measurement(deviceNamespace);
                        String error;
                        if ((error = measurement.parse(result)) == null) {
                            columns.addAll(measurement.getColumns());
                            format.addAll(measurement.getFormat());
                            units.addAll(measurement.getUnits());
                            data.add(measurement.getDeviceData());
                        } else {
                            operationStatus = error;
                            failed();
                        }

                        updateProgress(progressBarResolution, progressBarResolution);
                        operationStatus = "SUCCESS";

                    } catch (Exception ex) {
                        operationStatus = ex.getLocalizedMessage();
                        return operationStatus;
                    } finally {
                        setB2Label(null);
                    }
                    return operationStatus;
                }

                // the measure of completion and success is returning "SUCCESS"
                // all other outcomes indicate failure and pipe the failure
                // reason given from the device to the error message box
                @Override
                protected void succeeded() {
                    super.succeeded();
                    setButton2Name(STARTBUTTONTEXT, graphic);
                    setB2Label(null);
                    if (bluetoothService != null) {
                        bluetoothService.StopLookingForBTConnection();
                    }
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
                    setButton2Name(STARTBUTTONTEXT, graphic);
                    setB2Label(null);
                    if (bluetoothService != null) {
                        bluetoothService.StopLookingForBTConnection();
                    }
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
                }

                @Override
                protected void cancelled() {
                    super.failed();
                    setButton2Name(STARTBUTTONTEXT, graphic);
                    setB2Label(null);
                    if (bluetoothService != null) {
                        bluetoothService.StopLookingForBTConnection();
                    }
                }
            };

            // Set up the bindings to control the UI elements during the running of the task
            if (progressBarResolution > 0D) {
                downProg.progressProperty().bind(task.progressProperty());
                downProg.visibleProperty().bind(task.runningProperty());
            }
            progressIndicator.visibleProperty().bind(task.runningProperty());
            button3.disableProperty().bind(Bindings.when(task.runningProperty().and(this.isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
            button1.disableProperty().bind(Bindings.when(task.runningProperty().and(this.isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
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
