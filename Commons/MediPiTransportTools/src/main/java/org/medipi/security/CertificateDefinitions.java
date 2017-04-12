/*
 Copyright 2016  Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>

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
package org.medipi.security;

import java.util.HashMap;
import java.util.Properties;

/**
 * A class to manage the certificate proprties to be altered depending on the
 * context of where UploadEncryptionAdaptor is called from. All the properties
 * variables have a default value but can be overridden using the getters and
 * setters
 *
 * @author rick@robinsonhq.com
 */
public class CertificateDefinitions {

    private final String ENCRYPTTRUSTSTORELOCATION = "medipi.json.encrypt.truststore.location";
    private final String ENCRYPTTRUSTSTOREPASSWORD = "medipi.json.encrypt.truststore.password";
    private final String ENCRYPTTRUSTSTOREALIAS = "medipi.json.encrypt.truststore.alias";
    private final String SIGNTRUSTSTORELOCATION = "medipi.json.sign.truststore.location";
    private final String SIGNTRUSTSTOREPASSWORD = "medipi.json.sign.truststore.password";
    private final String ENCRYPTKEYSTOREPASSWORD = "medipi.json.encrypt.keystore.password";
    private final String ENCRYPTKEYSTOREALIAS = "medipi.json.encrypt.keystore.alias";
    private final String ENCRYPTKEYSTORELOCATION = "medipi.json.encrypt.keystore.location";
    private final String SIGNKEYSTOREPASSWORD = "medipi.json.sign.keystore.password";
    private final String SIGNKEYSTOREALIAS = "medipi.json.sign.keystore.alias";
    private final String SIGNKEYSTORELOCATION = "medipi.json.sign.keystore.location";
    private final Properties properties;
    
    public static final int SYSTEM = 0;
    public static final int INTERNAL = 1;
    private final HashMap<String, CertificateDefinitions.PropertyTypeDO> propertiesMap = new HashMap<>();
    private byte[] pem = null;
    public CertificateDefinitions(Properties p) {
        properties = p;
    }

    public byte[] getEncryptTruststorePEM() {
        return pem;
    }

    public void setEncryptTruststorePEM(byte[] pem) {
        this.pem = pem;
    }

