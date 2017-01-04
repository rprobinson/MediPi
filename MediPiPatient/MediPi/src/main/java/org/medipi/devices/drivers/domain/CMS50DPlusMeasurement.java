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
package org.medipi.devices.drivers.domain;

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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Instant;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import javafx.concurrent.Task;
import org.medipi.logging.MediPiLogger;
import org.medipi.MediPiMessageBox;
import org.medipi.utilities.Utilities;

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
public class CMS50DPlusMeasurement implements SerialPortEventListener {

    private long nanoTime;
    private long epochTimeAtStart;
    static CommPortIdentifier portId;
    static Enumeration portList;

    private InputStream inputStream;
    private SerialPort serialPort;
    boolean stopping = false;
    boolean fingerOut = false;
    boolean probeError = false;
    private final String portName;
    private BufferedReader dataReader;
    protected PipedOutputStream pos = new PipedOutputStream();
    private Task task;
    private final String separator;

    /**
     * Constructor for ContecCMS50DPlus
     * @param pn
     */
    public CMS50DPlusMeasurement(String pn, String separator) {
        this.separator = separator;
        portName = pn;

    }

    /**
     * Opens the USB serial connection and prepares for serial data
     *
     * @param task
     * @return BufferedReader to set up the data stream
     */
    public BufferedReader startSerialDevice(Task task) {
        this.task = task;
        stopping = false;
        portList = CommPortIdentifier.getPortIdentifiers();
        StringBuilder errorString = new StringBuilder();

        //Open the USB port 
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    System.out.println(portId.getName());
                if (portId.getName().equals(portName)) {
                    try {
                        serialPort = (SerialPort) portId.open("Medipi", 2000);
                    } catch (PortInUseException e) {
                        errorString.append("Port:").append(portName).append(" is already in use by ").append(portId.getCurrentOwner()).append("\n");
                    }
                    try {
                        inputStream = serialPort.getInputStream();
                    } catch (IOException e) {
                        errorString.append("Port:").append(portName).append("- can't get serial data stream from device\n");
                    }
                    try {
                        serialPort.addEventListener(this);
                    } catch (TooManyListenersException e) {
                        errorString.append("Port:").append(portName).append("- too many listeners\n");
                    }
                    serialPort.notifyOnDataAvailable(true);
                    try {
                        serialPort.setSerialPortParams(19200,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_ODD);
                        nanoTime = System.nanoTime();
                        epochTimeAtStart = System.currentTimeMillis();
                        pos = new PipedOutputStream();
                        PipedInputStream pis = new PipedInputStream(pos);
                        dataReader = new BufferedReader(new InputStreamReader(pis));
                        return dataReader;
                    } catch (UnsupportedCommOperationException | IOException e) {
                        errorString.append("Port:").append(portName).append("- device driver doesn't allow the serial port parameters\n");
                            System.out.println(e);
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
    public boolean stopSerialDevice() {
        stopping = true;
        if (serialPort != null) {
            try{
            serialPort.removeEventListener();
            } catch(NullPointerException npe){
                //do nothing - just need to remove if it's there
            }
            serialPort.close();
            try {
                dataReader.close();
            } catch (IOException ex) {
                MediPiLogger.getInstance().log(CMS50DPlusMeasurement.class.getName() + ".stopserialdevice-datareader", ex);
            }
            try {
                pos.close();
            } catch (IOException ex) {
                MediPiLogger.getInstance().log(CMS50DPlusMeasurement.class.getName() + ".stopserialdevice-pos", ex);
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
                    System.out.println("Break interrupt.");
            case SerialPortEvent.OE:
                    System.out.println("Overrun error.");
            case SerialPortEvent.FE:
                    System.out.println("Framing error.");
            case SerialPortEvent.PE:
                    System.out.println("Parity error.");
            case SerialPortEvent.CD:
                    System.out.println("Carrier detect.");
            case SerialPortEvent.CTS:
                    System.out.println("Clear to send.");
            case SerialPortEvent.DSR:
                    System.out.println("Data set ready.");
            case SerialPortEvent.RI:
                    System.out.println("Ring indicator.");
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    System.out.println("Output buffer is empty.");
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                byte[] buffer = new byte[10];
                int idx = 0;
                int[] packet = new int[5];
                try {
                    while (inputStream.available() > 10) {
                        inputStream.read(buffer);
                        for (int b : buffer) {
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
                                    int i = (packet[2] & 0x40) << 1;
                                    i |= packet[3] & 0x7f;
                                    line[0] = String.valueOf(i);
                                    //5th byte
                                    //bloodSpO2
                                    line[1] = String.valueOf(packet[4] & 127);
                                    if (fingerOut) {
                                            System.out.println("finger out");
                                        if (probeError) {
                                                System.out.println("probe error");
                                            task.cancel();
                                            stopSerialDevice();
                                        }
                                    }
                                    if (!stopping) {
                                        Instant time = Instant.ofEpochMilli(Math.round(System.nanoTime() / 1000000L) - Math.round(nanoTime / 1000000L) + epochTimeAtStart);
                                        StringBuilder sb = new StringBuilder(Utilities.ISO8601FORMATDATEMILLI_UTC.format(time));
                                        sb.append(separator);
                                        sb.append(line[0]);
                                        sb.append(separator);
                                        sb.append(line[1]);
                                        sb.append(separator);
                                        sb.append(line[2]);
                                        sb.append("\n");
                                        pos.write(sb.toString().getBytes());
                                        pos.flush();
                                            System.out.print(String.valueOf(fingerOut) + String.valueOf(probeError) + sb.toString());
                                    }
                                }
                                packet = new int[5];
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
                    MediPiMessageBox.getInstance().makeErrorMessage("Device no longer accessible", e);
                        System.out.println(e);
                }
                break;
        }
    }

}
