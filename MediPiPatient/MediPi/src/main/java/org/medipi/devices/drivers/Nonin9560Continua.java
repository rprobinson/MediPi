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
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.Element;
import org.medipi.devices.Guide;
import org.medipi.devices.Oximeter;
import org.medipi.devices.drivers.domain.ContinuaData;
import org.medipi.devices.drivers.domain.ContinuaManager;
import org.medipi.devices.drivers.domain.ContinuaMeasurement;
import org.medipi.devices.drivers.domain.ContinuaOximeter;
import org.medipi.devices.drivers.service.BluetoothPropertiesDO;
import org.medipi.devices.drivers.service.BluetoothPropertiesService;
import org.medipi.devices.drivers.domain.DeviceModeUpdateInterface;

/**
 * A concrete implementation of a specific device - Nonin 9560 PulseOx Pulse
 * Oximeter
 *
 * The class uses the Continua Manager class which uses Antidote IEEE 11073 Library to retrieve
 * the data via the stdout of the script.
 *
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 * The class also is able to update the mode of operation of the pulse oximeter when being setup
 *
 * @author rick@robinsonhq.com
 */
//@SuppressWarnings("restriction")
public class Nonin9560Continua extends Oximeter implements DeviceModeUpdateInterface {

    private static final String MAKE = "Nonin";
    private static final String MODEL = "9560";
    private static final String DISPLAYNAME = "Nonin 9560 Finger Pulse Oximeter";
    private static final String STARTBUTTONTEXT = "Start";
    private static final String SEARCHING_MESSAGE = "Insert Finger";
    private static final String CONNECTING_MESSAGE = "Connecting";
    private static final String DOWNLOADING_MESSAGE = "Downloading";
    private ImageView graphic;
    private ImageView stopImg;
    private String deviceNamespace;
    private BluetoothPropertiesService bluetoothPropertiesService;
    private Task<String> task = null;
    private Task<String> modeTask = null;

    private Process process = null;

    /**
     * Constructor for BeurerBF480
     */
    public Nonin9560Continua() {
    }

    // initialise and load the configuration data
    @Override
    public String init() throws Exception {
        //find the python script location
        deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        stopImg = medipi.utils.getImageView("medipi.images.no", 20, 20);
        graphic = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        graphic.setRotate(90);
        initialGraphic = graphic;
        initialButtonText = STARTBUTTONTEXT;
// define the data to be collected
        columns.add("iso8601time");
        columns.add("pulse");
        columns.add("spo2");
        format.add("DATE");
        format.add("INTEGER");
        format.add("INTEGER");
        units.add("NONE");
        units.add("BPM");
        units.add("%");
        bluetoothPropertiesService = BluetoothPropertiesService.getInstance();
        bluetoothPropertiesService.register(Nonin9560Continua.this);

        return super.init();

    }

    /**
     * method to get the Make of the device
     *
     * @return make and model of device
     */
    @Override
    public String getMake() {
        return MAKE;
    }

    /**
     * method to get the Make and Model of the device
     *
     * @return make and model of device
     */
    @Override
    public String getModel() {
        return MODEL;
    }

