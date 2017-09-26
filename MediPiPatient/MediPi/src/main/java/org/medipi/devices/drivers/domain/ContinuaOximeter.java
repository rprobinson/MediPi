/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.devices.drivers.domain;

/**
 *
 * @author riro
 */
public class ContinuaOximeter implements ContinuaMeasurement {

    public static final int PERCENT = 544;
    public static final int BPM = 2720;

    private String[] dataValue;
    private String time;
    private int reportedIdentifier;
    private int measurementSetID;

    public ContinuaOximeter() {
    }

    @Override
    public void insertData(String in, int id) throws Exception {
        String[] dataPoints = in.split(" ");
        if (dataPoints.length != 4) {
            throw new Exception("Device Data in wrong format - not in 4 parts");
        }
        if (!dataPoints[2].equals("@")) {
            throw new Exception("Device Data in wrong format - time format");
        }
        switch (dataPoints[1]) {
            case "%":
                reportedIdentifier = PERCENT;
                break;
            case "bpm":
                reportedIdentifier = BPM;
                break;
            default:
                throw new Exception("Device Data units unrecognised");
        }

        dataValue = new String[1];
        dataValue[0] = ContinuaData.removeUnwantedDecimalPoints(dataPoints[0]);
        time = dataPoints[3];
        measurementSetID = id;
    }

    public int getReportedIdentifier() {
        return reportedIdentifier;
    }

    public String[] getDataValue() {
        return dataValue;
    }

    public void setDataValue(String[] dataValue) {
        this.dataValue = dataValue;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getMeasurementSetID() {
        return measurementSetID;
    }

}
