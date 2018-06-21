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
public class ContinuaBloodPressure implements ContinuaMeasurement {

    public static final int MMHG = 3872;
    public static final int BPM = 2720;
    public static final int DIMENTIONLESS = 512;

    private String dataValue[];
    private String time;
    private int reportedIdentifier;
    private int measurementSetID;

    public ContinuaBloodPressure() {
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
            case "mmHg":
                reportedIdentifier = MMHG;
                String dp = dataPoints[0].replace("(", "").replace(")", "");
                dataValue = dp.split(",");
                if (dataValue.length != 3) {
                    throw new Exception("Device Data in wrong format - not in 3 parts");
                }
                int counter = 0;
                for (String s : dataValue) {
                    dataValue[counter] = ContinuaData.removeUnwantedDecimalPoints(s);
                    counter++;
                }
                break;
            case "bpm":
                reportedIdentifier = BPM;
                dataValue= new String[1];
                dataValue[0] = ContinuaData.removeUnwantedDecimalPoints(dataPoints[0]);
                break;
            case "":
                reportedIdentifier = DIMENTIONLESS;
                dataValue= new String[1];
                String s = ContinuaData.removeUnwantedDecimalPoints(dataPoints[0]);
                if(s.equals("256")){
                    dataValue[0] = "true";
                }else if(s.equals("0")){
                    dataValue[0] = "false";
                }else{
                    dataValue[0] = "true";
                    throw new Exception("Irregular Heart Beat units unrecognised");
                }
                break;
            default:
                throw new Exception("Device Data units unrecognised");
        }

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
