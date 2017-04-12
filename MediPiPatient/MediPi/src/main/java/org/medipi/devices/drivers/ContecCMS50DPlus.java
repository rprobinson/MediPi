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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import org.medipi.logging.MediPiLogger;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.Oximeter;
import org.medipi.devices.drivers.domain.CMS50DPlusMeasurement;
import org.medipi.utilities.Utilities;

/**
 * An implementation of a specific device - ContecCMS50DPlus retrieving data
 * from the serial USB port.
 *
 * The USB serial interface implementation taking raw data from the device via
 * the USB and streaming it to the Device class. Uses RXTX library for serial
 * communication under the GNU Lesser General Public License
 *
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 * @author rick@robinsonhq.com
 */
public class ContecCMS50DPlus extends Oximeter {

    private static final String MAKE = "Contec";
    private static final String MODEL = "CMS50D+";
    private static final String DISPLAYNAME = "Contec CMS50D+ Finger Pulse Oximeter";
    private static final String RECORD = "Record";
    private static final String STOP = "Stop";
    private CMS50DPlusMeasurement measurement;
    private int spO2DataCounter = 1;
    private int pulseRateDataCounter = 1;
    private int sumPulseRate = 0;
    private int sumSpO2Rate = 0;
    private int meanPulse = 0;
    private int meanSpO2 = 0;
    private Instant endTime;
    private boolean transmitAverages = true;
    private ArrayList<ArrayList<String>> verboseDeviceData = new ArrayList<>();
    /**
     * The main task. It is exposed to allow the concrete driver class to
     * control the main task. This is due to the fact that the device is
     * serially pushing data to this class, so that when it stops it needs
     * access to the task itself.
     */
    protected Task task;

    /**
     * Constructor for ContecCMS50DPlus
     */
    public ContecCMS50DPlus() {
    }

    @Override
    public String init() throws Exception {
        String deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        String portName = medipi.getProperties().getProperty(deviceNamespace + ".portname");
        if (portName == null || portName.trim().length() == 0) {
            String error = "Cannot find the portname for for driver for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".portname";
            MediPiLogger.getInstance().log(CMS50DPlusMeasurement.class.getName(), error);
            return error;
        }
        // Decide whether to transmit the full set of streamed data points or just the averages
        String b = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + getClassTokenName() + ".data.transmitaverages");
        if (b == null || b.trim().length() == 0) {
            transmitAverages = true;
        } else {
            transmitAverages = !b.toLowerCase().startsWith("n");
        }

        initialButtonText = RECORD;
        initialGraphic = medipi.utils.getImageView("medipi.images.record", 20, 20);

        columns.add("iso8601time");
        columns.add("pulse");
        columns.add("spo2");
        if (!transmitAverages) {
            columns.add("wave");
        }
        format.add("DATE");
        format.add("INTEGER");
        format.add("INTEGER");
        if (!transmitAverages) {
            format.add("DOUBLE");
        }
        units.add("NONE");
        units.add("BPM");
        units.add("%");
        if (!transmitAverages) {
            units.add("NONE");
        }
        measurement = new CMS50DPlusMeasurement(portName, medipi.getDataSeparator());
        return super.init();
    }

    // Method to handle the recording of the serial device data
    @Override
    protected void downloadData() {
        if (task == null || !task.isRunning()) {
            resetDevice();
            processData();
        } else {
            Platform.runLater(() -> {
                task.cancel();
                if (measurement.stopSerialDevice()) {
                } else {
                    // serial device cant stop
                }
            });

        }
    }

    private void processData() {
        task = new Task<String>() {
            ArrayList<ArrayList<String>> data = new ArrayList<>();

            @Override
            protected String call() throws Exception {
                try {
                    updateValue("Is the device plugged in?");
                    BufferedReader stdInput = measurement.startSerialDevice(task);
                    if (stdInput != null) {
                        updateValue("No readings taken");
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
                                String[] line = readData.split(Pattern.quote(separator));
                                ArrayList<String> lineList = new ArrayList<>(Arrays.asList(line));
                                ZonedDateTime zdt = ZonedDateTime.parse(line[0], Utilities.ISO8601FORMATDATEMILLI_UTC);
                                final Instant timestamp = zdt.toInstant();
                                final int pulse = Integer.parseInt(line[1]);
                                final int spO2 = Integer.parseInt(line[2]);
                                if (transmitAverages) {
                                    lineList.remove(3);
                                }
                                // add the data to the data array
                                if (pulse > 0 && spO2 > 0) {
                                    updateValue("INPROGRESS");
                                    verboseDeviceData.add(lineList);
                                    // add the data to the screen display - this might be a graph/table 
                                    // or just a simple result of the last measure
                                    averageAndDisplay(timestamp, pulse, spO2);
                                }

                            }
                        }
                    }
                    return "Is the device plugged in?";
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
                    makeDataAvailableForDownload();
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
                MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
            }

            @Override
            // Also counts as a positive outcome as the straeming of data is stopped by cancelling
            protected void cancelled() {
                super.succeeded();
                if (getValue().equals("INPROGRESS")) {
                    makeDataAvailableForDownload();
                } else {
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
                }
            }

            private void makeDataAvailableForDownload() {
                if (!verboseDeviceData.isEmpty()) {
                    if (transmitAverages) {
                        ArrayList<String> al = new ArrayList<>();
                        al.add(endTime.toString());
                        al.add(String.valueOf(meanPulse));
                        al.add(String.valueOf(meanSpO2));
                        data.add(al);
                    } else {
                        data = verboseDeviceData;
                    }
                    setData(data);
                }
            }
        };

        // Set up the bindings to control the UI elements during the running of the task
        actionButton.textProperty()
                .bind(
                        Bindings.when(task.runningProperty())
                        .then(STOP)
                        .otherwise(RECORD)
                );
        actionButton.graphicProperty()
                .bind(
                        Bindings.when(task.runningProperty())
                        .then(medipi.utils.getImageView("medipi.images.stop", 20, 20))
                        .otherwise(medipi.utils.getImageView("medipi.images.record", 20, 20))
                );
        button3.disableProperty()
                .bind(
                        Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution))
                        .then(true)
                        .otherwise(false)
                );
        button1.disableProperty()
                .bind(
                        Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution))
                        .then(true)
                        .otherwise(false)
                );

        new Thread(task)
                .start();
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

    // initialises the device window and the data behind it
    @Override
    public void resetDevice() {
        spO2DataCounter = 1;
        pulseRateDataCounter = 1;
        sumPulseRate = 0;
        sumSpO2Rate = 0;
        endTime = null;
        meanPulse = 0;
        meanSpO2 = 0;
        super.resetDevice();
    }

    /**
     * Add data to the graph
     *
     * @param time as UNIX epoch time format
     * @param pulseRate in BPM
     * @param spO2 in %
     * @param waveForm
     */
    public void averageAndDisplay(Instant time, int pulseRate, int spO2) {

        endTime = time;
        if (pulseRate != 0) {
            sumPulseRate = sumPulseRate + pulseRate;
            meanPulse = sumPulseRate / pulseRateDataCounter;
            pulseRateDataCounter++;
        }
        if (spO2 != 0) {
            sumSpO2Rate = sumSpO2Rate + spO2;
            meanSpO2 = sumSpO2Rate / spO2DataCounter;
            spO2DataCounter++;
        }
        displayData(endTime, meanPulse, meanSpO2);
    }
}
