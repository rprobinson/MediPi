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
package org.medipi.devices;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.devices.drivers.domain.DeviceTimestampChecker;
import org.medipi.model.DeviceDataDO;
import org.medipi.utilities.Utilities;

/**
 * Class to display and handle the functionality for a generic Pulse Oximeter
 * Medical Device.
 *
 * Generic Oximeter class which exposes basic generic information and the data
 * to other classes and allows the classes which are specific to a particular
 * device to set data
 *
 * @author rick@robinsonhq.com
 */
public abstract class Oximeter extends Device {

    private final String GENERIC_DEVICE_NAME = "Oximeter";
    private static final String PROFILEID = "urn:nhs-en:profile:Oximeter";
    protected Button actionButton;
    private ArrayList<ArrayList<String>> deviceData = new ArrayList<>();
    private Instant schedStartTime = null;
    private Instant schedExpireTime = null;
    private VBox oxiWindow;
    private Label meanPulseTF;
    private Label meanPulseDB;
    private Label meanSpO2TF;
    private Label meanSpO2DB;
    private Label measurementTimeTF;
    private Label measurementTimeLabel;
    private VBox resultsVBox;
    private final IntegerProperty pulse = new SimpleIntegerProperty(0);
    private final IntegerProperty spO2 = new SimpleIntegerProperty(0);
    private final StringProperty lastMeasurementTime = new SimpleStringProperty();
    private final StringProperty resultsSummary = new SimpleStringProperty();
    private DeviceTimestampChecker deviceTimestampChecker;
    protected static String initialButtonText;
    protected static Node initialGraphic = null;

    protected ArrayList<String> metadata = new ArrayList<>();

    protected ArrayList<String> columns = new ArrayList<>();
    protected ArrayList<String> format = new ArrayList<>();
    protected ArrayList<String> units = new ArrayList<>();

    /**
     * This is the data separator from the MediPi.properties file
     *
     */
    protected String separator;

    /**
     * Constructor for a Generic Oximeter
     *
     */
    public Oximeter() {
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
        actionButton = new Button(initialButtonText, initialGraphic);
        actionButton.setId("button-record");
        actionButton.setMaxHeight(20);

        oxiWindow = new VBox();
        oxiWindow.setPadding(new Insets(0, 5, 0, 5));
        oxiWindow.setSpacing(5);
        oxiWindow.setMinSize(800, 300);
        oxiWindow.setMaxSize(800, 300);
        //ascertain if this element is to be displayed on the dashboard
        String b = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".showdashboardtile");
        if (b == null || b.trim().length() == 0) {
            showTile = new SimpleBooleanProperty(true);
        } else {
            showTile = new SimpleBooleanProperty(!b.toLowerCase().startsWith("n"));
        }
        Guide guide = new Guide(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".guide");
        meanPulseDB = new Label("");
        meanPulseTF = new Label("--");
        meanPulseTF.setId("resultstext");

        meanSpO2DB = new Label("");
        meanSpO2TF = new Label("--");
        meanSpO2TF.setId("resultstext");

        measurementTimeTF = new Label("");
        measurementTimeTF.setId("resultstimetext");
        measurementTimeLabel = new Label("");
        this.resetDevice();

        // create the large result box for the average value of the measurements
        // taken over the period
        // heart rate reading
        HBox pulseRateHbox = new HBox();
        pulseRateHbox.setAlignment(Pos.CENTER_LEFT);
        pulseRateHbox.setPadding(new Insets(0, 0, 0, 10));
        Label bpm = new Label("BPM");
        bpm.setId("resultsunits");
        pulseRateHbox.getChildren().addAll(
                meanPulseTF,
                bpm
        );
        // SpO2 reading
        HBox spO2RateHbox = new HBox();
        spO2RateHbox.setAlignment(Pos.CENTER_LEFT);
        spO2RateHbox.setPadding(new Insets(0, 0, 0, 10));
        Label spO2Label = new Label("SpO2");
        spO2Label.setId("resultsunits");
        spO2RateHbox.getChildren().addAll(
                meanSpO2TF,
                spO2Label
        );

        resultsVBox = new VBox();
        resultsVBox.setPrefWidth(200);
        resultsVBox.setId("resultsbox");
        resultsVBox.getChildren().addAll(
                spO2RateHbox,
                pulseRateHbox,
                measurementTimeLabel,
                measurementTimeTF
        );
        //create the main window HBox
        HBox dataHBox = new HBox();
        dataHBox.getChildren().addAll(
                guide.getGuide(),
                resultsVBox
        );
        oxiWindow.getChildren().addAll(
                dataHBox
        );
        // set main Element window
        window.setCenter(oxiWindow);
        setButton2(actionButton);

        // Setup reccord button action to start or stop the main task
        actionButton.setOnAction((ActionEvent t) -> {
            if (confirmReset()) {
                downloadData();
            }
        });