    /**
     * method to get the Make and Model of the device
     *
     * @return make and model of device
     */
    @Override
    public String getSpecificDeviceDisplayName() {
        return DISPLAYNAME;
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
        task = new Task<String>() {
            ArrayList<ArrayList<String>> data = new ArrayList<>();
            ContinuaManager continuaManager = null;
            MeasurementTimerTask mtt = null;

            @Override
            protected String call() throws Exception {
                try {
                    continuaManager = ContinuaManager.getInstance();
                    continuaManager.reset();
                    setButton2Name("Stop", stopImg);
                    // input datastream from the device driver
                    BufferedReader stdInput = continuaManager.callIEEE11073Agent("0x1004");
                    if (stdInput != null) {
                        String readData = new String();
                        while ((readData = stdInput.readLine()) != null) {
                            System.out.println(readData);

                            if (continuaManager.parse(readData)) {
                                if (mtt != null) {
                                    mtt.stopTimer();
                                }
                                ContinuaData cd = continuaManager.getData();
                                if (!cd.getManufacturer().equals("Nonin Medical, Inc.")) {
                                    return "Expected device manufacturer is Nonin Medical, Inc. but returned device manufacturer is: " + cd.getManufacturer();
                                }
                                if (!cd.getModel().equals("Model 9560")) {
                                    return "Expected device model is Model 9560 but returned device model is: " + cd.getModel();
                                }
                                int setId = 0;
                                while (cd.getDataSetCounter() >= setId) {
                                    String spO2 = null;
                                    String heartRate = null;
                                    String time = null;
                                    for (ContinuaMeasurement cm : cd.getMeasurements()) {
                                        if (cm.getMeasurementSetID() == setId) {
                                            if (cm.getReportedIdentifier() == ContinuaOximeter.PERCENT) {
                                                spO2 = cm.getDataValue()[0];
                                                time = cm.getTime();
                                            } else if (cm.getReportedIdentifier() == ContinuaOximeter.BPM) {
                                                heartRate = cm.getDataValue()[0];
                                                time = cm.getTime();
                                            }

                                        }
                                        if (spO2 != null && heartRate != null && time != null) {
                                            data.add(new ArrayList<>(Arrays.asList(time, heartRate, spO2)));
                                            setId++;
                                            break;
                                        }
                                    }
                                }
                                if (!data.isEmpty()) {
                                    return "SUCCESS";
                                } else {
                                    return getSpecificDeviceDisplayName()+" has no data to download - please try again, referring to your clinician's advice on how best to take the measurements";
                                }

                            }
                            switch (continuaManager.getStatus()) {
                                case WAITING:
                                    setB2Label("Please Insert Finger");
                                    break;
                                case ATTRIBUTES:
                                    setB2Label("Connecting...");
                                    break;
                                case CONFIGURATION:
                                    setB2Label("Reading Device");
                                    break;
                                case MEASUREMENT:
                                    if (mtt != null && mtt.isRunning()) {
                                        mtt.reset();
                                    } else {
                                        mtt = new MeasurementTimerTask();
                                        Timer timer = new Timer();
                                        timer.schedule(mtt, 0, 1000);
                                    }

                                    break;
                                default:
                                    break;
                            }

                        }
                    }

                } catch (Exception ex) {
                    return ex.getLocalizedMessage();
                }
                return getSpecificDeviceDisplayName() + " - Unknown error connecting to Meter";

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

        progressIndicator.visibleProperty().bind(task.runningProperty());
        button3.disableProperty().bind(Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
        button1.disableProperty().bind(Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
        new Thread(task).start();
    }

    private Task<String> callUpdateTask(Button syncButton, Label status, String mode) {
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                StreamConnection conn = null;
                try {
                    Platform.runLater(() -> syncButton.setText("Stop"));
                    LocalDevice localDevice = LocalDevice.getLocalDevice();
                    System.out.println("Address: " + localDevice.getBluetoothAddress());
                    System.out.println("Name: " + localDevice.getFriendlyName());
                    boolean foundIt = false;
                    for (Element list : bluetoothPropertiesService.getRegisteredElements()) {
                        if (list.getClassTokenName().equals(Nonin9560Continua.this.getClassTokenName())) {
                            foundIt = true;
                        }
                    }
                    if (!foundIt) {
                        throw new Exception("Can't find the device in the configuration");
                    }
                    BluetoothPropertiesDO bpdo = bluetoothPropertiesService.getBluetoothPropertyDOByMedipiDeviceName(Nonin9560Continua.this.getClassTokenName());

                    // UUID uuid = new UUID(Integer.valueOf(bpdo.getBtProtocolId()));
                    //Create the servicve url
                    String connectionString = bpdo.getUrl();

                    //open server url
                    //Wait for client connection
                    System.out.println("\nServer Started. Waiting for clients to connect…");

                    try {

                        conn = (StreamConnection) Connector.open(connectionString);

                        InputStream is = conn.openInputStream();
                        OutputStream os = conn.openOutputStream();
                        int byteInt;
                        int counter = 0;
                        // First need to check that the device is in the correct data format - i.e. Data Format 2 - refer to nonin spec
                        if (mode.equals("MODE")) {
                            ensureCorrectDataFormat(os);
                        }
                        dataloop:
                        while ((byteInt = is.read()) != -1) {
                            System.out.print(byteInt + ",");
                            if (counter == 0 || counter % 22 == 0) {
                                switch (byteInt) {
                                    case 6:
                                        Platform.runLater(() -> status.setText("Successfully synchronised"));
                                        return "SUCCESS";
                                    case 15:
                                        Platform.runLater(() -> status.setText("Synchronisation Failed"));
                                        cancel();
                                        return "CANCEL";
                                    default:
                                        break;
                                }
                            }
                            counter++;
                        }
                    } catch (IOException e) {
                        Platform.runLater(() -> status.setText("Failed: follow the guide & retry"));
                        System.out.println("attempt connection: " + Instant.now() + e.getMessage());
                        return "FAIL";
                    }

                } catch (Exception e) {
                    MediPiMessageBox.getInstance().makeErrorMessage("", e);
                } finally {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (IOException ex) {
                            //do nothing as if the connection is null it doesnt need closing
                        }
                    }
                }

                Platform.runLater(() -> status.setText("Failed to synchronise"));
                return "Unknown error connecting to Meter";
            }

            // the measure of completion and success is returning "SUCCESS"
            // all other outcomes indicate failure and pipe the failure
            // reason given from the device to the error message box
            @Override
            protected void succeeded() {
                super.succeeded();
                if (mode.equals("MODE")) {
                    syncButton.setText("Synchronise Mode");
                }
            }

            @Override
            protected void scheduled() {
                super.scheduled();
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> status.setText("Failed to synchronise"));
                if (mode.equals("MODE")) {
                    syncButton.setText("Synchronise Mode");
                }
            }

            @Override
            protected void cancelled() {
                if (mode.equals("MODE")) {
                    syncButton.setText("Synchronise Mode");
                }
            }

        };
        return task;
    }

