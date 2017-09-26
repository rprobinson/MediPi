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
public class ContinuaMeasurementFactory {

    public ContinuaMeasurement getMeasurementClass(String specialisation) {
        ContinuaMeasurement response = null;
        if (specialisation.equals("0x1004")) {
            response = new ContinuaOximeter();
        } else if (specialisation.equals("0x1007")) {
            response = new ContinuaBloodPressure();
        }
        return response;
    }
}
