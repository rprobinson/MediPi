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

import extfx.scene.chart.DateAxis;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.utilities.Utilities;

/**
 * Class to display and handle the functionality for a generic Pulse Oximeter
 * Medical Device.
 *
 * Dependent on the view mode the UI displays either a graphical representation
 * of the results (pulse rate, oxygen saturation and pulse waveform) or a visual
 * guide to using the device. When data is taken from the device metadata is
 * added to identify it. The large type result display shows an average heart
 * rate and SpO2 level over the period of measurement. The data is received
 * serially "in real time" and not in the "download" paradigm
 *
 * This class expects each line of data coming in from the driver to conform to
 * the following format:
 *
 * Date in UNIX epoch time, heart rate in BPM, SpO2 in %, heart waveform range
 * 0-99. All individual data points within a line are separated using the
 * configurable separator
 *
 * JavaFX has no implementation for a Date axis in its graphs so the extFX
 * library has been used (Published under the MIT OSS licence. This may need to
 * be altered/changed (https://bitbucket.org/sco0ter/extfx)
 *
 * TODO: This class expect measurements for Heart rate , SpO2 and heart waveform
 * which for any specific scale might not be appropriate. The fact that the
 * large results box is showing averaged results is not being displayed. The
 * start and finish times are recorded internally but not displayed
 *
 * @author rick@robinsonhq.com
 */
public abstract class Oximeter extends Device {