    //This methos sets the device to be in data format 13 without ATR (attempt to reconnect) which is required for best operation
    private void ensureCorrectDataFormat(OutputStream os) throws IOException {
        byte[] dataFormat = new byte[]{
            (byte) 0x02, //STX
            (byte) 0x70, //Op Code
            (byte) 0x04, //Data Size
            (byte) 0x02, //Data Type
            (byte) 0x0D, //Data Format
            (byte) 0x00, //Options
            (byte) 0x83, //CheckSum
            (byte) 0x03 //ETX
        };
        os.write(dataFormat);
    }

    @Override
    public Node getDeviceModeUpateMessageBoxContent() {
        Guide guide = null;
        try {
            guide = new Guide(deviceNamespace + ".modeset");
        } catch (Exception ex) {
            return new Label("Cant find the appropriate action for setting the mode for " + deviceNamespace);
        }
        ProgressIndicator pi = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        pi.setMinSize(20, 20);
        pi.setMaxSize(20, 20);
        pi.setVisible(false);
        VBox timeSyncVbox = new VBox();
        HBox buttonHbox = new HBox();
        Label status = new Label("Unsynchronised");
        status.setId("guide-text");
        Button syncButton = new Button("Update Mode");
        syncButton.setId("button-record");
        buttonHbox.setPadding(new Insets(10, 10, 10, 10));
        buttonHbox.setSpacing(10);
        buttonHbox.getChildren().addAll(
                syncButton,
                status,
                pi
        );
        timeSyncVbox.getChildren().addAll(
                guide.getGuide(),
                buttonHbox
        );
        syncButton.setOnAction((ActionEvent t) -> {
            if (modeTask == null || !modeTask.isRunning()) {
                status.setText("Attempting to connect...");
                modeTask = callUpdateTask(syncButton, status, "MODE");
                // Set up the bindings to control the UI elements during the running of the task
                pi.visibleProperty().bind(modeTask.runningProperty());
                new Thread(modeTask).start();
            } else {
                Platform.runLater(() -> {
                    modeTask.cancel();
                    status.setText("Synchronisation Cancelled");
                });

            }
        });
        return timeSyncVbox;
    }

    private class MeasurementTimerTask extends TimerTask {

        private boolean isRunning = true;
        int second = 15;

        @Override
        public void run() {
            this.isRunning = true;
            setB2Label("Downloading " + second-- + "s");
            if (second <= 0) {
                setB2Label("Downloaded");
                this.isRunning = false;
                this.cancel();
            }
        }

        public boolean isRunning() {
            return this.isRunning;
        }

        public void stopTimer() {
            setB2Label("");
            this.isRunning = false;
            this.cancel();
        }

        public void reset() {
            second = 15;
        }
    }
}
