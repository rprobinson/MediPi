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

import java.io.Serializable;

/**
 * Class to contain the bluetooth properties for devices which may have been
 * paired but MediPi needs MAC address for in order to communicate serially with
 *
 * @author rick@robinsonhq.com
 */
public class BluetoothPropertiesDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String uuid;
    private String medipiDeviceName;
    private String btFriendlyName;
    private String btProtocolId;
    private String url;

    /**
     * Constructor
     */
    public BluetoothPropertiesDO() {
    }

    public BluetoothPropertiesDO(String uuid, String medipiDeviceName, String btFriendlyName, String btProtocolId, String url) {
        this.uuid = uuid;
        this.medipiDeviceName = medipiDeviceName;
        this.btFriendlyName = btFriendlyName;
        this.btProtocolId = btProtocolId;
        this.url = url;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMedipiDeviceName() {
        return medipiDeviceName;
    }

    public void setMedipiDeviceName(String medipiDeviceName) {
        this.medipiDeviceName = medipiDeviceName;
    }

    public String getBtFriendlyName() {
        return btFriendlyName;
    }

    public void setBtFriendlyName(String btFriendlyName) {
        this.btFriendlyName = btFriendlyName;
    }

    public String getBtProtocolId() {
        return btProtocolId;
    }

    public void setBtProtocolId(String btProtocolId) {
        this.btProtocolId = btProtocolId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uuid != null ? uuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BluetoothPropertiesDO)) {
            return false;
        }
        BluetoothPropertiesDO other = (BluetoothPropertiesDO) object;
        if ((this.uuid == null && other.uuid != null) || (this.uuid != null && !this.uuid.equals(other.uuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.model.BluetoothProperties [ uuid=" + uuid + " ]";
    }

}
