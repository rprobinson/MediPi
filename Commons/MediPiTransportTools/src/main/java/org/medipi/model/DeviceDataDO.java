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
package org.medipi.model;

import java.io.Serializable;

/**
 * Class to hold data from one device in order to send to the MediPi Concentrator
 * @author rick@robinsonhq.com
 */
public class DeviceDataDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String deviceDataUuid;
    private String profileId;
    private String payload;

    /**
     * Constructor
     */
    public DeviceDataDO() {
    }

    /**
     * Constructor
     * @param deviceDataUuid
     */
    public DeviceDataDO(String deviceDataUuid) {
        this.deviceDataUuid = deviceDataUuid;
    }

    /**
     * Constructor
     * @param payloadUuid
     * @param profileId
     * @param deviceDataUuid
     */
    public DeviceDataDO(String payloadUuid, String profileId, String deviceDataUuid) {
        this.deviceDataUuid = deviceDataUuid;
        this.profileId = profileId;
        this.payload = payload;
    }

    public String getDeviceDataUuid() {
        return deviceDataUuid;
    }

    public void setDeviceDataUuid(String deviceDataUuid) {
        this.deviceDataUuid = deviceDataUuid;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (deviceDataUuid != null ? deviceDataUuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DeviceDataDO)) {
            return false;
        }
        DeviceDataDO other = (DeviceDataDO) object;
        if ((this.deviceDataUuid == null && other.deviceDataUuid != null) || (this.deviceDataUuid != null && !this.deviceDataUuid.equals(other.deviceDataUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.model.DeviceDataDO [ deviceDataUuid=" + deviceDataUuid + " ]";
    }

}
