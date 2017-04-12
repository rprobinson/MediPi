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

import java.time.Instant;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.Oximeter;
import org.medipi.devices.drivers.domain.N9560Measurement;
import org.medipi.devices.drivers.domain.ServiceMeasurement;
import org.medipi.devices.drivers.service.BTStreamEXT;
import org.medipi.devices.drivers.service.BluetoothPropertiesDO;
import org.medipi.devices.drivers.service.BluetoothPropertiesService;

/**
 * A concrete implementation of a specific device - Nonin 9560 PulseOx Pulse
 * Oximeter
 *
 * The class uses Nonin's freely available/simple serial port protocol. The
 * device is required to be in the data format 13 with ATR disabled - see protocol
 *
 * This class defines the device which is to be connected, defines the data to
 * be collected and passes this forward to the generic device class
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public class Nonin9560 extends Oximeter implements DeviceServiceMeasurement {

    private static final String MAKE = "Nonin";
    private static final String MODEL = "9560";
    private static final String DISPLAYNAME = "Nonin 9560 Finger Pulse Oximeter";
    private static final String STARTBUTTONTEXT = "Start";
    private static final String SEARCHING_MESSAGE = "Insert Finger";
    private static final String CONNECTING_MESSAGE = "Connecting";
    private static final String DOWNLOADING_MESSAGE = "Downloading data";
    private BluetoothPropertiesService bluetoothPropertiesService;
    private ImageView graphic;
    private ArrayList<ArrayList<String>> data = new ArrayList<>();

    /**
     * Constructor for BeurerBF480
     */
    public Nonin9560() {
    }

    // initialise and load the configuration data
    @Override
    public String init() throws Exception {
        String deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        graphic = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        graphic.setRotate(90);
        initialGraphic = graphic;
        initialButtonText = STARTBUTTONTEXT;
        bluetoothPropertiesService = BluetoothPropertiesService.getInstance();
        bluetoothPropertiesService.register(Nonin9560.this);

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

    // initialises the device window and the data behind it
    @Override
    public void resetDevice() {
        columns.clear();
        format.clear();
        units.clear();
        data.clear();
        super.resetDevice();
    }

    /**
     * Method to to download the data from the device. This data is digested by
     * the generic device class
     */
    @Override
    protected void downloadData() {
        resetDevice();
        try {
            Task<String> task = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    String operationStatus = "Unknown error connecting to " + getSpecificDeviceDisplayName();
                    try {
                        // input datastream from the device driver
                        BTStreamEXT bluetoothService = new BTStreamEXT(Nonin9560.this);
//                        bluetoothService.setOriginalMessage(STARTBUTTONTEXT);
//                        bluetoothService.setSearchingMessage(SEARCHING_MESSAGE);
//                        bluetoothService.setConnectionMessage(CONNECTING_MESSAGE);
//                        bluetoothService.setDownloadingMessage(DOWNLOADING_MESSAGE);
                        BluetoothPropertiesDO btp = bluetoothPropertiesService.getBluetoothPropertyDOByMedipiDeviceName(getClassTokenName());
                        //This is a blocking method until data arrives (or not)
                        bluetoothService.getStreamWithETX(btp.getUrl(), getNewServiceMeasurement());
                        try {
                            synchronized (Nonin9560.this) {
                                System.out.println("wait@ " + Instant.now());
                                Nonin9560.this.wait();
//                                bluetoothService.closeConnection();
                            }
                            // allow enough time for the small amount of data to be passed
                        } catch (InterruptedException e) {
                        }
                        System.out.println("continue@ " + Instant.now());

                        operationStatus = "SUCCESS";

                    } catch (Exception ex) {
                        operationStatus = ex.getLocalizedMessage();
                        return operationStatus;
                    } finally {
                        setButton2Name(STARTBUTTONTEXT, graphic);
                    }
                    return operationStatus;
                }

                // the measure of completion and success is returning "SUCCESS"
                // all other outcomes indicate failure and pipe the failure
                // reason given from the device to the error message box
                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (getValue().equals("SUCCESS")) {
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
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
                }

                @Override
                protected void cancelled() {
                    super.failed();
                    MediPiMessageBox.getInstance().makeErrorMessage(getValue(), null);
                }
            };

            // Disabling Button control
            actionButton.disableProperty().bind(task.runningProperty());
            progressIndicator.visibleProperty().bind(task.runningProperty());
            button3.disableProperty().bind(Bindings.when(task.runningProperty().and(this.isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
            button1.disableProperty().bind(Bindings.when(task.runningProperty().and(this.isThisElementPartOfAScheduleExecution)).then(true).otherwise(false));
            new Thread(task).start();
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Download of data unsuccessful", ex);
        }
    }

    @Override
    public synchronized ServiceMeasurement getNewServiceMeasurement() {
        return new N9560Measurement();
    }

    @Override
    public synchronized void addMeasurement(ServiceMeasurement serviceMeasurement) {
        N9560Measurement measurement = (N9560Measurement) serviceMeasurement;
        System.out.println("pulse:" + measurement.getPulse() + " spo2:" + measurement.getSpO2());

        if (columns.isEmpty()) {
            this.columns = measurement.getColumns();
        }
        if (format.isEmpty()) {
            this.format = measurement.getFormat();
        }
        if (units.isEmpty()) {
            this.units = measurement.getUnits();
        }
        this.data.add(measurement.getDeviceData());
    }
}
