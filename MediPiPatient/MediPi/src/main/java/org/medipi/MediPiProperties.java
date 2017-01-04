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
package org.medipi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.medipi.logging.MediPiLogger;
import org.medipi.utilities.ConfigurationStringTokeniser;

/**
 * Singleton Class to digest the .properties file and make it easily accessible.
 * All the properties for MediPi are contained within the properties file which
 * is passed into the main class. This class make the Properties object
 * containing the properties loaded from the file accessible to the rest of the
 * application.
 *
 * @author rick@robinsonhq.com
 */
public class MediPiProperties {

    private static MediPiProperties me = null;
    private Properties properties = null;

    /**
     * Constructor
     */
    public MediPiProperties() {
        properties = new java.util.Properties();
    }

    /**
     * @return the singleton instance of this class.
     */
    public static synchronized MediPiProperties getInstance() {
        if (me == null) {
            me = new MediPiProperties();
        }
        return me;
    }

    /**
     * Method to get the properties read from the .properties file in a
     * properties object
     *
     * @return properties object
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Method to pass the properties file location into the class
     *
     * @param propertiesfile - the file location of the .properties file
     * @return pass/fail indicator as to the success of loading the properties
     */
    public boolean setProperties(String propertiesfile) {
        BufferedReader br = null;
        try {
            // load the External TKW properties - Port names for the request handlers MUST be appended
            br = new BufferedReader(new FileReader(propertiesfile));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }
                if (line.trim().length() == 0) {
                    continue;
                }

                ConfigurationStringTokeniser st = new ConfigurationStringTokeniser(line);
                String key = st.nextToken();
                StringBuilder value = new StringBuilder(st.nextToken());
                String element;
                while (st.hasMoreTokens()) {
                    element = st.nextToken();
                    value.append(" ");
                    value.append(element);
                }
                properties.setProperty(key, value.toString());
            }
            br.close();
        } catch (Exception ex) {
            MediPiLogger.getInstance().log(MediPiProperties.class.getName(), ex);
            return false;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                MediPiLogger.getInstance().log(MediPiProperties.class.getName(), ex);
                return false;
            }
        }
        return true;
    }
}
