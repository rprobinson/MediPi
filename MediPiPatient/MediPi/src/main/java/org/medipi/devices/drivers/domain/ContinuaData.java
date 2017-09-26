/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.devices.drivers.domain;

import java.util.ArrayList;

/**
 *
 * @author riro
 */
public class ContinuaData {

    private int dataSetCounter=-1;
    private String manufacturer; 
    private String model; 
    private final ArrayList<ContinuaMeasurement> measurement = new ArrayList();

    public static String removeUnwantedDecimalPoints(String s) {
        return s.indexOf(".") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }
    public ContinuaData() {
    }

    public ArrayList<ContinuaMeasurement> getMeasurements() {
        return measurement;
    }

    public void addMeasurement(ContinuaMeasurement measurement) {
        this.measurement.add(measurement);
    }

    public int getDataSetCounter() {
        return dataSetCounter;
    }
    public void addDataSetCounter() {
        dataSetCounter++;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

}
