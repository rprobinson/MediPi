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
package org.medipi.devices;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.utilities.Utilities;

/**
 * Class to display and handle the functionality for a generic BloodPressure
 * Medical Device.
 *
 * Dependent on the view mode the UI displays either a graphical representation
 * of the results or a visual guide to using the device. When data is taken from
 * the device metadata is added to identify it. The last reading taken from the
 * device is displayed
 *
 * This class expects each line of data coming in from the driver to start with
 * the following in order:
 *
 * START, LOOP: n (where n is the loop number), DATA:x (where x is a line of
 * char separated data), END.All individual data points within a line are
 * separated using the configurable separator
 *
 * TODO: This class expect measurements systolic blood pressure, diastolic blood
 * pressure, pulseRate, user, At rest indicator and heart arrhythmia detection
 * which for any specific device might not be appropriate
 *
 * @author rick@robinsonhq.com
 */
public abstract class BloodPressure extends Device {

    private final String DEVICE_TYPE = "Blood Pressure";
    private static final String PROFILEID = "urn:nhs-en:profile:BloodPressure";
    private ArrayList<String[]> deviceData = new ArrayList<>();
    // property to indicate whether data has been recorded for this device
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);
    private VBox meterWindow;
    private final TableView<MeterReading> table = new TableView<>();
    private final ObservableList<MeterReading> data = FXCollections.observableArrayList();
    private VBox deviceWindow;
    private final ProgressBar downProg = new ProgressBar(0.0F);
    private Label lastSystolDB;
    private Label lastSystol;
    private Label lastDiastolDB;
    private Label lastDiastol;
    private Label lastPulseDB;
    private Label lastPulse;
    private Node dataBox;
    private Button downloadButton;
    // initially set to unix epoch time
    private Date lastDate = new Date(0L);

    private Date downloadTimestamp = null;

    /**
     * This is the data separator from the MediPi.properties file
     *
     */
    protected String separator;

    /**
     * This defines how many steps there are in the progress bar - 0 means that
     * no progress bar is shown
     */
    protected Double progressBarResolution = null;

    /**
     * Constructor for Generic Blood Pressure Meter
     *
     */
    public BloodPressure() {
    }

    /**
     * Initiation method called for this Element.
     *
     * Successful initiation of the this class results in a null return. Any
     * other response indicates a failure with the returned content being a
     * reason for the failure
     *
     * @return populated or null for whether the initiation was successful
     * @throws java.lang.Exception
     */
    @Override
    public String init() throws Exception {

        String uniqueDeviceName = getClassTokenName();
        separator = medipi.getDataSeparator();
        downloadButton = new Button("Download");
        downloadButton.setId("button-download");
        meterWindow = new VBox();
        meterWindow.setPadding(new Insets(0, 5, 0, 5));
        meterWindow.setSpacing(5);
        meterWindow.setMinSize(800, 350);
        meterWindow.setMaxSize(800, 350);
        downProg.setVisible(false);
        HBox buttonHbox = new HBox();
        buttonHbox.setAlignment(Pos.CENTER_RIGHT);
        buttonHbox.setSpacing(10);
        if (progressBarResolution > 0D) {
            buttonHbox.getChildren().add(downProg);
        }
        buttonHbox.getChildren().add(downloadButton);
        //Decide whether to show basic or advanced view
        if (medipi.isBasicDataView()) {
            Guide guide = new Guide(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName);
            dataBox = guide.getGuide();
        } else {
            //Setup the results table
            TableColumn dateCol = new TableColumn("Date/Time");
            dateCol.setMinWidth(50);
            dateCol.setCellValueFactory(
                    new PropertyValueFactory<>("date"));

            TableColumn systolCol = new TableColumn("Systolic Pressure");
            systolCol.setMinWidth(50);
            systolCol.setCellValueFactory(
                    new PropertyValueFactory<>("systol"));

            TableColumn diastolCol = new TableColumn("Diastolic Pressure");
            diastolCol.setMinWidth(50);
            diastolCol.setCellValueFactory(
                    new PropertyValueFactory<>("diastol"));

            TableColumn pulseRateCol = new TableColumn("Pulse Rate");
            pulseRateCol.setMinWidth(50);
            pulseRateCol.setCellValueFactory(
                    new PropertyValueFactory<>("pulseRate"));

            TableColumn userCol = new TableColumn("User");
            userCol.setMinWidth(50);
            userCol.setCellValueFactory(
                    new PropertyValueFactory<>("user"));

            TableColumn restCol = new TableColumn("Rest");
            restCol.setMinWidth(50);
            restCol.setCellValueFactory(
                    new PropertyValueFactory<>("rest"));

            TableColumn arrhythmiaCol = new TableColumn("Arrhythmia");
            arrhythmiaCol.setMinWidth(50);
            arrhythmiaCol.setCellValueFactory(
                    new PropertyValueFactory<>("arrhythmia"));

            table.setItems(data);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.getColumns().addAll(dateCol, systolCol, diastolCol, pulseRateCol, userCol, restCol, arrhythmiaCol);

            deviceWindow = new VBox();
            deviceWindow.setPadding(new Insets(0, 5, 0, 5));
            deviceWindow.setSpacing(5);
            deviceWindow.setAlignment(Pos.CENTER);
            deviceWindow.setMinWidth(600);
            deviceWindow.getChildren().addAll(
                    table
            );
            dataBox = deviceWindow;
        }
        //Set the initial values of the simple results
        lastSystol = new Label("--");
        lastSystolDB = new Label("");
        lastDiastol = new Label("--");
        lastDiastolDB = new Label("");
        lastPulse = new Label("--");
        lastPulseDB = new Label("");

        // create the large result box for the last measurement
        // downloaded from the device
        // Systolic reading
        HBox systolHbox = new HBox();
        systolHbox.setAlignment(Pos.CENTER);
        Label mmhg = new Label("mmHg");
        mmhg.setId("resultstext");
        mmhg.setStyle("-fx-font-size:10px;");
        systolHbox.getChildren().addAll(
                lastSystol,
                mmhg
        );
        // Diastolic reading
        HBox diastolHbox = new HBox();
        diastolHbox.setAlignment(Pos.CENTER);
        Label mmhg2 = new Label("mmHg");
        mmhg2.setId("resultstext");
        mmhg2.setStyle("-fx-font-size:10px;");
        diastolHbox.getChildren().addAll(
                lastDiastol,
                mmhg2
        );
        // Heart Rate reading
        HBox pulseHbox = new HBox();
        pulseHbox.setAlignment(Pos.CENTER);
        Label bpm = new Label("BPM");
        bpm.setId("resultstext");
        bpm.setStyle("-fx-font-size:10px;");
        pulseHbox.getChildren().addAll(
                lastPulse,
                bpm
        );
        VBox meterVBox = new VBox();
        meterVBox.setAlignment(Pos.CENTER);
        meterVBox.setId("resultsbox");
        meterVBox.setPrefWidth(200);
        meterVBox.getChildren().addAll(
                systolHbox,
                diastolHbox,
                pulseHbox
        );

        //create the main window HBox
        HBox dataHBox = new HBox();
        dataHBox.getChildren().addAll(
                dataBox,
                meterVBox
        );

        meterWindow.getChildren().addAll(
                dataHBox,
                new Separator(Orientation.HORIZONTAL)
        );
        // set main Element window
        window.setCenter(meterWindow);

        setRightButton(buttonHbox);

        downloadButton(meterVBox);

        // successful initiation of the this class results in a null return
        return null;
    }

    private void downloadButton(VBox meterVBox) {
        // Setup download button action to run in its own thread
        downloadButton.setOnAction((ActionEvent t) -> {
            resetDevice();
            Task<String> task = new Task<String>() {
                
                @Override
                protected String call() throws Exception {
                    try {
                        // input datastream from the device driver
                        BufferedReader stdInput = downloadData();
                        if (stdInput != null) {
                            String readData = new String();
                            while ((readData = stdInput.readLine()) != null) {
                                if (medipi.getDebugMode() == MediPi.DEBUG) {
                                    System.out.println(readData);
                                }
                                //Digest the incoming read data
                                if (readData.startsWith("START")) {
                                    updateProgress(0D, progressBarResolution);
                                    // the LOOP function allows devices to control a progress bar
                                } else if (readData.startsWith("LOOP")) {
                                    updateProgress(Double.parseDouble(readData.substring(readData.indexOf(":") + 1)), progressBarResolution);
                                } else if (readData.startsWith("DATA")) {
                                    String data = readData.substring(readData.indexOf(":") + 1);
                                    String[] line = data.split(Pattern.quote(separator));
                                    final Date d = Utilities.INTERNAL_DEVICE_FORMAT.parse(line[0] + ":" + line[1]);
                                    final int systol = Integer.parseInt(line[2]);
                                    final int diastol = Integer.parseInt(line[3]);
                                    final int pulse = Integer.parseInt(line[4]);
                                    final String user = line[5];
                                    final boolean rest = Boolean.parseBoolean(line[6]);
                                    final boolean arrhythmia = Boolean.parseBoolean(line[7]);
                                    // add the data to the data array
                                    deviceData.add(line);
                                    // add the data to the screen display - this might be a graph/table
                                    // or just a simple result of the last measure
                                    Platform.runLater(() -> {
                                        addDataPoint(d, systol, diastol, pulse, user, rest, arrhythmia);
                                    });
                                } else if (readData.startsWith("END")) {
                                    updateProgress(progressBarResolution, progressBarResolution);
                                    hasData.set(true);
                                    return "SUCCESS";
                                } else {
                                    return readData;
                                }
                            }
                        }

                    } catch (IOException | NumberFormatException | ParseException ex) {
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
                    if (getValue().equals("SUCCESS")) {
                        // take the time of downloading the data
                        downloadTimestamp = new Date();
                    } else {
                        MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null, Thread.currentThread());
                    }
                }
                
                @Override
                protected void scheduled() {
                    super.scheduled();
                }
                
                @Override
                protected void failed() {
                    super.failed();
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null, Thread.currentThread());
                }
                
                @Override
                protected void cancelled() {
                    super.failed();
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null, Thread.currentThread());
                }
            };
            
            // Set up the bindings to control the UI elements during the running of the task
            if (progressBarResolution > 0D) {
                downProg.progressProperty().bind(task.progressProperty());
                downProg.visibleProperty().bind(task.runningProperty());
            }
            
            // Disabling Button control
            downloadButton.disableProperty().bind(task.runningProperty());
            
            leftButton.disableProperty().bind(
                    Bindings.when(task.runningProperty().and(isSchedule))
                            .then(true)
                            .otherwise(false)
            );
            //Last measurement taken large display
            meterVBox.visibleProperty().bind(
                    Bindings.when(task.valueProperty().isEqualTo("SUCCESS"))
                            .then(true)
                            .otherwise(false)
            );
            
            new Thread(task).start();
        });
    }

    /**
     * method to get the generic Type of the device
     *
     * @return generic type of device e.g. Blood Pressure
     */
    @Override
    public String getType() {
        return DEVICE_TYPE;
    }

    @Override
    public String getProfileId() {
        return PROFILEID;
    }

    // initialises the device window and the data behind it
    @Override
    public void resetDevice() {
        deviceData = new ArrayList<>();
        hasData.set(false);
        downloadTimestamp = null;
        if (!medipi.isBasicDataView()) {
            data.clear();
        }
        lastSystol.setText("--");
        lastSystolDB.setText("");
        lastDiastol.setText("--");
        lastDiastolDB.setText("");
        lastPulse.setText("--");
        lastPulseDB.setText("");
        lastDate = new Date(0L);
    }

    /**
     * Method which returns a booleanProperty which UI elements can be bound to,
     * to discover whether there is data to be downloaded
     *
     * @return BooleanProperty signalling the presence of downloaded data
     */
    @Override
    public BooleanProperty hasDataProperty() {
        return hasData;
    }

    /**
     * Gets a csv representation of the data
     *
     * @return csv string of each value set of data points
     */
    @Override
    public String getData() {
        StringBuilder sb = new StringBuilder();
        //Add MetaData
        sb.append("metadata:medipiversion:").append(medipi.getVersion()).append("\n");
        sb.append("metadata:patientname:").append(medipi.getPatientLastName()).append(",").append(medipi.getPatientFirstName()).append("\n");
        sb.append("metadata:patientdob:").append(medipi.getPatientDOB()).append("\n");
        sb.append("metadata:patientnhsnumber:").append(medipi.getPatientNHSNumber()).append("\n");
        sb.append("metadata:timedownloaded:").append(downloadTimestamp).append("\n");
        sb.append("metadata:device:").append(getName()).append("\n");
        sb.append("metadata:format:").append("date,time,systol,diastol,pulseRate,user,rest,arrhythmia").append("\n");
        // Add Downloaded data
        for (String[] s : deviceData) {
            sb.append(s[0]);
            sb.append(separator);
            sb.append(s[1]);
            sb.append(separator);
            sb.append(s[2]);
            sb.append(separator);
            sb.append(s[3]);
            sb.append(separator);
            sb.append(s[4]);
            sb.append(separator);
            sb.append(s[5]);
            sb.append(separator);
            sb.append(s[6]);
            sb.append(separator);
            sb.append(s[7]);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * method to return the component to the dashboard
     *
     * @return @throws Exception
     */
    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this);
        dashComponent.addTitle(getType());
        dashComponent.addOverlay(lastSystolDB, "mmHg");
        dashComponent.addOverlay(lastDiastolDB, "mmHg");
        dashComponent.addOverlay(lastPulseDB, "BPM");
        return dashComponent.getTile();
    }

    /**
     * Add data to the graph
     *
     * Private method to add data to the internal structure and propogate it to
     * the UI
     *
     * @param d in UNIX epoch time format
     * @param systolic Blood Pressure in mm/Hg
     * @param diastolic Blood Pressure in mm/Hg
     * @param user as defined in the physical meter
     * @param At Rest indicator as measured in the meter
     * @param Heart Arrhythmia detection indicator as measured in the meter
     */
    private void addDataPoint(Date d, int systol, int diastol, int pulseRate, String user, boolean rest, boolean arrhythmia) {
        if (d.after(lastDate)) {
            lastSystol.setText(String.valueOf(systol));
            lastSystolDB.setText(String.valueOf(systol));
            lastDiastol.setText(String.valueOf(diastol));
            lastDiastolDB.setText(String.valueOf(diastol));
            lastPulse.setText(String.valueOf(pulseRate));
            lastPulseDB.setText(String.valueOf(pulseRate));
            lastDate = d;
        }
        if (!medipi.isBasicDataView()) {
            MeterReading mr = new MeterReading(Utilities.DISPLAY_TABLE_FORMAT.format(d), String.valueOf(systol), String.valueOf(diastol), String.valueOf(pulseRate), String.valueOf(user), String.valueOf(rest), String.valueOf(arrhythmia));
            data.add(mr);
        }

    }

    /**
     * Abstract method to download data from the device driver
     *
     * @return bufferedReader
     */
    public abstract BufferedReader downloadData();

    /**
     * ----------------------INNER CLASS----------------------------------------
     * Inner Class to handle and store the data for display in the table in the
     * advanced view of BloodPressure
     */
    public static class MeterReading {

        private final SimpleStringProperty date;
        private final SimpleStringProperty systol;
        private final SimpleStringProperty diastol;
        private final SimpleStringProperty pulseRate;
        private final SimpleStringProperty user;
        private final SimpleStringProperty rest;
        private final SimpleStringProperty arrhythmia;

        private MeterReading(String date, String systol, String diastol, String pulseRate, String user, String rest, String arrythmia) {
            this.date = new SimpleStringProperty(date);
            this.systol = new SimpleStringProperty(systol);
            this.diastol = new SimpleStringProperty(diastol);
            this.pulseRate = new SimpleStringProperty(pulseRate);
            this.user = new SimpleStringProperty(user);
            this.rest = new SimpleStringProperty(rest);
            this.arrhythmia = new SimpleStringProperty(arrythmia);
        }

        /**
         *
         * @return
         */
        public String getDate() {
            return date.get();
        }

        /**
         *
         * @param d
         */
        public void setDate(String d) {
            date.set(d);
        }

        /**
         *
         * @return
         */
        public String getSystol() {
            return systol.get();
        }

        /**
         *
         * @param s
         */
        public void setSystol(String s) {
            systol.set(s);
        }

        /**
         *
         * @return
         */
        public String getDiastol() {
            return diastol.get();
        }

        /**
         *
         * @param d
         */
        public void setDiastol(String d) {
            diastol.set(d);
        }

        /**
         *
         * @return
         */
        public String getPulseRate() {
            return pulseRate.get();
        }

        /**
         *
         * @param d
         */
        public void setPulseRate(String d) {
            pulseRate.set(d);
        }

        /**
         *
         * @return
         */
        public String getUser() {
            return user.get();
        }

        /**
         *
         * @param d
         */
        public void setUser(String d) {
            user.set(d);
        }

        /**
         *
         * @return
         */
        public String getRest() {
            return rest.get();
        }

        /**
         *
         * @param d
         */
        public void setRest(String d) {
            rest.set(d);
        }

        /**
         *
         * @return
         */
        public String getArrhythmia() {
            return arrhythmia.get();
        }

        /**
         *
         * @param d
         */
        public void setArrhythmia(String d) {
            arrhythmia.set(d);
        }

    }
}
