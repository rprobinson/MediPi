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
package org.medipi.devices.drivers.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.medipi.MediPiProperties;
import org.medipi.devices.Element;
import org.medipi.logging.MediPiLogger;

/**
 * Service class to maintain the json file representation of the bluetooth properties entries
 * 
 * TODO: relies on the spp port number 1, no authentication,encryption and slave device
 * @author rick@robinsonhq.com
 */
public class BluetoothPropertiesService {

    private static final String SERIALURLSTART = "btspp://";
    private static final String SERIALURLEND = ":1;authenticate=false;encrypt=false;master=false";
    private static BluetoothPropertiesService me = null;
    private static final String BLUETOOTHPROPERTIES = "medipi.bluetooth.properties";
    private ArrayList<BluetoothPropertiesDO> bluetoothProperties = null;
    private final ArrayList<Element> elements = new ArrayList<>();
    private File propertiesFile = null;
    private final MediPiLogger logger = MediPiLogger.getInstance();
 
    /**
     * Constructor
     * @throws java.lang.Exception
     */
    public BluetoothPropertiesService() throws Exception{
        String propertiesDirectory = MediPiProperties.getInstance().getProperties().getProperty(BLUETOOTHPROPERTIES);
        if (propertiesDirectory == null || propertiesDirectory.trim().length() == 0) {
            throw new Exception("Bluetooth Properties Directory parameter not configured");
        }
        String error = "";
        if ((error = loadProperties(propertiesDirectory)) != null) {
            throw new Exception(error);
        }
    }

    /**
     * @return the singleton instance of this class.
     * @throws java.lang.Exception
     */
    public static synchronized BluetoothPropertiesService getInstance() throws Exception {
        if (me == null) {
            me = new BluetoothPropertiesService();
        }
        return me;
    }


    private String loadProperties(String propertiesDir) {
        try {
            propertiesFile = new File(propertiesDir);
            ObjectMapper mapper = new ObjectMapper();
            bluetoothProperties = mapper.readValue(propertiesFile, new TypeReference<ArrayList<BluetoothPropertiesDO>>() {
            });
        } catch (IOException ex) {
            return "Cannot read the Bluetooth Properties file: " + propertiesDir + ex.getLocalizedMessage();
        }
        return null;
    }

    private String saveProperties() {
        FileOutputStream fop = null;
        try {
            fop = new FileOutputStream(propertiesFile);
            // if file doesnt exists, then create it
            if (!propertiesFile.exists()) {
                String error = "Cannot find Bluetooth properties file";
                logger.log(BluetoothPropertiesService.class.getName() + ".error", error);
                return error;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(fop, bluetoothProperties);
            fop.flush();
            fop.close();

            logger.log(BluetoothPropertiesService.class.getName(), Instant.now().toString() + " Change to persisted to Bluetooth Properties file");
        } catch (IOException e) {
            String error = "Cannot save Bluetooth properties change to local drive - check the configured directory: " + propertiesFile;
            logger.log(BluetoothPropertiesService.class.getName() + ".error", error);
            return error;
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                String error = "Cannot save Bluetooth properties change to local drive - check the configured directory: " + propertiesFile;
                logger.log(BluetoothPropertiesService.class.getName() + ".error", error);
                return error;
            }
        }
        return null;

    }

    public boolean addPropertyDO(String device, String friendlyName, String protocolId, String url) {
        return addPropertyDO(null, device, friendlyName, protocolId, url);
    }

    private boolean addPropertyDO(String uuid, String device, String friendlyName, String protocolId, String url) {
        BluetoothPropertiesDO bp = new BluetoothPropertiesDO(uuid, device, friendlyName, protocolId, url);
        return addPropertyDO(bp);
    }

    private boolean addPropertyDO(BluetoothPropertiesDO bp) {
        if (bp.getUuid() == null || bp.getUuid().trim().length() == 0) {
            bp.setUuid(UUID.randomUUID().toString());
        }
        // if there is already an entry with that medipi device name remove it
        BluetoothPropertiesDO bpdo = getBluetoothPropertyDOByMedipiDeviceName(bp.getMedipiDeviceName());
        if (bpdo != null) {
            removePropertyDO(bpdo.getUuid());
        }
        if (bluetoothProperties.add(bp)) {
            String error;
            return (error = saveProperties()) == null;
        } else {
            return false;
        }
    }

    private boolean removePropertyDO(String uuid) {
        for (BluetoothPropertiesDO bp : bluetoothProperties) {
            if (bp.getUuid().equals(uuid)) {
                bluetoothProperties.remove(bp);
                String error;
                return (error = saveProperties()) == null;
            }
        }
        return false;
    }

    public BluetoothPropertiesDO getBluetoothPropertyDOByUuid(String uuid) {
        for (BluetoothPropertiesDO bp : bluetoothProperties) {
            if (bp.getUuid().equals(uuid)) {
                return bp;
            }
        }
        return null;
    }

    public BluetoothPropertiesDO getBluetoothPropertyDOByMedipiDeviceName(String medipiDeviceName) {
        for (BluetoothPropertiesDO bp : bluetoothProperties) {
            if (bp.getMedipiDeviceName().equals(medipiDeviceName)) {
                return bp;
            }
        }
        return null;
    }

    public void register(Element aThis) {
        elements.add(aThis);
    }

    public ArrayList<Element> getRegisteredElements() {
        return elements;
    }
    
    public String getMACFromUrl(String url) {
        if (url == null || url.trim().length() == 0) {
            return null;
        } else {
            int start = url.indexOf(SERIALURLSTART) + SERIALURLSTART.length();
            int end = start + 12;
            return url.substring(start, end);
        }
    }

    public String getUrlFromMac(String mac) {
        return SERIALURLSTART + mac +SERIALURLEND;
    }

}
