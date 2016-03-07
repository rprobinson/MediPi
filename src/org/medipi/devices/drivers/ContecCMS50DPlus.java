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
package org.medipi.devices.drivers;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import org.warlock.spine.logging.MediPiLogger;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.devices.Oximeter;

/**
 * An implementation of a specific device - ContecCMS50DPlus retrieving data
 * from the serial USB port.
 *
 * The USB serial interface implementation taking raw data from the device via
 * the USB and streaming it to the Device class. Uses RXTX library for serial
 * communication under the GNU Lesser General Public License
 *
 * @author rick@robinsonhq.com
 */
public class ContecCMS50DPlus extends Oximeter implements SerialPortEventListener {

    private static final String MAKE = "Contec";
    private static final String MODEL = "CMS50D+";
    private long nanoTime;
    private long epochTime;
    static CommPortIdentifier portId;
    static Enumeration portList;

    InputStream inputStream;
    SerialPort serialPort;
    boolean stopping = false;
    boolean fingerOut = false;
    boolean probeError = false;
    private String portName;
    BufferedReader dataReader;
    protected PipedOutputStream pos = new PipedOutputStream();
    OutputStream os;

    /**
     * Constructor for ContecCMS50DPlus
     */
    public ContecCMS50DPlus() {
    }

    @Override
    public String init() throws Exception {
        String deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + getClassTokenName();
        portName = MediPiProperties.getInstance().getProperties().getProperty(deviceNamespace + ".portname");
        if (portName == null || portName.trim().length() == 0) {
            String error = "Cannot find the portname for for driver for " + MAKE + " " + MODEL + " - for " + deviceNamespace + ".portname";
            MediPiLogger.getInstance().log(ContecCMS50DPlus.class.getName(), error);
            return error;
        }
        return super.init();
    }

    /**
     * method to get the Make and Model of the device
     *
     * @return make and model of device
     */
    @Override
    public String getName() {
        return MAKE + " " + MODEL;
    }

    /**
     * Opens the USB serial connection and prepares for serial data
     *
     * @return BufferedReader to set up the data stream
     */
    @Override
    public BufferedReader startSerialDevice() {
        stopping = false;
        portList = CommPortIdentifier.getPortIdentifiers();
        StringBuilder errorString = new StringBuilder();

        //Open the USB port 
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println(portId.getName());
                }
                if (portId.getName().equals(portName)) {
                    try {
                        serialPort = (SerialPort) portId.open("Medipi", 2000);
                    } catch (PortInUseException e) {
                        errorString.append("Port:").append(portName).append(" is already in use by ").append(portId.getCurrentOwner()).append("\n");
                        if (medipi.getDebugMode() == MediPi.DEBUG) {
                            System.out.println(e);
                        }
                    }
                    try {
                        inputStream = serialPort.getInputStream();
                    } catch (IOException e) {
                        errorString.append("Port:").append(portName).append("- can't get serial data stream from device\n");
                        if (medipi.getDebugMode() == MediPi.DEBUG) {
                            System.out.println(e);
                        }
                    }
                    try {
                        serialPort.addEventListener(this);
                    } catch (TooManyListenersException e) {
                        errorString.append("Port:").append(portName).append("- too many listeners\n");
                        if (medipi.getDebugMode() == MediPi.DEBUG) {
                            System.out.println(e);
                        }
                    }
                    serialPort.notifyOnDataAvailable(true);
                    try {
                        serialPort.setSerialPortParams(19200,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_ODD);
                        nanoTime = System.nanoTime();
                        epochTime = System.currentTimeMillis();
                        pos = new PipedOutputStream();
                        PipedInputStream pis = new PipedInputStream(pos);
                        dataReader = new BufferedReader(new InputStreamReader(pis));
                        return dataReader;
                    } catch (UnsupportedCommOperationException | IOException e) {
                        errorString.append("Port:").append(portName).append("- device driver doesn't allow the serial port parameters\n");
                        if (medipi.getDebugMode() == MediPi.DEBUG) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }
        stopSerialDevice();
        if (errorString.length() == 0) {
            errorString.append("Device not accessible - is it attached/in range?");
        }
        return null;
    }

