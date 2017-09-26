/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.devices.drivers.domain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.devices.drivers.Omron708BT;
import org.medipi.logging.MediPiLogger;

/**
 *
 * @author riro
 */
/**
 * The BM55User represents the user related to BM55 instrument for blood
 * pressure measurement.
 */
public class ContinuaManager {

    Process healthdProcess = null;

    public enum ReadingStatus {
        NOTHING,
        WAITING,
        CONNECTED,
        CONFIGURATION,
        ATTRIBUTES,
        MEASUREMENT,
        DISASSOCIATED,
        DISCONNECTED;
    }

    private boolean isDataComplete = false;
    private ReadingStatus isReading = ReadingStatus.NOTHING;
    private ContinuaData continuaDataSet = new ContinuaData();
    private int measurementAttributeIDCounter = 0;
    private ArrayList<Integer> referenceList = new ArrayList();
    private final ContinuaMeasurementFactory measurementFactory = new ContinuaMeasurementFactory();
    private String specialisation = "";
    private Process process = null;
    private final String agentScript;
    private final String managerScript;
    private static Exception bootException = null;

    private ContinuaManager() {
        managerScript = MediPiProperties.getInstance().getProperties().getProperty("medipi.iee11073.manager.python");
        if (managerScript == null || managerScript.trim().length() == 0) {
            String error = "Cannot find python ieee11073 manager script";
            MediPiLogger.getInstance().log(ContinuaManager.class.getName(), error);
            bootException = new Exception("Cannot find python ieee11073 manager script");
        }
        startHealthd();
        agentScript = MediPiProperties.getInstance().getProperties().getProperty("medipi.iee11073.agent.python");
        if (agentScript == null || agentScript.trim().length() == 0) {
            String error = "Cannot find python ieee11073 agent script";
            MediPiLogger.getInstance().log(ContinuaManager.class.getName(), error);
            bootException = new Exception("Cannot find python ieee11073 agent script");
        }
    }

    public static ContinuaManager getInstance() throws Exception {
        if (bootException != null) {
            throw bootException;
        }
        return ContinuaManagerHolder.INSTANCE;
    }

    private static class ContinuaManagerHolder {

        private static final ContinuaManager INSTANCE = new ContinuaManager();
    }

    private void startHealthd() {
        //call healthd
        stopHealthd();
        try {
            System.out.println("start healthd");
            String[] callAndArgs = {managerScript};
            healthdProcess = Runtime.getRuntime().exec(callAndArgs);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(healthdProcess.getInputStream()));
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Unable to start healthd service, try restarting MediPi", ex);
        }
    }

    public BufferedReader callIEEE11073Agent(String specialisation) {
        stopIEEE11073Agent();
        if (healthdProcess == null || !healthdProcess.isAlive()) {
            startHealthd();
        }
        try {
            System.out.println(agentScript);
            String[] callAndArgs = {"python", agentScript, "--interpret", "--set-time", "--mds", "--agent-type", specialisation};
            process = Runtime.getRuntime().exec(callAndArgs);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return stdInput;
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Download of data unsuccessful", ex);
            return null;
        }

    }

    public void stopIEEE11073Agent() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
        stopHealthd();
    }

    public void stopHealthd() {
        if (healthdProcess != null && healthdProcess.isAlive()) {
            healthdProcess.destroy();
        }
    }

    public void reset() {
        stopIEEE11073Agent();
        stopHealthd();
        isDataComplete = false;
        isReading = ReadingStatus.NOTHING;
        continuaDataSet = new ContinuaData();
        measurementAttributeIDCounter = -1;
        referenceList = new ArrayList();
        specialisation = "";
    }

    public ReadingStatus getStatus() {
        return isReading;
    }

    public boolean parse(String data) throws Exception {
        if (data.equals("START")) {
            isReading = ReadingStatus.NOTHING;
            isDataComplete = false;
        } else if (data.equals("healthd service not found, waiting...") || data.equals("Detaching...")) {
            isReading = ReadingStatus.NOTHING;
            isDataComplete = false;
            startHealthd();
        } else if (data.equals("END")) {
            isReading = ReadingStatus.NOTHING;
            isDataComplete = true;
            stopHealthd();
        } else if (data.equals("")) {
            isReading = ReadingStatus.NOTHING;
        } else if (data.equals("Waiting...")) {
            isReading = ReadingStatus.WAITING;
        } else if (data.startsWith("Connected")) {
            isReading = ReadingStatus.CONNECTED;
        } else if (data.equals("Configuration")) {
            isReading = ReadingStatus.CONFIGURATION;
        } else if (data.equals("Device Attributes")) {
            isReading = ReadingStatus.ATTRIBUTES;
        } else if (data.equals("Measurement")) {
            isReading = ReadingStatus.MEASUREMENT;
            measurementAttributeIDCounter = -1;
            continuaDataSet.addDataSetCounter();
        } else if (data.startsWith("\t")) {
            if (null != isReading) {
                switch (isReading) {
                    case CONFIGURATION:
                        if (data.indexOf("	Numeric  unit  ") == 0) {
                            String s = data.substring("	Numeric  unit  ".length());
                            referenceList.add(Integer.valueOf(s));
                        } else {
                            //THROW error in reading attribute
                            throw new Exception("error in reading configuration from device");
                        }
                        break;
                    case ATTRIBUTES:
                        if (data.indexOf("	Specializations: ") == 0) {
                            String s = data.substring("	Specializations: ".length());
                            specialisation = s.trim();
                        }
                        if (data.indexOf("	Manufacturer: ") == 0) {
                            String s = data.substring("	Manufacturer: ".length());
                            String[] details = s.trim().split(" Model: ");
                            continuaDataSet.setManufacturer(details[0]);
                            continuaDataSet.setModel(details[1]);
                        }
                        break;
                    case MEASUREMENT:
                        if (specialisation.equals("")) {
                            //THROW error in reading specialisation
                            throw new Exception("no specialisation found on device, please retry");
                        }
                        measurementAttributeIDCounter++;
                        if (data.indexOf("	") == 0) {
                            ContinuaMeasurement continuaMeasurement = measurementFactory.getMeasurementClass(specialisation);
                            continuaMeasurement.insertData(data.trim(), continuaDataSet.getDataSetCounter());
                            //check that the unit identifiers match
                            int unitIdentifier = referenceList.get(measurementAttributeIDCounter);
                            if (unitIdentifier != continuaMeasurement.getReportedIdentifier()) {
                                throw new Exception("error in matching attribute to measurement in device");
                            } else {
                                continuaDataSet.addMeasurement(continuaMeasurement);
                            }

                        } else {
                            //THROW error in reading measurements
                            throw new Exception("error in reading measurements from device, please retry");
                        }
                        break;
                    default:
                        break;
                }
            }
        } else if (data.startsWith("Disassociated")) {
            isReading = ReadingStatus.DISASSOCIATED;
        } else if (data.startsWith("Disconnected")) {
            isReading = ReadingStatus.DISCONNECTED;
        }
        return isDataComplete;
    }

    public ContinuaData getData() throws Exception {
        if (isDataComplete) {
            return continuaDataSet;
        } else {
            //THROW error in reading measurements
            throw new Exception("Request for data before data has been fully downloaded");
        }
    }

}
