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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.medipi.utilities.Roller;
import org.medipi.utilities.Utilities;

/**
 * Class to display and handle the functionality for a manual input only
 * thermometer
 *
 * TODO; this class is not genericised as are the other device classes and will
 * need refactoring to be so. This has been done in the first instance for
 * expediency.
 *
 * @author rick@robinsonhq.com
 */
public abstract class Thermometer extends Device {

    private final String GENERIC_DEVICE_NAME = "Thermometer";
    private static final String PROFILEID = "urn:nhs-en:profile:Thermometer";
    protected Button actionButton;
    private ArrayList<ArrayList<String>> deviceData = new ArrayList<>();
    private Instant schedStartTime = null;
    private Instant schedExpireTime = null;
    private VBox thermWindow;
    private Roller tempTens;
    private Roller tempUnits;
    private Roller tempTenths;
    private Label tempDB;
    private Label measurementTimeTF;
    private Label measurementTimeLabel;
    private VBox resultsVBox;
    private final DoubleProperty temp = new SimpleDoubleProperty(0.0D);
    private final StringProperty lastMeasurementTime = new SimpleStringProperty();
    private final StringProperty resultsSummary = new SimpleStringProperty();
    protected static String initialButtonText;
    protected static Node initialGraphic = null;
    private DeviceTimestampChecker deviceTimestampChecker;

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
    public Thermometer() {
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

        thermWindow = new VBox();
        thermWindow.setPadding(new Insets(0, 5, 0, 5));
        thermWindow.setSpacing(5);
        thermWindow.setMinSize(800, 300);
        thermWindow.setMaxSize(800, 300);
        Guide guide = new Guide(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".guide");
        // fundamental UI decisions made from the properties
        String b = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".showdashboardtile");
        //ascertain if this element is to be displayed on the dashboard
        if (b == null || b.trim().length() == 0) {
            showTile = new SimpleBooleanProperty(true);
        } else {
            showTile = new SimpleBooleanProperty(!b.toLowerCase().startsWith("n"));
        }
        tempDB = new Label("");
        tempTens = new Roller(50, 110, 3, 4);
        tempUnits = new Roller(50, 110, 0, 9);
        Label tempPoint = new Label(".");
        tempPoint.setId("resultstext");
        tempTenths = new Roller(50, 110, 0, 9);
        tempTens.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                evaluate();
            }
        }
        );
        tempUnits.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                evaluate();
            }
        }
        );
        tempTenths.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                evaluate();
            }
        }
        );
        measurementTimeTF = new Label("");
        measurementTimeTF.setId("resultstimetext");
        measurementTimeLabel = new Label("");
        this.resetDevice();

        // create the large result box for the average value of the measurements
        // taken over the period
        // heart rate reading
        HBox thermHbox = new HBox();
        thermHbox.setAlignment(Pos.CENTER);
        Label units = new Label((char) 176 + "C");
        units.setId("resultsunits");
        thermHbox.getChildren().addAll(
                tempTens.getRoller(),
                tempUnits.getRoller(),
                tempPoint,
                tempTenths.getRoller(),
                units
        );
        resultsVBox = new VBox();
        resultsVBox.setMinWidth(200);
        resultsVBox.setId("resultsbox");
        resultsVBox.getChildren().addAll(
                thermHbox,
                measurementTimeLabel,
                measurementTimeTF
        );
        //create the main window HBox
        HBox dataHBox = new HBox();
        dataHBox.getChildren().addAll(
                guide.getGuide(),
                resultsVBox
        );
        thermWindow.getChildren().addAll(
                dataHBox
        );
        // set main Element window
        window.setCenter(thermWindow);
        setButton2(null);

        tempDB.textProperty().bind(
                Bindings.when(temp.isEqualTo(0))
                .then("")
                .otherwise(temp.asString())
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
        // successful initiation of the this class results in a null return
        // bind the button disable to the time sync indicator
        tempTens.disableProperty().bind(medipi.timeSync.not());
        tempUnits.disableProperty().bind(medipi.timeSync.not());
        tempPoint.disableProperty().bind(medipi.timeSync.not());
        tempTenths.disableProperty().bind(medipi.timeSync.not());
        deviceTimestampChecker = new DeviceTimestampChecker(medipi, this);
        return null;
    }

    private void evaluate() {
        if (tempTens.hasValue() && tempUnits.hasValue() && tempTenths.hasValue()) {
            double value = (tempTens.getValue() * 10) + (tempUnits.getValue()) + (tempTenths.getValue() / 10.0);
            Instant valueTime = Instant.now();
            displayData(value, valueTime);
            deviceData.clear();
            ArrayList<String> deviceDataSingleRow = new ArrayList<>();
            deviceDataSingleRow.add(valueTime.toString());
            deviceDataSingleRow.add(String.valueOf(value));
            
            deviceData.add(deviceDataSingleRow);
            deviceData = (deviceTimestampChecker.checkTimestamp(deviceData));
            String dataCheckMessage = null;
            if ((dataCheckMessage = deviceTimestampChecker.getMessages()) != null) {
                MediPiMessageBox.getInstance().makeMessage(getSpecificDeviceDisplayName() + "\n" + dataCheckMessage);
            }
            if (deviceData == null || deviceData.isEmpty()) {
            } else {
                hasData.set(true);
                confirm = true;
                Scheduler scheduler = null;
                if ((scheduler = medipi.getScheduler()) != null) {
                    schedStartTime = scheduler.getCurrentScheduleStartTime();
                    schedExpireTime = scheduler.getCurrentScheduleExpiryTime();
                }
            }
        } else {
            deviceData = new ArrayList<>();
            hasData.set(false);
            temp.set(0D);
            lastMeasurementTime.set("");
            deviceData.clear();
            confirm = false;

        }
    }

    private ObservableList<String> getComboContent(int low, int high) {
        ObservableList<String> options = FXCollections.observableArrayList();
        options.add(String.valueOf("--.-"));
        int low10 = low * 10;
        int high10 = high * 10;
        for (int i = low10; i <= high10; i++) {
            double d = i / 10.0;
            options.add(String.valueOf(d));
        }
        return options;
    }

    /**
     * method to get the generic Type of the device
     *
     * @return generic type of device e.g. Oximeter
     */
    @Override
    public String getGenericDeviceDisplayName() {
        return GENERIC_DEVICE_NAME;
    }

    @Override
    public String getProfileId() {
        return PROFILEID;
    }

    // resets the device data
    @Override
    public void resetDevice() {
        deviceData = new ArrayList<>();
        hasData.set(false);
        temp.set(0D);
        lastMeasurementTime.set("");
        tempTens.reset();
        tempUnits.reset();
        tempTenths.reset();
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
    public void setData(ArrayList<ArrayList<String>> deviceData) {
        throw new UnsupportedOperationException("This method is not used as the class has no extensions");
    }

    private void displayData(double temp, Instant time) {
        Platform.runLater(() -> {
            this.temp.set(temp);
            this.lastMeasurementTime.set(Utilities.DISPLAY_DEVICE_FORMAT_LOCALTIME.format(time));
            resultsSummary.set(getGenericDeviceDisplayName() + " - "
                    + this.temp.getValue().toString() + (char) 176 + "C");
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
        dashComponent.addOverlay(tempDB, (char) 176 + "C");
        dashComponent.addOverlay(Color.LIGHTGREEN, hasDataProperty());
        return dashComponent.getTile();
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
}
