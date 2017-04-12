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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
 * Class to display and handle the functionality for a generic Diagnostic Scale
 * Medical Device.
 *
 * Generic Scale class which exposes basic generic information and the data to
 * other classes and allows the classes which are specific to a particular
 * device to set data
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public abstract class Scale extends Device {

    private final String GENERIC_DEVICE_NAME = "Scale";
    private final static String PROFILEID = "urn:nhs-en:profile:DiagnosticScale";
    private VBox scaleWindow;

    private Label weightDB;
    private Label weightTF;
    private Label weightInStoneTF;
    private Label measurementTimeTF;
    private Label measurementTimeLabel;
    private VBox resultsVBox;
    private DeviceTimestampChecker deviceTimestampChecker;

    protected Button downloadButton;
    protected static String initialButtonText;
    protected static ImageView initialGraphic = null;
    private ArrayList<ArrayList<String>> deviceData = new ArrayList<>();
    private Instant schedStartTime = null;
    private Instant schedExpireTime = null;

    protected DoubleProperty weight = new SimpleDoubleProperty(0D);
    protected DoubleProperty weightStones = new SimpleDoubleProperty(0D);
    private final StringProperty lastMeasurementTime = new SimpleStringProperty();
    private final StringProperty resultsSummary = new SimpleStringProperty();
    protected ProgressBar downProg = new ProgressBar(0.0F);

    protected HBox weightHBox;
    protected HBox weightInStoneHBox;
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
     * no progressbar is shown
     */
    protected Double progressBarResolution = null;

    /**
     * Constructor for Generic Diagnostic scale
     *
     */
    public Scale() {
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
        downloadButton = new Button(initialButtonText, initialGraphic);
        downloadButton.setId("button-download");
        scaleWindow = new VBox();
        scaleWindow.setPadding(new Insets(0, 5, 0, 5));
        scaleWindow.setSpacing(5);
        scaleWindow.setMinSize(800, 300);
        scaleWindow.setMaxSize(800, 300);
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

        // create the large result box for the last measurement
        // downloaded from the device
        // Mass reading
        weightTF = new Label("--");
        weightInStoneTF = new Label("--");
        weightDB = new Label("");
        weightTF.setId("resultstext");
        weightInStoneTF.setId("resultstext");
        measurementTimeTF = new Label("");
        measurementTimeTF.setId("resultstimetext");
        measurementTimeLabel = new Label("");

        weightHBox = new HBox();
        weightHBox.setAlignment(Pos.CENTER_LEFT);
        weightHBox.setId("resultsbox");
        weightHBox.setPrefWidth(200);
        Label kg = new Label("kg");
        kg.setId("resultsunits");
        weightHBox.getChildren().addAll(
                weightTF,
                kg
        );
        weightInStoneHBox = new HBox();
        weightInStoneHBox.setAlignment(Pos.CENTER_LEFT);
        weightInStoneHBox.setId("resultsbox");
        weightInStoneHBox.setPrefWidth(200);
        Label stone = new Label("st");
        stone.setId("resultsunits");
        weightInStoneHBox.getChildren().addAll(
                weightInStoneTF,
                stone
        );
        resultsVBox = new VBox();
        resultsVBox.setPrefWidth(200);
        resultsVBox.setId("resultsbox");
        resultsVBox.getChildren().addAll(
                weightHBox,
                weightInStoneHBox,
                measurementTimeLabel,
                measurementTimeTF
        );
        //create the main window HBox
        HBox dataHBox = new HBox();
        dataHBox.getChildren().addAll(
                guide.getGuide(),
                resultsVBox
        );
        scaleWindow.getChildren().addAll(
                dataHBox
        );
        // set main Element window
        window.setCenter(scaleWindow);
        setButton2(downloadButton);

        downloadButton();
        weightTF.textProperty().bind(
                Bindings.when(weight.isEqualTo(0))
                .then("--")
                .otherwise(weight.asString())
        );
        weightInStoneTF.textProperty().bind(
                Bindings.when(weightStones.isEqualTo(0))
                .then("--")
                .otherwise(weightStones.asString())
        );
        weightDB.textProperty().bind(
                Bindings.when(weight.isEqualTo(0))
                .then("")
                .otherwise(weight.asString())
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

    private void downloadButton() {
        // Setup download button action to run in its own thread
        downloadButton.setOnAction((ActionEvent t) -> {
            if (confirmReset()) {
                resetDevice();
                downloadData();
            }
        });
    }

    /**
     * method to get the generic Type of the device
     *
     * @return generic type of device e.g. Blood Pressure
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
        weight.set(0);
        weightStones.set(0);
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
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(getGenericDeviceDisplayName());
        dashComponent.addOverlay(weightDB, "kg");
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
                final Double weight = Double.valueOf(a.get(1));
                displayData(weight, i);
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
     * @param weight in kg
     */
    private void displayData(double weight, Instant time) {

        //weight graph - this is expected for all data points
        Platform.runLater(() -> {
            this.weight.set(weight);
            this.weightStones.set(convertToStone(weight));
            this.lastMeasurementTime.set(Utilities.DISPLAY_DEVICE_FORMAT_LOCALTIME.format(time));
            resultsSummary.set(getGenericDeviceDisplayName() + " - "
                    + this.weight.getValue().toString() + "kg");
        });
    }

    /**
     * Abstract method to download data from the device driver
     *
     */
    protected abstract void downloadData();

    @Override
    public StringProperty getResultsSummary() {
        return resultsSummary;
    }

    private double convertToStone(double weight) {
        return (double)Math.round((weight * 0.157473044418) * 100d) / 100d;
    }

}
