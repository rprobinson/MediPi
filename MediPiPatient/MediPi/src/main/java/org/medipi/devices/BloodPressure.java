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

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.utilities.Utilities;
import org.medipi.model.DeviceDataDO;

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
@SuppressWarnings("restriction")
public abstract class BloodPressure extends Device {

    private final String DEVICE_TYPE = "Blood Pressure";
    private final static String PROFILEID = "urn:nhs-en:profile:BloodPressure";
    protected ArrayList<String[]> deviceData = new ArrayList<>();
    // property to indicate whether data has been recorded for this device
    protected BooleanProperty hasData = new SimpleBooleanProperty(false);
    private VBox meterWindow;
    private TableView<MeterReading> table = new TableView<>();
    private ObservableList<MeterReading> data = FXCollections.observableArrayList();
    private VBox deviceWindow;
    protected ProgressBar downProg = new ProgressBar(0.0F);
    private Label lastSystolDB;
    private Label lastSystol;
    private Label lastDiastolDB;
    private Label lastDiastol;
    private Label lastPulseDB;
    private Label lastPulse;
    private Node dataBox;
    protected Button downloadButton;
    // initially set to unix epoch time
    private Instant lastDate = Instant.EPOCH;

    private ArrayList<String> metadata = new ArrayList<>();

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
        ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        iw.setRotate(90);
        downloadButton = new Button("Download", iw);
        downloadButton.setId("button-download");
        meterWindow = new VBox();
        meterWindow.setPadding(new Insets(0, 5, 0, 5));
        meterWindow.setSpacing(5);
        meterWindow.setMinSize(800, 350);
        meterWindow.setMaxSize(800, 350);
        downProg.setVisible(false);
        HBox buttonHbox = new HBox();
        buttonHbox.setSpacing(10);
        buttonHbox.getChildren().add(downloadButton);
        if (progressBarResolution > 0D) {
            buttonHbox.getChildren().add(downProg);
        }
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
            table.getColumns().addAll(dateCol, systolCol, diastolCol, pulseRateCol, restCol, arrhythmiaCol);

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

        setButton2(buttonHbox);

        downloadButton(meterVBox);

        // successful initiation of the this class results in a null return
        return null;
    }

    private void downloadButton(VBox meterVBox) {
        // Setup download button action to run in its own thread
        downloadButton.setOnAction((ActionEvent t) -> {
            resetDevice();
            downloadData(meterVBox);
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
        metadata.clear();
        if (!medipi.isBasicDataView()) {
            data.clear();
        }
        lastSystol.setText("--");
        lastSystolDB.setText("");
        lastDiastol.setText("--");
        lastDiastolDB.setText("");
        lastPulse.setText("--");
        lastPulseDB.setText("");
        lastDate = Instant.EPOCH;
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
     * Gets a DeviceDataDO representation of the data
     *
     * @return DevicedataDO containing the payload
     */
    @Override
    public DeviceDataDO getData() {
        DeviceDataDO payload = new DeviceDataDO(UUID.randomUUID().toString());
        StringBuilder sb = new StringBuilder();
        //Add MetaData
        sb.append("metadata->persist->medipiversion->").append(medipi.getVersion()).append("\n");
        for (String s : metadata) {
            sb.append("metadata->persist->").append(s).append("\n");
        }
        sb.append("metadata->subtype->").append(getName()).append("\n");
        sb.append("metadata->datadelimiter->").append(medipi.getDataSeparator()).append("\n");
        if (scheduler != null) {
            sb.append("metadata->scheduleeffectivedate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(scheduler.getCurrentScheduledEventTime())).append("\n");
            sb.append("metadata->scheduleexpirydate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(scheduler.getNextScheduledEventTime())).append("\n");
        }
        sb.append("metadata->columns->")
                .append("iso8601time").append(medipi.getDataSeparator())
                .append("systol").append(medipi.getDataSeparator())
                .append("diastol").append(medipi.getDataSeparator())
                .append("pulserate").append(medipi.getDataSeparator())
                .append("rest").append(medipi.getDataSeparator())
                .append("arrhythmia").append("\n");
        sb.append("metadata->format->")
                .append("DATE").append(medipi.getDataSeparator())
                .append("INTEGER").append(medipi.getDataSeparator())
                .append("INTEGER").append(medipi.getDataSeparator())
                .append("INTEGER").append(medipi.getDataSeparator())
                .append("BOOLEAN").append(medipi.getDataSeparator())
                .append("BOOLEAN").append("\n");
        sb.append("metadata->units->")
                .append("NONE").append(medipi.getDataSeparator())
                .append("mmHg").append(medipi.getDataSeparator())
                .append("mmHg").append(medipi.getDataSeparator())
                .append("BPM").append(medipi.getDataSeparator())
                .append("NONE").append(medipi.getDataSeparator())
                .append("NONE").append("\n");
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
            sb.append("\n");
        }
        payload.setProfileId(PROFILEID);
        payload.setPayload(sb.toString());
        return payload;
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
    protected void addDataPoint(Instant d, int systol, int diastol, int pulseRate, boolean rest, boolean arrhythmia) {
        if (d.isAfter(lastDate)) {
            lastSystol.setText(String.valueOf(systol));
            lastSystolDB.setText(String.valueOf(systol));
            lastDiastol.setText(String.valueOf(diastol));
            lastDiastolDB.setText(String.valueOf(diastol));
            lastPulse.setText(String.valueOf(pulseRate));
            lastPulseDB.setText(String.valueOf(pulseRate));
            lastDate = d;
        }
        if (!medipi.isBasicDataView()) {
            MeterReading mr = new MeterReading(Utilities.DISPLAY_TABLE_FORMAT_LOCALTIME.format(d), String.valueOf(systol), String.valueOf(diastol), String.valueOf(pulseRate), String.valueOf(rest), String.valueOf(arrhythmia));
            data.add(mr);
        }

    }

    /**
     * Abstract method to download data from the device driver
     *
     * @return bufferedReader
     */
    public abstract void downloadData(VBox meterVBox);

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
        private final SimpleStringProperty rest;
        private final SimpleStringProperty arrhythmia;

        private MeterReading(String date, String systol, String diastol, String pulseRate, String rest, String arrythmia) {
            this.date = new SimpleStringProperty(date);
            this.systol = new SimpleStringProperty(systol);
            this.diastol = new SimpleStringProperty(diastol);
            this.pulseRate = new SimpleStringProperty(pulseRate);
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
