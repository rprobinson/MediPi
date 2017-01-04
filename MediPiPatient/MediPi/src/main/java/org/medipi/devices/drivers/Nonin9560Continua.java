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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import org.medipi.devices.drivers.domain.TimestampValidationInterface;
import org.medipi.devices.drivers.service.BluetoothPropertiesDO;
import org.medipi.devices.drivers.service.BluetoothPropertiesService;
import org.medipi.logging.MediPiLogger;

/**
 * A concrete implementation of a specific device - Nonin 9560 PulseOx Pulse
 * Oximeter
 *
 * The class uses a python script using the Continua HDP protocol to retrieve
 * the data via the stdout of the script.
 *
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 * In the event that the data is found to be outside the timestamp checker's
 * threshold then the class also is able to guide the user to resynchronise the
 * device
 *
 * @author rick@robinsonhq.com
 */
//@SuppressWarnings("restriction")
public class Nonin9560Continua extends Oximeter implements TimestampValidationInterface {

    private static final String MAKE = "Nonin";
    private static final String MODEL = "9560";
    private static final String DISPLAYNAME = "Nonin 9560 Finger Pulse Oximeter";
    private static final String STARTBUTTONTEXT = "Start";
    private static final String SEARCHING_MESSAGE = "Insert Finger";
    private static final String CONNECTING_MESSAGE = "Connecting";
    private static final String DOWNLOADING_MESSAGE = "Downloading data";
    private ImageView graphic;
    private String pythonScript;
    private String deviceNamespace;
    private BluetoothPropertiesService bluetoothPropertiesService;

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
        pythonScript = medipi.getProperties().getProperty(deviceNamespace + ".python");
        if (pythonScript == null || pythonScript.trim().length() == 0) {
            String error = "Cannot find python script for driver for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".python";
            MediPiLogger.getInstance().log(Nonin9560Continua.class.getName(), error);
            return error;
        }
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
    public String getDisplayName() {
        return DISPLAYNAME;
    }