        meanPulseTF.textProperty().bind(
                Bindings.when(pulse.isEqualTo(0))
                .then("--")
                .otherwise(pulse.asString())
        );
        meanPulseDB.textProperty().bind(
                Bindings.when(pulse.isEqualTo(0))
                .then("")
                .otherwise(pulse.asString())
        );
        meanSpO2TF.textProperty().bind(
                Bindings.when(spO2.isEqualTo(0))
                .then("--")
                .otherwise(spO2.asString())
        );
        meanSpO2DB.textProperty().bind(
                Bindings.when(spO2.isEqualTo(0))
                .then("")
                .otherwise(spO2.asString())
        );
        measurementTimeTF.textProperty().bind(
                Bindings.when(lastMeasurementTime.isEqualTo(""))
                .then("")
                .otherwise(lastMeasurementTime)
        );
        measurementTimeLabel.textProperty().bind(
                Bindings.when(lastMeasurementTime.isEqualTo(""))
                .then("")
                .otherwise("measured at")
        );
        window.visibleProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                guide.reset();

            }
        });

        // bind the button disable to the time sync indicator
        actionButton.disableProperty().bind(medipi.timeSync.not());

        // This class is used to deny access to recording data if time has not been synchronised
        deviceTimestampChecker = new DeviceTimestampChecker(medipi, this);
        // successful initiation of the this class results in a null return
        return null;
    }

    /**
     * method to get the generic device name of the device
     *
     * @return generic device name e.g. Oximeter
     */
    @Override
    public String getGenericDeviceDisplayName() {
        return GENERIC_DEVICE_NAME;
    }

    @Override
    public String getProfileId() {
        return PROFILEID;
    }

    // resets the device
    @Override
    public void resetDevice() {
        deviceData = new ArrayList<>();
        hasData.set(false);
        metadata.clear();
        pulse.set(0);
        spO2.set(0);
        lastMeasurementTime.set("");
        resultsSummary.setValue("");
        schedStartTime = null;
        schedExpireTime = null;
    }

    /**
     * Gets a DeviceDO representation of the data
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
        sb.append("metadata->make->").append(getMake()).append("\n");
        sb.append("metadata->model->").append(getModel()).append("\n");
        sb.append("metadata->displayname->").append(getSpecificDeviceDisplayName()).append("\n");
        sb.append("metadata->datadelimiter->").append(separator).append("\n");
        if (medipi.getScheduler() != null) {
            sb.append("metadata->scheduleeffectivedate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(schedStartTime)).append("\n");
            sb.append("metadata->scheduleexpirydate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(schedExpireTime)).append("\n");
        }
        sb.append("metadata->columns->");
        for (String string : columns) {
            sb.append(string).append(separator);
        }
        //replace the last separator with a new line
        sb.replace(sb.length() - separator.length(), sb.length(), "\n");

        sb.append("metadata->format->");
        for (String string : format) {
            sb.append(string).append(separator);
        }
        //replace the last separator with a new line
        sb.replace(sb.length() - separator.length(), sb.length(), "\n");

        sb.append("metadata->units->");
        for (String string : units) {
            sb.append(string).append(separator);
        }
        //replace the last separator with a new line
        sb.replace(sb.length() - separator.length(), sb.length(), "\n");

        // Add Downloaded data
        for (ArrayList<String> dataLine : deviceData) {
            for (String data : dataLine) {
                sb.append(data).append(separator);
            }
            //replace the last separator with a new line
            sb.replace(sb.length() - separator.length(), sb.length(), "\n");
        }

        payload.setProfileId(PROFILEID);
        payload.setPayload(sb.toString());
        return payload;
    }

    @Override
    public void setData(ArrayList<ArrayList<String>> data) {
        data = deviceTimestampChecker.checkTimestamp(data);
        String dataCheckMessage = null;
        if ((dataCheckMessage = deviceTimestampChecker.getMessages()) != null) {
            MediPiMessageBox.getInstance().makeMessage(getSpecificDeviceDisplayName() + "\n" + dataCheckMessage);
        }
        if (data == null || data.isEmpty()) {
        } else {
            for (ArrayList<String> a : data) {
                Instant i = Instant.parse(a.get(0));
                final int pulse = Integer.parseInt(a.get(1));
                final int spO2 = Integer.parseInt(a.get(2));
                displayData(i, pulse, spO2);
                hasData.set(true);
            }
            deviceData = data;
            Scheduler scheduler = null;
            if ((scheduler = medipi.getScheduler()) != null) {
                schedStartTime = scheduler.getCurrentScheduleStartTime();
                schedExpireTime = scheduler.getCurrentScheduleExpiryTime();
            }
        }

    }

    protected void displayData(Instant time, int pulse, int spo2) {
        Platform.runLater(() -> {
            this.pulse.set(pulse);
            this.spO2.set(spo2);
            this.lastMeasurementTime.set(Utilities.DISPLAY_DEVICE_FORMAT_LOCALTIME.format(time));
            resultsSummary.set(getGenericDeviceDisplayName() + " - "
                    + this.pulse.getValue().toString() + "BPM, "
                    + this.spO2.getValue().toString() + "%SpO2");
        });
    }

    /**
     * method to return the component to the dashboard
     *
     * @return @throws Exception
     */
    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(getGenericDeviceDisplayName());
        dashComponent.addOverlay(meanPulseDB, "BPM");
        dashComponent.addOverlay(meanSpO2DB, "SpO2");
        dashComponent.addOverlay(Color.LIGHTGREEN, hasDataProperty());
        return dashComponent.getTile();
    }

    @Override
    public StringProperty getResultsSummary() {
        return resultsSummary;
    }

    /**
     * Abstract method to download data from the device driver
     *
     */
    protected abstract void downloadData();

}
