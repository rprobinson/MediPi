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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.devices.drivers.domain.DeviceTimestampChecker;
import org.medipi.utilities.Utilities;
import org.medipi.model.DeviceDataDO;

/**
 * Class to display and handle the functionality for a generic BloodPressure
 * Medical Device.
 *
 * Generic Blood pressure class which exposes basic generic information and the
 * data to other classes and allows the classes which are specific to a
 * particular device to set data
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public abstract class BloodPressure extends Device {

    private final String GENERIC_DEVICE_NAME = "Blood Pressure";
    private String profileId = "urn:nhs-en:profile:BloodPressure";
    private VBox meterWindow;

    private Label lastSystolDB;
    private Label lastSystol;
    private Label lastDiastolDB;
    private Label lastDiastol;
    private Label lastPulseDB;
    private Label lastPulse;
    private Label measurementTimeTF;
    private Label measurementTimeLabel;

    protected String measurementContext = null;

    protected Button downloadButton;
    protected static String initialButtonText;
    protected static ImageView initialGraphic = null;

    private ArrayList<ArrayList<String>> deviceData = new ArrayList<>();
    private Instant schedStartTime = null;
    private Instant schedExpireTime = null;
    private final StringProperty resultsSummary = new SimpleStringProperty();
    private DeviceTimestampChecker deviceTimestampChecker;
    protected IntegerProperty systol = new SimpleIntegerProperty(0);
    protected IntegerProperty diastol = new SimpleIntegerProperty(0);
    protected IntegerProperty heartrate = new SimpleIntegerProperty(0);
    private final StringProperty lastMeasurementTime = new SimpleStringProperty();

    protected ProgressBar downProg = new ProgressBar(0.0F);

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
        measurementContext = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".measurementcontext");
        if (measurementContext != null) {
            final StringBuilder pid = new StringBuilder(measurementContext.length());

            for (final String word : measurementContext.split(" ")) {
                if (!word.isEmpty()) {
                    pid.append(word.substring(0, 1).toUpperCase());
                    pid.append(word.substring(1).toLowerCase());
                }
            }
            profileId = profileId + pid.toString();
        }
        separator = medipi.getDataSeparator();
        ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        iw.setRotate(90);
        downloadButton = new Button(initialButtonText, initialGraphic);
        downloadButton.setId("button-download");
        meterWindow = new VBox();
        meterWindow.setPadding(new Insets(0, 5, 0, 5));
        meterWindow.setSpacing(5);
        meterWindow.setMinSize(800, 300);
        meterWindow.setMaxSize(800, 300);
        downProg.setVisible(false);
        HBox buttonHbox = new HBox();
        buttonHbox.setSpacing(10);
        buttonHbox.getChildren().add(downloadButton);
        if (progressBarResolution > 0D) {
            buttonHbox.getChildren().add(downProg);
        }
        Guide guide = new Guide(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".guide");
        //ascertain if this element is to be displayed on the dashboard
        String b = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".showdashboardtile");
        if (b == null || b.trim().length() == 0) {
            showTile = new SimpleBooleanProperty(true);
        } else {
            showTile = new SimpleBooleanProperty(!b.toLowerCase().startsWith("n"));
        }
        //Set the initial values of the simple results
        lastSystol = new Label("--");
        lastSystol.setId("resultstext");
        lastSystolDB = new Label("");
        lastDiastol = new Label("--");
        lastDiastol.setId("resultstext");
        lastDiastolDB = new Label("");
        lastPulse = new Label("--");
        lastPulse.setId("resultstext");
        lastPulseDB = new Label("");
        measurementTimeTF = new Label("");
        measurementTimeTF.setId("resultstimetext");
        measurementTimeLabel = new Label("");

        // create the large result box for the last measurement
        // downloaded from the device
        // Systolic reading
        HBox systolHbox = new HBox();
        systolHbox.setAlignment(Pos.CENTER_LEFT);
        systolHbox.setPadding(new Insets(0, 0, 0, 10));
        Label mmhg = new Label("mmHg");
        mmhg.setId("resultsunits");
        systolHbox.getChildren().addAll(
                lastSystol,
                mmhg
        );
        // Diastolic reading
        HBox diastolHbox = new HBox();
        diastolHbox.setAlignment(Pos.CENTER_LEFT);
        diastolHbox.setPadding(new Insets(0, 0, 0, 10));
        Label mmhg2 = new Label("mmHg");
        mmhg2.setId("resultsunits");
        diastolHbox.getChildren().addAll(
                lastDiastol,
                mmhg2
        );
        // Heart Rate reading
        HBox pulseHbox = new HBox();
        pulseHbox.setAlignment(Pos.CENTER_LEFT);
        pulseHbox.setPadding(new Insets(0, 0, 0, 10));
        Label bpm = new Label("BPM");
        bpm.setId("resultsunits");
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
                pulseHbox,
                measurementTimeLabel,
                measurementTimeTF
        );

        //create the main window HBox
        HBox dataHBox = new HBox();
        dataHBox.getChildren().addAll(
                guide.getGuide(),
                meterVBox
        );

        meterWindow.getChildren().addAll(
                dataHBox
        );
        // set main Element window
        window.setCenter(meterWindow);

        setButton2(downloadButton);

        downloadButton(meterVBox);
        lastSystol.textProperty().bind(
                Bindings.when(systol.isEqualTo(0))
                .then("--")
                .otherwise(systol.asString())
        );
        lastSystolDB.textProperty().bind(
                Bindings.when(systol.isEqualTo(0))
                .then("")
                .otherwise(systol.asString())
        );
        lastDiastol.textProperty().bind(
                Bindings.when(diastol.isEqualTo(0))
                .then("--")
                .otherwise(diastol.asString())
        );
        lastDiastolDB.textProperty().bind(
                Bindings.when(diastol.isEqualTo(0))
                .then("")
                .otherwise(diastol.asString())
        );
        lastPulse.textProperty().bind(
                Bindings.when(heartrate.isEqualTo(0))
                .then("--")
                .otherwise(heartrate.asString())
        );
        lastPulseDB.textProperty().bind(
                Bindings.when(heartrate.isEqualTo(0))
                .then("")
                .otherwise(heartrate.asString())
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
        downloadButton.disableProperty().bind(medipi.timeSync.not());
        // This class is used to deny access to recording data if time has not been synchronised
        deviceTimestampChecker = new DeviceTimestampChecker(medipi, this);
        // successful initiation of the this class results in a null return
        return null;
    }

    private void downloadButton(VBox meterVBox) {
        // Setup download button action to run in its own thread
        downloadButton.setOnAction((ActionEvent t) -> {
            if (confirmReset()) {
                resetDevice();
                downloadData(meterVBox);
            }
        });
    }

    /**
     * method to get the generic device name of the device
     *
     * @return generic device name e.g. Blood Pressure
     */
    @Override
    public String getGenericDeviceDisplayName() {
        if (measurementContext != null) {
            return GENERIC_DEVICE_NAME + " (" + measurementContext + ")";
        } else {
            return GENERIC_DEVICE_NAME;
        }
    }

    @Override
    public String getProfileId() {
        return profileId;
    }

    // reset the device
    @Override
    public void resetDevice() {
        deviceData = new ArrayList<>();
        hasData.set(false);
        metadata.clear();
        systol.set(0);
        diastol.set(0);
        heartrate.set(0);
        lastMeasurementTime.set("");
        resultsSummary.setValue("");
        schedStartTime = null;
        schedExpireTime = null;
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
        sb.append("metadata->make->").append(getMake()).append("\n");
        sb.append("metadata->model->").append(getModel()).append("\n");
        sb.append("metadata->displayname->").append(getSpecificDeviceDisplayName()).append("\n");
        sb.append("metadata->datadelimiter->").append(medipi.getDataSeparator()).append("\n");
        if (medipi.getScheduler() != null) {
            sb.append("metadata->scheduleeffectivedate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(schedStartTime)).append("\n");
            sb.append("metadata->scheduleexpirydate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(schedExpireTime)).append("\n");
        }
        sb.append("metadata->columns->");
        for (String string : columns) {
            sb.append(string).append(medipi.getDataSeparator());
        }
        //replace the last separator with a new line
        sb.replace(sb.length() - medipi.getDataSeparator().length(), sb.length(), "\n");

        sb.append("metadata->format->");
        for (String string : format) {
            sb.append(string).append(medipi.getDataSeparator());
        }
        //replace the last separator with a new line
        sb.replace(sb.length() - medipi.getDataSeparator().length(), sb.length(), "\n");

        sb.append("metadata->units->");
        for (String string : units) {
            sb.append(string).append(medipi.getDataSeparator());
        }
        //replace the last separator with a new line
        sb.replace(sb.length() - medipi.getDataSeparator().length(), sb.length(), "\n");

        // Add Downloaded data
        for (ArrayList<String> dataLine : deviceData) {
            for (String data : dataLine) {
                sb.append(data).append(separator);
            }
            //replace the last separator with a new line
            sb.replace(sb.length() - separator.length(), sb.length(), "\n");
        }

        payload.setProfileId(profileId);
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
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(getGenericDeviceDisplayName());
        dashComponent.addOverlay(lastSystolDB, "mmHg");
        dashComponent.addOverlay(lastDiastolDB, "mmHg");
        dashComponent.addOverlay(lastPulseDB, "BPM");
        dashComponent.addOverlay(Color.LIGHTGREEN, hasDataProperty());
        return dashComponent.getTile();
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
                final int systol = Integer.parseInt(a.get(1));
                final int diastol = Integer.parseInt(a.get(2));
                final int pulse = Integer.parseInt(a.get(3));
                displayData(i, systol, diastol, pulse);
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

    /**
     * Private method to add data to the internal structure and propogate it to
     * the UI
     *
     * @param time
     * @param systol reading
     * @param diastol reading
     * @param heartrate reading
     */
    private void displayData(Instant time, int systol, int diastol, int heartrate) {

        Platform.runLater(() -> {
            this.systol.set(systol);
            this.diastol.set(diastol);
            this.heartrate.set(heartrate);
            this.lastMeasurementTime.set(Utilities.DISPLAY_DEVICE_FORMAT_LOCALTIME.format(time));
            resultsSummary.set(getGenericDeviceDisplayName() + " - "
                    + this.systol.getValue().toString()
                    + "/"
                    + this.diastol.getValue().toString()
                    + "mmHg "
                    + this.heartrate.getValue().toString()
                    + "BPM"
            );
        });
    }

    /**
     * Abstract method to download data from the device driver
     *
     * @param meterVBox
     */
    public abstract void downloadData(VBox meterVBox);

    @Override
    public StringProperty getResultsSummary() {
        return resultsSummary;
    }

}