    /**
     * Method to to download the data from the device. This data is digested by
     * the generic device class
     */
    @Override
    protected void downloadData() {
        resetDevice();
        Task<String> task = new Task<String>() {
            ArrayList<ArrayList<String>> data = new ArrayList<>();

            @Override
            protected String call() throws Exception {
                try {
                    // input datastream from the device driver
                    BufferedReader stdInput = callHDPPython();
                    if (stdInput != null) {
                        String readData = new String();
                        while ((readData = stdInput.readLine()) != null) {
                            if (medipi.getDebugMode() == MediPi.DEBUG) {
                                System.out.println(readData);
                            }
                            //Digest the incoming read data
                            if (readData.startsWith("START")) {
                                setButton2Name(SEARCHING_MESSAGE);
                            } else if (readData.startsWith("METADATA")) {
                                metadata.add(readData.substring(readData.indexOf(":") + 1));
                                // the LOOP function allows devices to control a progress bar
                            } else if (readData.startsWith("DATA")) {
                                setButton2Name(DOWNLOADING_MESSAGE);
                                String dataStream = readData.substring(readData.indexOf(":") + 1);
                                String[] line = dataStream.split(Pattern.quote(separator));
//                                // add the data to the data array
                                final ArrayList<String> values = new ArrayList<>();
                                values.add(line[0]);
                                values.add(line[1]);
                                values.add(line[2]);
                                data.add(values);
                            } else if (readData.startsWith("END")) {
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
                if (getValue().equals("SUCCESS")) {
                    setData(data);
                } else {
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
                }
            }

            @Override
            protected void scheduled() {
                super.scheduled();
                setButton2Name(STARTBUTTONTEXT, graphic);
            }

            @Override
            protected void failed() {
                super.failed();
                setButton2Name(STARTBUTTONTEXT, graphic);
                MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
            }

            @Override
            protected void cancelled() {
                super.failed();
                setButton2Name(STARTBUTTONTEXT, graphic);
                MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
            }
        };        // Set up the bindings to control the UI elements during the running of the task

        // Disabling Button control
        actionButton.disableProperty().bind(task.runningProperty());
        progressIndicator.visibleProperty().bind(task.runningProperty());
        button3.disableProperty().bind(Bindings.when(task.runningProperty().and(isSchedule)).then(true).otherwise(false));
        new Thread(task).start();
    }

    public BufferedReader callHDPPython() {
        try {
            if (medipi.getDebugMode() == MediPi.DEBUG) {
                System.out.println(pythonScript);
            }
            String[] callAndArgs = {"python", pythonScript};
            Process p = Runtime.getRuntime().exec(callAndArgs);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return stdInput;
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Download of data unsuccessful", ex);
            return null;
        }

    }

    @Override
    public Node getTimestampMessageBoxContent() {
        Guide guide = null;
        try {
            guide = new Guide(deviceNamespace + ".timeset");
        } catch (Exception ex) {
            return new Label("Cant find the appropriate action for setting the timestamp for " + deviceNamespace);
        }

        VBox timeSyncVbox = new VBox();
        HBox buttonHbox = new HBox();
        Label status = new Label("Unsynchronised");
        status.setId("resultstext");
        Button syncButton = new Button("Synchronise Bluetooth Device");
        syncButton.setId("button-record");
        buttonHbox.setPadding(new Insets(10, 10, 10, 10));
        buttonHbox.setSpacing(10);
        buttonHbox.getChildren().addAll(
                syncButton,
                status,
                progressIndicator
        );
        timeSyncVbox.getChildren().addAll(
                guide.getGuide(),
                buttonHbox
        );
        progressIndicator.visibleProperty().bind(syncButton.disableProperty());
        syncButton.setOnAction((ActionEvent t) -> {
            // Request focus on the username field by default.
            syncButton.setDisable(true);
            status.setText("Finding device");
            Task<String> task = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    StreamConnection conn = null;
                    try {
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

                        Platform.runLater(() -> status.setText("Trying to connect"));
                        try {
                            conn = (StreamConnection) Connector.open(connectionString);
                            System.out.println("Connected:" + connectionString + "\n");
                            InputStream is = conn.openInputStream();
                            OutputStream os = conn.openOutputStream();

                            ensureCorrectTime(os);

                            byte b;
                            int counter = 0;
                            final byte ackFormat = (byte) 0x06;
                            final byte nackFormat = (byte) 0x15;

                            while ((b = (byte) is.read()) != -1) {
                                if (counter == 0 && b == ackFormat) {
                                    System.out.println("ACK");
                                    return "SUCCESS";
                                } else if (counter == 0 && b == nackFormat) {
                                    System.out.println("NACK");
                                    return "FAIL";
                                }
                                if (counter == 21) {
                                    counter = 0;
                                } else {
                                    counter++;
                                }

                            }
                        } catch (IOException e) {

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

                    return "Unknown error connecting to Meter";
                }

                // the measure of completion and success is returning "SUCCESS"
                // all other outcomes indicate failure and pipe the failure
                // reason given from the device to the error message box
                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (getValue().equals("SUCCESS")) {
                        Platform.runLater(() -> status.setText("Sucessfully synchronised"));
                    } else {
                        Platform.runLater(() -> status.setText("failed to synchronise"));
                        syncButton.setDisable(false);
                    }
                }

                @Override
                protected void scheduled() {
                    super.scheduled();
                }

                @Override
                protected void failed() {
                    super.failed();
                    Platform.runLater(() -> status.setText("failed to synchronise"));
                    syncButton.setDisable(false);
                }

                @Override
                protected void cancelled() {
                    super.failed();
                }

            };        // Set up the bindings to control the UI elements during the running of the task
            progressIndicator.visibleProperty().bind(task.runningProperty());
            new Thread(task).start();
        });
        return timeSyncVbox;
    }

    private void ensureCorrectTime(OutputStream os) throws IOException {
        Instant i = Instant.now();
        LocalDateTime ldt = LocalDateTime.ofInstant(i, ZoneId.of("UTC"));
        int hexYY = formatDateElements(ldt.getYear() - 2000);
        int hexMM = formatDateElements(ldt.getMonthValue());
        int hexdd = formatDateElements(ldt.getDayOfMonth());
        int hexhh = formatDateElements(ldt.getHour());
        int hexmm = formatDateElements(ldt.getMinute());
        int hexss = formatDateElements(ldt.getSecond());
        byte[] setdatetime = new byte[]{
            (byte) 0x02,
            (byte) 0x72,
            (byte) 0x06,
            (byte) hexYY, //YY
            (byte) hexMM, //MM
            (byte) hexdd, //dd
            (byte) hexhh, //hh
            (byte) hexmm, //mm
            (byte) hexss, //ss
            (byte) 0x03
        };
        os.write(setdatetime);
    }

    private int formatDateElements(int i) {
        return Integer.parseInt(Integer.toHexString(i), 16);
    }

}
