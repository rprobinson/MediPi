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
package org.medipi.concentrator.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Utilities class to provide common elements
 *
 * @author rick@robinsonhq.com
 */
public class Utilities {

    private Properties properties;

    public static final DateFormat INTERNAL_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final DateFormat INTERNAL_SPINE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss.SSS");

    private Utilities() {
    }

    public static Utilities getInstance() {
        return UtilitiesNHolder.INSTANCE;
    }

    private static class UtilitiesNHolder {

        private static final Utilities INSTANCE = new Utilities();
    }

    public void setProperties(Properties mp) {
        properties = mp;
    }

    public Properties getProperties() {
        return properties;
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
