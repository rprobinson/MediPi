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
package org.medipi.devices.drivers.service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import org.medipi.MediPiMessageBox;
import org.medipi.devices.Element;
import org.medipi.devices.drivers.DeviceServiceMeasurement;
import org.medipi.devices.drivers.domain.ServiceMeasurement;

/**
 * Class to access a Bluetooth Serial Port Device asynchronously to take all
 * available data
 *
 * The class notifies the calling device class once it has completed. important:
 * It is able to update the timestamp and mode on the device - it is too closely
 * paired to the Nonin 9560 device to be generic. Should be refactored
 *
 * @author rick@robinsonhq.com
 */
public class BTStreamEXT extends BluetoothService implements Runnable {

    private final Element element;
    private String url;
    private byte etx;
    private ArrayList<byte[]> results = null;
    private boolean keepLookingForBTConnection = true;
    private DeviceServiceMeasurement deviceServiceMeasurement;
    private ServiceMeasurement serviceMeasurement;

    public BTStreamEXT(Element e) throws Exception {
        element = e;
        deviceServiceMeasurement = (DeviceServiceMeasurement) e;

    }

    public void getStreamWithETX(String url, ServiceMeasurement sm) throws IOException {
        this.url = url;
        this.serviceMeasurement = sm;

        new Thread(this).start();
    }

    public ArrayList<byte[]> getResults() {
        return results;
    }

    private static byte[] convertBytes(ArrayList<Byte> b) {
        byte[] ret = new byte[b.size()];
        Iterator<Byte> iterator = b.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next();
        }
        return ret;
    }

    @Override
    public void run() {
        ArrayList<Byte> output = new ArrayList<>();
        Instant timer = Instant.now();
        boolean firstLoop = true;
        synchronized (element) {
            try {

                element.setButton2Name(searchingMessage);
                while (keepLookingForBTConnection) {
                    try {
                        if (timer.plusSeconds(btConnectionAttemptPeriod).isBefore(Instant.now())) {
                            break;
                        }
                        c = (StreamConnection) Connector.open(url);
                        if (firstLoop) {
                            MediPiMessageBox.getInstance().makeErrorMessage("Oximeter must be \"OFF\" when the start button is pressed", null);
                            break;
                        }
                        is = c.openInputStream();
                        os = c.openOutputStream();
                        keepLookingForBTConnection = false;
                        element.setButton2Name(connectionMessage);
                        byte b;

                        serviceMeasurement = deviceServiceMeasurement.getNewServiceMeasurement();

                        // First need to check that the device is in the correct data format - i.e. Data Format 2 - refer to nonin spec
                        //ensureCorrectDataFormat(os);
                        // Next need to update the time if necessary
                        ensureCorrectTime(os);
                        boolean complete = false;

                        try {
                            while ((b = (byte) is.read()) != -1) {
                                element.setButton2Name(downloadingMessage);
                                System.out.println("byte:" + b);
                                complete = serviceMeasurement.parse(b);
                                if (complete) {

                                    boolean lastMeasurement = serviceMeasurement.translate();
                                    deviceServiceMeasurement.addMeasurement(serviceMeasurement);
                                    if (lastMeasurement) {
                                        break;
                                    } else {
                                        serviceMeasurement = deviceServiceMeasurement.getNewServiceMeasurement();
                                        complete = false;
                                    }
//                                    element.notifyAll();
                                }
                            }
                        } catch (Exception e) {
                            MediPiMessageBox.getInstance().makeErrorMessage("Error in reading from the device: ", e);
                        }
                    } catch (IOException e) {
                        firstLoop = false;
                        element.setButton2Name("Insert Finger");
                        System.out.println("attempt connection: " + Instant.now() + e.getMessage());
                        // do nothing as this is thrown when there is an attempted connection and the BT device is switched off
                    }
                }
                // reset keepLookingForBTConnection
                keepLookingForBTConnection = true;
            } finally {
                System.out.println("finallynotify@ " + Instant.now());
                element.notifyAll();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        //do nothing as if the connection is null it doesnt need closing
                    }
                }
                if (c != null) {
                    try {
                        c.close();
                    } catch (IOException ex) {
                        //do nothing as if the connection is null it doesnt need closing
                    }
                }
            }
        }

    }

    public void StopLookingForBTConnection() {
        keepLookingForBTConnection = false;
    }

    //This methos sets the device to be in data format 13 without ATR (attempt to reconnect) which is required for best operation
    private void ensureCorrectDataFormat(OutputStream os) throws IOException {
        byte[] dataFormat2 = new byte[]{
            (byte) 0x02, //STX
            (byte) 0x70, //Op Code
            (byte) 0x04, //Data Size
            (byte) 0x02, //Data Type
            (byte) 0x0D, //Data Format
            (byte) 0x00, //Options
            (byte) 0x83, //CheckSum
            (byte) 0x03 //ETX
        };
        os.write(dataFormat2);
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
