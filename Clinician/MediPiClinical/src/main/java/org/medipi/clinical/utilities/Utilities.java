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
package org.medipi.clinical.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Utilities class to provide common elements
 * @author rick@robinsonhq.com
 */
public class Utilities {

    private Properties properties;

    public static final DateFormat DISPLAY_FORMAT = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss z");
    public static final DateFormat INTERNAL_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final DateFormat DISPLAY_DOB_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
    public static final DateFormat INTERNAL_DOB_FORMAT = new SimpleDateFormat("yyyyMMdd");
    public static final DateFormat DISPLAY_TABLE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final DateFormat INTERNAL_DEVICE_FORMAT = new SimpleDateFormat("yyyy-MM-dd':'HH:mm");
    public static final DateFormat DISPLAY_SCALE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat DISPLAY_SCHEDULE_FORMAT = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    public static final DateFormat INTERNAL_SPINE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
    public static final DateFormat DISPLAY_OXIMETER_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static final String CREATEPATIENTFORUNASSOCIATEDDEVICES = "medipi.concentrator.db.createpatientforunassociateddevices";
    public static final String SAVEMESSAGESTOFILE = "medipi.concentrator.savemessagestofile";
    public static final String MEDIPIINBOUNDSAVEDMESSAGEDIR = "medipi.concentrator.inboundsavedmessagedir";
    public static final DateFormat ISO8601FORMATDATEMILLI = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateFormat ISO8601FORMATDATESECONDS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final DateFormat ISO8601FORMATDATEMINUTES = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    
    private Utilities() {
    }
    
    public static Utilities getInstance() {
        return UtilitiesNHolder.INSTANCE;
    }
    
    private static class UtilitiesNHolder {

        private static final Utilities INSTANCE = new Utilities();
    }
    public void setProperties(Properties mp){
        properties = mp;
    }

    public Properties getProperties() {
        return properties;
    }
    
    /**
     *
     * @param property the property to be returned
     * @param dfault - the default state of the property should it not be found
     * @return the boolean state of the property
     */
    public boolean getBooleanProperty(String property, boolean dfault) {
        String value = properties.getProperty(property);

        if (value == null) {
            return dfault;
        } else if (value.toLowerCase().startsWith("y")) {
            return true;
        } else if (value.toLowerCase().startsWith("n")) {
            return false;
        } else {
            return dfault;
        }
    }
    /**
     *
     * @param property the property to be returned
     * @return the String representation of the property
     */
    public String getStringProperty(String property) {
        String value = properties.getProperty(property);

        return value;
    }
}
