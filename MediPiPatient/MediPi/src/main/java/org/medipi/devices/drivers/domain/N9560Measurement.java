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

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import org.medipi.MediPiMessageBox;

/**
 * Class to take raw output from the Nonin 9560 Pulse Oximeter and parse it wrt its
 * protocol.
 *
 * The class allows access to the data and its format from the Nonin device
 * class
 *
 * @author rick@robinsonhq.com
 */
public class N9560Measurement implements ServiceMeasurement {

    private final byte[] resultDataFormat = new byte[]{(byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x0D};
    private final byte ackFormat = (byte) 0x06;
    private final byte nackFormat = (byte) 0x15;
    private final ArrayList<String> columns = new ArrayList<>();
    private final ArrayList<String> format = new ArrayList<>();
    private final ArrayList<String> units = new ArrayList<>();
    protected ArrayList<String> deviceDataSingleRow = new ArrayList<>();
    private final byte[] dataLengthArray = new byte[2];
    private final byte[] pulseRateArray = new byte[2];
    private final HashMap<String, Integer> intResult = new HashMap<>();
    private final HashMap<String, Boolean> booleanResult = new HashMap<>();
    private int counter = 0;
    private int checkSum = (byte) 0x00;
    private final StringBuilder serial = new StringBuilder();
    private boolean optionalData = false;
    private boolean processedFullMessage = false;

    public N9560Measurement() {

    }

    private Integer binaryCodedDecimal(byte b) {
        return Integer.valueOf(String.format("%02X", b));
    }

    private Integer hex(byte[] b) {
        switch (b.length) {
            case 2:
                return Integer.valueOf(ByteBuffer.wrap(b).getShort());
            case 4:
                return ByteBuffer.wrap(b).getInt();
            default:
                return null;
        }
    }

    public boolean parse(byte[] result) throws Exception {
        //Read Header
        for (byte b : result) {
            if (parse(b)) {
                return translate();
            }

        }
        throw new Exception("There was a problem parsing the response from the Nonin 9560 Pulse Oximeter ");

    }
// returns true if the end character has been detected in the correct place

    @Override
    public boolean parse(byte b) throws Exception {
        try {

            System.out.println("byte read: " + b);
            // handle acks and nacks from the nonin device -
            // do nothing for an ack and throw exception for nack
            if (counter == 0 && b == ackFormat) {
                System.out.println("ACK");
                return false;
            } else if (counter == 0 && b == nackFormat) {
                System.out.println("NACK");
                throw new Exception("Nonin returned a nack code from a write command " + counter);
            }
            //Read Header
            switch (counter) {

                case 0:
                case 1:
                case 2:
                case 3:
                    System.out.println("header byte: " + b);
                    if (b != resultDataFormat[counter]) {
//                        throw new Exception("Header in the wrong format: byte number " + counter);
                    }
                    break;
                case 4:
                    dataLengthArray[0] = b;
                    break;
                case 5:
                    dataLengthArray[1] = b;
                    int dataLength = hex(dataLengthArray);
                    if (dataLength == 14) {
                        optionalData = false;
                    } else if (dataLength == 23) {
                        optionalData = true;
                    } else {
                        throw new Exception("The stated data length is not compliant with the spec:" + dataLength);
                    }
                    intResult.put("DATALENGTH", dataLength);
                    break;
                case 6:
                    intResult.put("CC", binaryCodedDecimal(b));
                    checkSum = checkSum + b;
                    break;
                case 7:
                    intResult.put("YY", binaryCodedDecimal(b));
                    checkSum = checkSum + b;
                    break;
                case 8:
                    intResult.put("MM", binaryCodedDecimal(b));
                    checkSum = checkSum + b;
                    break;
                case 9:
                    intResult.put("DD", binaryCodedDecimal(b));
                    checkSum = checkSum + b;
                    break;
                case 10:
                    intResult.put("HH", binaryCodedDecimal(b));
                    checkSum = checkSum + b;
                    break;
                case 11:
                    intResult.put("mm", binaryCodedDecimal(b));
                    checkSum = checkSum + b;
                    break;
                case 12:
                    intResult.put("ss", binaryCodedDecimal(b));
                    checkSum = checkSum + b;
                    break;
                case 13:
                    intResult.put("hh", binaryCodedDecimal(b));
                    checkSum = checkSum + b;
                    break;
                case 14:
                    BitSet bs14 = BitSet.valueOf(new byte[]{b});
                    booleanResult.put("SPA", bs14.get(1));
                    booleanResult.put("NOMS", bs14.get(0));
                    checkSum = checkSum + b;
                    break;
                case 15:
                    BitSet bs15 = BitSet.valueOf(new byte[]{b});
                    booleanResult.put("MEM", bs15.get(4));
                    booleanResult.put("LOWBAT", bs15.get(0));
                    checkSum = checkSum + b;
                    break;
                case 16:
                    pulseRateArray[0] = b;
                    checkSum = checkSum + b;
                    break;
                case 17:
                    pulseRateArray[1] = b;
                    intResult.put("PULSERATE", hex(pulseRateArray));
                    checkSum = checkSum + b;
                    break;
                case 18:
                    checkSum = checkSum + b;
                    break;
                case 19:
                    intResult.put("SPO2", Integer.valueOf(b));
                    checkSum = checkSum + b;
                    if (!optionalData) {
                        counter = counter + 9;
                    }
                    break;
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                    serial.append((char) b);
                    checkSum = checkSum + b;
                    break;
                case 29:
                    byte calculatedCheckSum = (byte) (checkSum & 0xFF);
                    if (calculatedCheckSum != b) {
                        throw new Exception("The stated checksum (" + b + ")does not match the calculated sum(" + calculatedCheckSum);
                    }
                    break;
                case 30:
                    if (b != (byte) 0x03) {
                        throw new Exception("ETX is not stated in the correct place");
                    }
                    processedFullMessage = true;
                    break;

            }
            System.out.print(b + " ");
            counter++;
        } catch (Exception ex) {
            throw new Exception("There was a problem parsing the response from the Nonin 9560 Pulse Oximeter " + ex.getLocalizedMessage());
        }
        return processedFullMessage;
    }

    @Override
    public boolean translate() throws Exception {
        if (booleanResult.get("LOWBAT")) {
            MediPiMessageBox.getInstance().makeMessage("The Nonin 9560 Pulse Oximeter reports low battery - please replace the batteries");
        }
        // only record data if it is High Quality SmartPoint Data & both measurements taken
        if (booleanResult.get("SPA") && !booleanResult.get("NOMS")) {
            columns.clear();
            format.clear();
            units.clear();
            deviceDataSingleRow.clear();

            columns.add("iso8601time");
            format.add("DATE");
            units.add("NONE");

            int year = (intResult.get("CC") * 100) + intResult.get("YY");
            int month = intResult.get("MM");
            int day = intResult.get("DD");
            int hour = intResult.get("HH");
            int min = intResult.get("mm");
            int sec = intResult.get("ss");
            int nano = intResult.get("hh") * 10000000;
            LocalDateTime deviceTime = LocalDateTime.of(year, month, day, hour, min, sec, nano);
            LocalDateTime nowTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));

            long minutes = ChronoUnit.MINUTES.between(deviceTime, nowTime);

            System.out.println("time diff: " + minutes);
            deviceDataSingleRow.add(deviceTime.toInstant(ZoneOffset.UTC).toString());

            columns.add("pulse");
            format.add("INTEGER");
            units.add("BPM");
            deviceDataSingleRow.add(String.valueOf(intResult.get("PULSERATE")));

            columns.add("spo2");
            format.add("INTEGER");
            units.add("%");
            deviceDataSingleRow.add(String.valueOf(intResult.get("SPO2")));

            if (optionalData) {
                columns.add("serial");
                format.add("STRING");
                units.add("NONE");
                deviceDataSingleRow.add(serial.toString());
            }
            // If the reading is live  i.e. not from memory we know it must be the last reading
            if (!booleanResult.get("MEM")) {
                return true;
            } else {
                return false;
            }
        } else {
            StringBuilder sb = new StringBuilder("There is a data quality issue: ");
            if (!booleanResult.get("SPA")) {
                sb.append("The data is not High Quality SmartPoint Data. ");
            }
            if (booleanResult.get("NOMS")) {
                sb.append("The pulse or SpO2 is missing. ");
            }
            throw new Exception(sb.toString().trim());
        }
    }

    public ArrayList<String> getColumns() {
        return columns;
    }

    public ArrayList<String> getFormat() {
        return format;
    }

    public ArrayList<String> getUnits() {
        return units;
    }

    public ArrayList<String> getDeviceData() {
        return deviceDataSingleRow;
    }

    public int getPulse() {
        String s = deviceDataSingleRow.get(columns.indexOf("pulse"));
        return Integer.valueOf(s);
    }

    public Instant getTime() {
        String s = deviceDataSingleRow.get(columns.indexOf("iso8601time"));
        return Instant.parse(s);
    }

    public int getSpO2() {
        String s = deviceDataSingleRow.get(columns.indexOf("spo2"));
        return Integer.valueOf(s);
    }
}
