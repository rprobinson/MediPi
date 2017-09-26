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
public interface ContinuaMeasurement {


    public void insertData(String in, int id) throws Exception;

    public int getReportedIdentifier();

    public int getMeasurementSetID();

    public String[] getDataValue();

    public String getTime();
    
}
