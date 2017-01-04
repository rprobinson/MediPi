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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import org.medipi.MediPiProperties;

/**
 * Class to take raw output from the Marsden M430 Scale and parse it wrt its
 * protocol.
 *
 * The class allows access to the data and its format from the Marsden device
 * class
 *
 * @author rick@robinsonhq.com
 */
public class M430Measurement {

    private final ArrayList<String[]> resultFormat = new ArrayList<>();
    private final ArrayList<String> columns = new ArrayList<>();
    private final ArrayList<String> format = new ArrayList<>();
    private final ArrayList<String> units = new ArrayList<>();
    protected ArrayList<String> deviceDataSingleRow = new ArrayList<>();
    private boolean timestampFromDevice = true;

    public M430Measurement(String deviceNamespace) {

        String b = MediPiProperties.getInstance().getProperties().getProperty(deviceNamespace + ".taketimefromdevice");
        if (b == null || b.trim().length() == 0) {
            timestampFromDevice = true;
        } else {
            timestampFromDevice = !b.toLowerCase().startsWith("n");
        }
        resultFormat.add(new String[]{"G", "R", "O", "S", "S", " ", "W", "E", "I", "G", "H", "T", " ", " ", " ", " ", " ", " ", "<GW>", "<GW>", "<GW>", "<GW>", "<GW>", "<GW>", "<GW>", " ", "<GWU>", "<GWU>", "<CR>", "<LF>", "<LF>"});
        resultFormat.add(new String[]{"T", "A", "R", "E", " ", "W", "E", "I", "G", "H", "T", " ", " ", " ", " ", " ", " ", " ", "<TW>", "<TW>", "<TW>", "<TW>", "<TW>", "<TW>", "<TW>", " ", "<TWU>", "<TWU>", "<CR>", "<LF>", "<LF>"});
        resultFormat.add(new String[]{"N", "E", "T", " ", "W", "E", "I", "G", "H", "T", " ", " ", " ", " ", " ", " ", " ", "<NW>", "<NW>", "<NW>", "<NW>", "<NW>", "<NW>", "<NW>", "<NW>", " ", "<NWU>", "<NWU>", "<CR>", "<LF>", "<LF>"});
        resultFormat.add(new String[]{"H", "E", "I", "G", "H", "T", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "<H>", "<H>", "<H>", "<H>", "<H>", "<H>", "<H>", " ", "<HU>", "<HU>", "<CR>", "<LF>", "<LF>"});
//        resultFormat.put("HEIGHT      ", new String[]{"H", "E", "I", "G", "H", "T", " ", " ", " ", " ", " ", " ", " ", " ", "<F>", "<F>", "<F>", " ", "<U>", "<U>", " ", "<I>", "<I>", "<I>", "<I>", " ", "<U>", "<U>", "<CR>", "<LF>", "<LF>"});
//        resultFormat.put("HEIGHT      ", new String[]{"H", "E", "I", "G", "H", "T", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "-", "-", "-", " ", "<U>", "<U>", "<CR>", "<LF>", "<LF>"});
//        resultFormat.put("HEIGHT      ", new String[]{"H", "E", "I", "G", "H", "T", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "-", " ", "<U>", "<U>", " ", " ", "-", "-", "-", " ", "<U>", "<U>", "<CR>", "<LF>", "<LF>"});
        resultFormat.add(new String[]{"B", "M", "I", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "<B>", "<B>", "<B>", "<B>", "<B>", "<B>", "<B>", " ", " ", " ", "<CR>", "<LF>", "<LF>"});
//        resultFormat.put("BMI         ", new String[]{"B", "M", "I", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "N", "/", "A", "<LF>", "<CR>", "<LF>", "<CR>", "<LF>", "<LF>"});
        resultFormat.add(new String[]{"<D>", "<D>", "/", "<M>", "<M>", "/", "<Y>", "<Y>", "<Y>", "<Y>", " ", " ", " ", "<h>", "<h>", ":", "<m>", "<m>", " ", " ", "<CR>", "<LF>", "<CR>", "<LF>", " ", " ", " ", " ", "<CR>", "<LF>", "<LF>"});
        resultFormat.add(new String[]{" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "<CR>", "<LF>", "<LF>", "<LF>", "<LF>"});
    }

    public String parse(byte[] result) {
        try {
            HashMap<String, StringBuilder> mappedResult = new HashMap<>();
            if (result.length == 217) {
                //real data
                int rowNumber = 0;
                int columnNumber = 0;
                StringBuilder type = new StringBuilder();
                String[] rowMap = null;

                for (byte b : result) {
                    if (columnNumber == 0) {
                        rowMap = resultFormat.get(rowNumber);
                    }

                    // what should that byte look like?
                    String formatMap = rowMap[columnNumber];
                    byte formatByte;
                    //when the formatMap is explicit
                    if (formatMap.getBytes().length == 1) {
                        formatByte = formatMap.getBytes()[0];
                        if (formatByte != b) {
                            return "Incoming data from is in wrong format at column " + columnNumber;
                        }
                        // when the formatMap needs interpretation: either a special character or a result field    
                    } else if (formatMap.equals("<CR>")) {
                        if ((int) b != 13) {
                            return "Incoming data from is in wrong format at column " + columnNumber;
                        }
                    } else if (formatMap.equals("<LF>")) {
                        if ((int) b != 10) {
                            return "Incoming data from is in wrong format at column " + columnNumber;
                        }
                    } else {
                        StringBuilder sb = mappedResult.get(formatMap);
                        if (sb == null) {
                            char c = (char) b;
                            mappedResult.put(formatMap, new StringBuilder(String.valueOf(c)));
                        } else {
                            sb.append((char) b);
                            mappedResult.replace(formatMap, sb);
                        }
                    }
                    columnNumber++;
                    if (columnNumber == 31) {
                        type = new StringBuilder();
                        rowMap = null;
                        columnNumber = 0;
                        rowNumber++;
                    }

                }
            } else {
                //datalength is wrong
                return "Incoming data from is the wrong length";
            }

            columns.clear();
            format.clear();
            units.clear();
            deviceDataSingleRow.clear();

            columns.add("iso8601time");
            format.add("DATE");
            units.add("NONE");

            if (timestampFromDevice) {
                int year = Integer.valueOf(mappedResult.get("<Y>").toString());
                int month = Integer.valueOf(mappedResult.get("<M>").toString());
                int day = Integer.valueOf(mappedResult.get("<D>").toString());
                int hour = Integer.valueOf(mappedResult.get("<h>").toString());
                int min = Integer.valueOf(mappedResult.get("<m>").toString());
                LocalDateTime time = LocalDateTime.of(year, month, day, hour, min);
                deviceDataSingleRow.add(time.toInstant(ZoneOffset.UTC).toString());
            } else {
                deviceDataSingleRow.add(Instant.now().toString());
            }

            columns.add("weight");
            format.add("DOUBLE");
            units.add(mappedResult.get("<NWU>").toString().trim());
            deviceDataSingleRow.add(mappedResult.get("<NW>").toString().trim());

            columns.add("grossweight");
            format.add("DOUBLE");
            units.add(mappedResult.get("<GWU>").toString().trim());
            deviceDataSingleRow.add(mappedResult.get("<GW>").toString().trim());

            columns.add("tareweight");
            format.add("DOUBLE");
            units.add(mappedResult.get("<TWU>").toString().trim());
            deviceDataSingleRow.add(mappedResult.get("<TW>").toString().trim());

            columns.add("height");
            format.add("DOUBLE");
            units.add(mappedResult.get("<HU>").toString().trim());
            deviceDataSingleRow.add(mappedResult.get("<H>").toString().trim());

            columns.add("bmi");
            format.add("DOUBLE");
            units.add("NONE");
            deviceDataSingleRow.add(mappedResult.get("<B>").toString().trim());

            return null;
        } catch (Exception ex) {
            return "There was a problem parsing the response from scale " + ex.getLocalizedMessage();
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

    public Instant getTime() {
        String s = deviceDataSingleRow.get(columns.indexOf("iso8601time"));
        return Instant.parse(s);
    }

    public double getWeight() {
        return Double.valueOf(getDeviceData().get(columns.indexOf("weight")));
    }
}