    /**
     * Stops the USB serial port and resets the listeners
     *
     * @return boolean value of success of the connection closing
     */
    @Override
    public boolean stopSerialDevice() {
        stopping = true;
        if (serialPort != null) {
            serialPort.close();
            serialPort.removeEventListener();
            try {
                dataReader.close();
            } catch (IOException ex) {
                MediPiLogger.getInstance().log(ContecCMS50DPlus.class.getName() + ".stopserialdevice-datareader", ex);
            }
            try {
                pos.close();
            } catch (IOException ex) {
                MediPiLogger.getInstance().log(ContecCMS50DPlus.class.getName() + ".stopserialdevice-pos", ex);
            }
        }
        return stopping;
    }

    /**
     * For each serial event - digest the byte data and place into variables.
     * unused variables are commented out
     *
     * @param event
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Break interrupt.");
                }
            case SerialPortEvent.OE:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Overrun error.");
                }
            case SerialPortEvent.FE:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Framing error.");
                }
            case SerialPortEvent.PE:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Parity error.");
                }
            case SerialPortEvent.CD:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Carrier detect.");
                }
            case SerialPortEvent.CTS:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Clear to send.");
                }
            case SerialPortEvent.DSR:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Data set ready.");
                }
            case SerialPortEvent.RI:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Ring indicator.");
                }
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Output buffer is empty.");
                }
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                byte[] buffer = new byte[10];
                int idx = 0;
                byte[] packet = new byte[5];
                try {
                    while (inputStream.available() > 10) {
                        inputStream.read(buffer);
                        for (byte b : buffer) {
                            //System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                            if ((b & 128) != 0) {
                                //System.out.println((b&0x80)+" "+idx);
                                if (idx == 5 && (packet[0] & 128) != 0) {
                                    String[] line = new String[3];
                                    // 1st byte
                                    // signalStrength
                                    //output[0] = String.valueOf(packet[0] & 0x0f);
                                    // fingerOut
                                    fingerOut = ((packet[0] & 16) != 0);
                                    // droppingSpO2
                                    // output[2] = String.valueOf((packet[0] & 0x20) != 0);
                                    // beep
                                    // output[3] = String.valueOf((packet[0] & 0x40) != 0);
                                    // # 2nd byte
                                    // pulseWaveform
                                    line[2] = String.valueOf(packet[1]);
                                    // # 3rd byte
                                    // barGraph
                                    // output[5] = String.valueOf(packet[2] & 0x0f);
                                    // probeError
                                    probeError = ((packet[2] & 16) != 0);
                                    // searching
                                    // output[7] = String.valueOf((packet[2] & 0x20) != 0);
                                    // pulseRate
                                    // output[8] = String.valueOf(((packet[2] & 0x40) << 1));
                                    // # 4th byte
                                    // pulseRate
                                    line[0] = String.valueOf(packet[3] & 127);
                                    //5th byte
                                    //bloodSpO2
                                    line[1] = String.valueOf(packet[4] & 127);
                                    if (fingerOut) {
                                        if (medipi.getDebugMode() == MediPi.DEBUG) {
                                            System.out.println("finger out");
                                        }
                                        if (probeError) {
                                            if (medipi.getDebugMode() == MediPi.DEBUG) {
                                                System.out.println("probe error");
                                            }
                                            task.cancel();
                                            stopSerialDevice();
                                        }
                                    }
                                    if (!stopping) {
                                        StringBuilder sb = new StringBuilder(String.valueOf(Math.round(System.nanoTime() / 1000000d) - Math.round(nanoTime / 1000000d) + epochTime));
                                        sb.append(separator);
                                        sb.append(line[0]);
                                        sb.append(separator);
                                        sb.append(line[1]);
                                        sb.append(separator);
                                        sb.append(line[2]);
                                        sb.append("\n");
                                        pos.write(sb.toString().getBytes());
                                        pos.flush();
                                        if (medipi.getDebugMode() == MediPi.DEBUG) {
                                            System.out.print(String.valueOf(fingerOut) + String.valueOf(probeError) + sb.toString());
                                        }
                                    }
                                }
                                packet = new byte[5];
                                idx = 0;
                            }
                            if (idx < 5) {
                                packet[idx] = b;
                                idx++;
                            }
                        }
                    }
                } catch (IOException e) {
                    stopSerialDevice();
                    MediPiMessageBox.getInstance().makeErrorMessage("Device no longer accessible", e, Thread.currentThread());
                    if (medipi.getDebugMode() == MediPi.DEBUG) {
                        System.out.println(e);
                    }
                }
                break;
        }
    }


}