    private final String DEVICE_TYPE = "Finger Oximeter";
    private static final String PROFILEID = "urn:nhs-en:profile:Oximeter";
    private XYChart.Series pulseSeries;
    private DateAxis xAxis;
    private NumberAxis yAxis;
    private Button recordButton;
    private ArrayList<String[]> deviceData = new ArrayList<>();
    // property to indicate whether data has bee recorded for this device
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);
    private VBox oxiWindow;
    private LineChart<Date, Number> lineChart;
    //defining a series
    private XYChart.Series spO2Series;
    private XYChart.Series waveFormSeries;
    private int spO2DataCounter = 1;
    private int pulseRateDataCounter = 1;
    private int sumPulseRate = 0;
    private int sumSpO2Rate = 0;
    private int maxPulse = 0;
    private int minPulse = 999;
    private int meanPulse = 0;
    private Label maxPulseTF;
    private Label minPulseTF;
    private Label meanPulseTF;
    private Label meanPulseDB;
    private Label currentPulseTF;
    private int maxSpO2 = 0;
    private int minSpO2 = 100;
    private int meanSpO2 = 0;
    private Label maxSpO2TF;
    private Label minSpO2TF;
    private Label meanSpO2TF;
    private Label meanSpO2DB;
    private Label currentSpO2TF;
    private Label startTimeTF;
    private Label endTimeTF;
    private VBox resultsVBox;

    private Node dataBox;
    private Date downloadTimestamp = null;

    /**
     * This is the data separator from the MediPi.properties file
     *
     */
    protected String separator;

    /**
     * The main task. It is exposed to allow the concrete driver class to
     * control the main task. This is due to the fact that the device is
     * serially pushing data to this class, so that when it stops it needs
     * access to the task itself.
     */
    protected Task task;


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
        recordButton = new Button("Record");
        recordButton.setId("button-record");

        oxiWindow = new VBox();
        oxiWindow.setPadding(new Insets(0, 5, 0, 5));
        oxiWindow.setSpacing(5);
        oxiWindow.setMinSize(800, 350);
        oxiWindow.setMaxSize(800, 350);

        //Decide whether to show basic or advanced view
        if (medipi.isBasicDataView()) {
            Guide guide = new Guide(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName);
            dataBox = guide.getGuide();
        } else {
            //creating the chart
            yAxis = new NumberAxis(0, 120, 10);
            xAxis = new DateAxis();
            Date d = new Date();
            StringConverter sc = new DateStringConverter(Utilities.DISPLAY_OXIMETER_TIME_FORMAT);
            xAxis.setTickLabelFormatter(sc);
            xAxis.setLabel("Time");
            lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Finger Oximeter");
            lineChart.setCreateSymbols(false);
            lineChart.setMinWidth(600);
            dataBox = lineChart;
        }
        startTimeTF = new Label("-");
        startTimeTF.setMaxWidth(200);
        startTimeTF.setMinWidth(200);
        endTimeTF = new Label("-");
        endTimeTF.setMaxWidth(200);
        endTimeTF.setMinWidth(200);

        //defining a series
        maxPulseTF = new Label("-");
        maxPulseTF.setMaxWidth(50);
        maxPulseTF.setMinWidth(50);
        minPulseTF = new Label("-");
        minPulseTF.setMaxWidth(50);
        minPulseTF.setMinWidth(50);
        meanPulseDB = new Label("");        // Setup download button action to run in its own thread

        meanPulseTF = new Label("--");
        meanPulseTF.setId("resultstext");

        currentPulseTF = new Label("-");
        currentPulseTF.setMaxWidth(50);
        currentPulseTF.setMinWidth(50);
        maxSpO2TF = new Label("-");
        maxSpO2TF.setMaxWidth(50);
        maxSpO2TF.setMinWidth(50);
        minSpO2TF = new Label("-");
        minSpO2TF.setMaxWidth(50);
        minSpO2TF.setMinWidth(50);
        meanSpO2DB = new Label("-");
        meanSpO2TF = new Label("--");
        meanSpO2TF.setId("resultstext");
        currentSpO2TF = new Label("-");
        currentSpO2TF.setMaxWidth(50);
        currentSpO2TF.setMinWidth(50);
        resetDevice();

        // create the large result box for the average value of the measurements
        // taken over the period
        // heart rate reading
        HBox pulseRateHbox = new HBox();
        pulseRateHbox.setAlignment(Pos.CENTER);
        Label bpm = new Label("BPM");
        bpm.setId("resultstext");
        bpm.setStyle("-fx-font-size:10px;");
        pulseRateHbox.getChildren().addAll(
                meanPulseTF,
                bpm
        );
        // SpO2 reading
        HBox spO2RateHbox = new HBox();
        spO2RateHbox.setAlignment(Pos.CENTER);
        Label spo2 = new Label("SpO2");
        spo2.setId("resultstext");
        spo2.setStyle("-fx-font-size:10px;");
        spO2RateHbox.getChildren().addAll(
                meanSpO2TF,
                spo2
        );
        HBox timeHbox = new HBox();
        timeHbox.setSpacing(5);
        timeHbox.setAlignment(Pos.CENTER);
        timeHbox.getChildren().addAll(
                new Label("Time Started:"),
                startTimeTF,
                new Label("Time Finished:"),
                endTimeTF);
        resultsVBox = new VBox();
        resultsVBox.setPrefWidth(200);
        resultsVBox.setId("resultsbox");
        resultsVBox.getChildren().addAll(
                pulseRateHbox,
                spO2RateHbox
        );
        //create the main window HBox
        HBox dataHBox = new HBox();
        dataHBox.getChildren().addAll(
                dataBox,
                resultsVBox
        );
        oxiWindow.getChildren().addAll(
                dataHBox,
                new Separator(Orientation.HORIZONTAL)
        );
        // set main Element window
        window.setCenter(oxiWindow);
        setRightButton(recordButton);

        // Setup reccord button action to start or stop the main task
        recordButton.setOnAction((ActionEvent t) -> {
            if (recordButton.getText().equals("Record")) {
                resetDevice();
                record();
            } else {
                Platform.runLater(() -> {
                    task.cancel();
                    if (stopSerialDevice()) {
                    } else {
                        // serial device cant stop
                    }
                });
                
            }
        });

        // successful initiation of the this class results in a null return
        return null;
    }

    /**
     * method to get the generic Type of the device
     *
     * @return generic type of device e.g. Oximeter
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
            lineChart.getData().removeAll(pulseSeries, spO2Series, waveFormSeries);
            pulseSeries = new XYChart.Series();
            spO2Series = new XYChart.Series();
            waveFormSeries = new XYChart.Series();
            pulseSeries.setName("PulseRate (BPM)");
            spO2Series.setName("SpO2 (%)");
            waveFormSeries.setName("Pulse WaveForm");
            lineChart.getData().add(pulseSeries);
            lineChart.getData().add(spO2Series);
            lineChart.getData().add(waveFormSeries);
        }
        spO2DataCounter = 1;
        pulseRateDataCounter = 1;
        sumPulseRate = 0;
        sumSpO2Rate = 0;
        maxPulse = 0;
        minPulse = 999;
        meanPulse = 0;
        maxSpO2 = 0;
        minSpO2 = 100;
        meanSpO2 = 0;
        maxPulseTF.setText("-");
        maxSpO2TF.setText("-");
        minPulseTF.setText("-");
        minSpO2TF.setText("-");
        meanPulseTF.setText("--");
        meanPulseDB.setText("");
        meanSpO2TF.setText("--");
        meanSpO2DB.setText("");
        currentPulseTF.setText("-");
        currentSpO2TF.setText("-");
        startTimeTF.setText("-");
        endTimeTF.setText("-");

    }

    // Method to handle the recording of the serial device data
    private void record() {
        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                try {
                    updateValue("NOTSTARTED");
                    BufferedReader stdInput = startSerialDevice();
                    if (stdInput != null) {
                        String readData = new String();
                        while (true) {
                            try {
                                readData = stdInput.readLine();
                            } catch (IOException i) {
                                // This happens when the connection is dropped when stop is pressed
                                break;
                            }
                            if (readData == null) {
                                return "no data from device";
                            } else if (readData.equals("-1")) {
                                return "end of data stream from oximeter";
                            } else {
                                updateValue("INPROGRESS");
                                String[] line = readData.split(Pattern.quote(separator));
                                final long timestamp = Long.parseLong(line[0]);
                                final int pulse = Integer.parseInt(line[1]);
                                final int spO2 = Integer.parseInt(line[2]);
                                final int wave = Integer.parseInt(line[3]);
                                // add the data to the data array
                                deviceData.add(line);
                                // add the data to the screen display - this might be a graph/table 
                                // or just a simple result of the last measure
                                Platform.runLater(() -> {
                                    addDataPoint(timestamp, pulse, spO2, wave);
                                });

                            }
                        }
                    }
                    return "Is the device plugged in/within range?";
                } catch (Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            // the measure of completion and success is returning "SUCCESS"
            // all other outcomes indicate failure and pipe the failure 
            // reason given from the device to the error message box
            @Override
            protected void succeeded() {
                super.succeeded();
                if (getValue().equals("INPROGRESS")) {
                    if (!deviceData.isEmpty()) {
                        hasData.set(true);
                        // take the time of downloading the data
                        downloadTimestamp = new Date();
                    }
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
            // Also counts as a positive outcome as the straeming of data is stopped by cancelling
            protected void cancelled() {
                super.succeeded();
                if (getValue().equals("INPROGRESS")) {
                    if (!deviceData.isEmpty()) {
                        hasData.set(true);
                        // take the time of downloading the data
                        downloadTimestamp = new Date();
                    }
                } else {
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null, Thread.currentThread());
                }
            }
        };

        // Set up the bindings to control the UI elements during the running of the task
        recordButton.textProperty().bind(
                Bindings.when(task.runningProperty())
                .then("Stop")
                .otherwise("Record")
        );
        leftButton.disableProperty().bind(
                Bindings.when(task.runningProperty().and(isSchedule))
                .then(true)
                .otherwise(false)
        );

        new Thread(task).start();
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
        sb.append("metadata:format:").append("time,pulse,sp02,wave").append("\n");
        // Add Downloaded data
        for (String[] s : deviceData) {
            sb.append(s[0]);
            sb.append(separator);
            sb.append(s[1]);
            sb.append(separator);
            sb.append(s[2]);
            sb.append(separator);
            sb.append(s[3]);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Add data to the graph
     *
     * @param time as UNIX epoch time format
     * @param pulseRate in BPM
     * @param spO2 in %
     * @param waveForm
     */
    public void addDataPoint(long time, int pulseRate, int spO2, int waveForm) {

        if (startTimeTF.getText().equals("-")) {
            startTimeTF.setText(Utilities.DISPLAY_FORMAT.format(new Date(time)));
        }
        endTimeTF.setText(Utilities.DISPLAY_FORMAT.format(new Date(time)));
        if (!medipi.isBasicDataView()) {
            XYChart.Data<Date, Number> pulseXYData = new XYChart.Data<>(new Date(time), pulseRate);
            pulseSeries.getData().add(pulseXYData);
            XYChart.Data<Date, Number> spO2XYData = new XYChart.Data<>(new Date(time), spO2);
            spO2Series.getData().add(spO2XYData);
            XYChart.Data<Date, Number> waveFormXYData = new XYChart.Data<>(new Date(time), waveForm);
            waveFormSeries.getData().add(waveFormXYData);
        }
        if (pulseRate != 0) {
            if (pulseRate > maxPulse) {
                maxPulse = pulseRate;
                maxPulseTF.setText(String.valueOf(maxPulse));
            }
            if (pulseRate != 0 && pulseRate < minPulse) {
                minPulse = pulseRate;
                minPulseTF.setText(String.valueOf(minPulse));
            }
            sumPulseRate = sumPulseRate + pulseRate;
            meanPulse = sumPulseRate / pulseRateDataCounter;
            meanPulseTF.setText(String.valueOf(meanPulse));
            meanPulseDB.setText(String.valueOf(meanPulse));
            pulseRateDataCounter++;
        }
        currentPulseTF.setText(String.valueOf(pulseRate));
        if (spO2 != 0) {
            if (spO2 > maxSpO2) {
                maxSpO2 = spO2;
                maxSpO2TF.setText(String.valueOf(maxSpO2));
            }
            if (spO2 != 0 && spO2 < minSpO2) {
                minSpO2 = spO2;
                minSpO2TF.setText(String.valueOf(minSpO2));
            }
            sumSpO2Rate = sumSpO2Rate + spO2;
            meanSpO2 = sumSpO2Rate / spO2DataCounter;
            meanSpO2TF.setText(String.valueOf(meanSpO2));
            meanSpO2DB.setText(String.valueOf(meanSpO2));
            spO2DataCounter++;
        }
        currentSpO2TF.setText(String.valueOf(spO2));
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
        dashComponent.addOverlay(meanPulseDB, "BPM");
        dashComponent.addOverlay(meanSpO2DB, "SpO2");
        return dashComponent.getTile();
    }

    /**
     * Opens the USB serial connection and prepares for serial data
     *
     * @return bufferedReader to pass data serially with the device class
     */
    public abstract BufferedReader startSerialDevice();

    /**
     * Stops the USB serial port and resets the listeners
     *
     * @return boolean value of success of the connection closing
     */
    public abstract boolean stopSerialDevice();

}