    public String getENCRYPTTRUSTSTORELOCATION() {
        PropertyTypeDO ptdo = propertiesMap.get("ENCRYPTTRUSTSTORELOCATION");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, ENCRYPTTRUSTSTORELOCATION));
        } else {
            return getProperty(propertiesMap.get("ENCRYPTTRUSTSTORELOCATION"));
        }
    }

    public void setENCRYPTTRUSTSTORELOCATION(String ENCRYPTTRUSTSTORELOCATION, int PropertyType) {
        propertiesMap.put("ENCRYPTTRUSTSTORELOCATION", new PropertyTypeDO(PropertyType, ENCRYPTTRUSTSTORELOCATION));
    }

    public String getENCRYPTTRUSTSTOREPASSWORD() {
        PropertyTypeDO ptdo = propertiesMap.get("ENCRYPTTRUSTSTOREPASSWORD");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, ENCRYPTTRUSTSTOREPASSWORD));
        } else {
            return getProperty(propertiesMap.get("ENCRYPTTRUSTSTOREPASSWORD"));
        }
    }

    public void setENCRYPTTRUSTSTOREPASSWORD(String ENCRYPTTRUSTSTOREPASSWORD, int PropertyType) {
        propertiesMap.put("ENCRYPTTRUSTSTOREPASSWORD", new PropertyTypeDO(PropertyType, ENCRYPTTRUSTSTOREPASSWORD));
    }

    public String getENCRYPTTRUSTSTOREALIAS() {
        PropertyTypeDO ptdo = propertiesMap.get("ENCRYPTTRUSTSTOREALIAS");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, ENCRYPTTRUSTSTOREALIAS));
        } else {
            return getProperty(propertiesMap.get("ENCRYPTTRUSTSTOREALIAS"));
        }
    }

    public void setENCRYPTTRUSTSTOREALIAS(String ENCRYPTTRUSTSTOREALIAS, int PropertyType) {
        propertiesMap.put("ENCRYPTTRUSTSTOREALIAS", new PropertyTypeDO(PropertyType, ENCRYPTTRUSTSTOREALIAS));
    }

    public String getSIGNTRUSTSTORELOCATION() {
        PropertyTypeDO ptdo = propertiesMap.get("SIGNTRUSTSTORELOCATION");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, SIGNTRUSTSTORELOCATION));
        } else {
            return getProperty(propertiesMap.get("SIGNTRUSTSTORELOCATION"));
        }
    }

    public void setSIGNTRUSTSTORELOCATION(String SIGNTRUSTSTORELOCATION, int PropertyType) {
        propertiesMap.put("SIGNTRUSTSTORELOCATION", new PropertyTypeDO(PropertyType, SIGNTRUSTSTORELOCATION));
    }

    public String getSIGNTRUSTSTOREPASSWORD() {
        PropertyTypeDO ptdo = propertiesMap.get("SIGNTRUSTSTOREPASSWORD");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, SIGNTRUSTSTOREPASSWORD));
        } else {
            return getProperty(propertiesMap.get("SIGNTRUSTSTOREPASSWORD"));
        }
    }

    public void setSIGNTRUSTSTOREPASSWORD(String SIGNTRUSTSTOREPASSWORD, int PropertyType) {
        propertiesMap.put("SIGNTRUSTSTOREPASSWORD", new PropertyTypeDO(PropertyType, SIGNTRUSTSTOREPASSWORD));
    }

    public String getENCRYPTKEYSTOREPASSWORD() {
        PropertyTypeDO ptdo = propertiesMap.get("ENCRYPTKEYSTOREPASSWORD");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, ENCRYPTKEYSTOREPASSWORD));
        } else {
            return getProperty(propertiesMap.get("ENCRYPTKEYSTOREPASSWORD"));
        }
    }

    public void setENCRYPTKEYSTOREPASSWORD(String ENCRYPTKEYSTOREPASSWORD, int PropertyType) {
        propertiesMap.put("ENCRYPTKEYSTOREPASSWORD", new PropertyTypeDO(PropertyType, ENCRYPTKEYSTOREPASSWORD));
    }

    public String getENCRYPTKEYSTOREALIAS() {
        PropertyTypeDO ptdo = propertiesMap.get("ENCRYPTKEYSTOREALIAS");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, ENCRYPTKEYSTOREALIAS));
        } else {
            return getProperty(propertiesMap.get("ENCRYPTKEYSTOREALIAS"));
        }
    }

    public void setENCRYPTKEYSTOREALIAS(String ENCRYPTKEYSTOREALIAS, int PropertyType) {
        propertiesMap.put("ENCRYPTKEYSTOREALIAS", new PropertyTypeDO(PropertyType, ENCRYPTKEYSTOREALIAS));
    }

    public String getENCRYPTKEYSTORELOCATION() {
        PropertyTypeDO ptdo = propertiesMap.get("ENCRYPTKEYSTORELOCATION");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, ENCRYPTKEYSTORELOCATION));
        } else {
            return getProperty(propertiesMap.get("ENCRYPTKEYSTORELOCATION"));
        }
    }

    public void setENCRYPTKEYSTORELOCATION(String ENCRYPTKEYSTORELOCATION, int PropertyType) {
        propertiesMap.put("ENCRYPTKEYSTORELOCATION", new PropertyTypeDO(PropertyType, ENCRYPTKEYSTORELOCATION));
    }

    public String getSIGNKEYSTOREPASSWORD() {
        PropertyTypeDO ptdo = propertiesMap.get("SIGNKEYSTOREPASSWORD");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, SIGNKEYSTOREPASSWORD));
        } else {
            return getProperty(propertiesMap.get("SIGNKEYSTOREPASSWORD"));
        }
    }

    public void setSIGNKEYSTOREPASSWORD(String SIGNKEYSTOREPASSWORD, int PropertyType) {
        propertiesMap.put("SIGNKEYSTOREPASSWORD", new PropertyTypeDO(PropertyType, SIGNKEYSTOREPASSWORD));
    }

    public String getSIGNKEYSTOREALIAS() {
        PropertyTypeDO ptdo = propertiesMap.get("SIGNKEYSTOREALIAS");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, SIGNKEYSTOREALIAS));
        } else {
            return getProperty(propertiesMap.get("SIGNKEYSTOREALIAS"));
        }
    }

    public void setSIGNKEYSTOREALIAS(String SIGNKEYSTOREALIAS, int PropertyType) {
        propertiesMap.put("SIGNKEYSTOREALIAS", new PropertyTypeDO(PropertyType, SIGNKEYSTOREALIAS));
    }

    public String getSIGNKEYSTORELOCATION() {
        PropertyTypeDO ptdo = propertiesMap.get("SIGNKEYSTORELOCATION");
        if (ptdo == null) {
            return getProperty(new PropertyTypeDO(INTERNAL, SIGNKEYSTORELOCATION));
        } else {
            return getProperty(propertiesMap.get("SIGNKEYSTORELOCATION"));
        }
    }

    public void setSIGNKEYSTORELOCATION(String SIGNKEYSTORELOCATION, int PropertyType) {
        propertiesMap.put("SIGNKEYSTORELOCATION", new PropertyTypeDO(PropertyType, SIGNKEYSTORELOCATION));
    }

    private String getProperty(PropertyTypeDO ptdo) {
        switch (ptdo.getPropertyType()) {
            case SYSTEM: {
                String prop = System.getProperty(ptdo.getPropertyNamespace());
                if (prop == null || prop.trim().length() == 0) {
                    return null;
                } else {
                    return prop;
                }
            }
            case INTERNAL: {
                String prop = properties.getProperty(ptdo.getPropertyNamespace());;
                if (prop == null || prop.trim().length() == 0) {
                    return null;
                } else {
                    return prop;
                }
            }
            default:
                return null;
        }
    }

    private class PropertyTypeDO {

        private int propertyType;
        private String propertyNamespace;

        public PropertyTypeDO(int propertyType, String propertyNamespace) {
            this.propertyType = propertyType;
            this.propertyNamespace = propertyNamespace;
        }

        public int getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(int propertyType) {
            this.propertyType = propertyType;
        }

        public String getPropertyNamespace() {
            return propertyNamespace;
        }

        public void setPropertyNamespace(String propertyNamespace) {
            this.propertyNamespace = propertyNamespace;
        }

    }
}
