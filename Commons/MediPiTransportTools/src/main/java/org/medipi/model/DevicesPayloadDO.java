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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to hold data from one to many devices to be transmitted to the MediPi Concentrator
 * @author rick@robinsonhq.com
 */
public class DevicesPayloadDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String uploadUuid;
    private List<DeviceDataDO> payload = new ArrayList<>();
    private Date uploadedDate;

    /**
     * Constructor
     */
    public DevicesPayloadDO() {
    }

    /**
     * Constructor
     * @param uploadUuid
     */
    public DevicesPayloadDO(String uploadUuid) {
        this.uploadUuid = uploadUuid;
    }

    /**
     * Constructor
     * @param uploadUuid
     * @param uploadDate
     */
    public DevicesPayloadDO(String uploadUuid, Date uploadDate) {
        this.uploadUuid = uploadUuid;
        this.uploadedDate = uploadDate;
    }

    public String getUploadUuid() {
        return uploadUuid;
    }

    public void setUploadUuid(String uploadUuid) {
        this.uploadUuid = uploadUuid;
    }

    public List<DeviceDataDO> getPayload() {
        return payload;
    }

    public void setPayload(List<DeviceDataDO> payload) {
        this.payload = payload;
    }

    public void addPayload(DeviceDataDO payload) {
        this.payload.add(payload);
    }

    public Date getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uploadUuid != null ? uploadUuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DevicesPayloadDO)) {
            return false;
        }
        DevicesPayloadDO other = (DevicesPayloadDO) object;
        if ((this.uploadUuid == null && other.uploadUuid != null) || (this.uploadUuid != null && !this.uploadUuid.equals(other.uploadUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.model.DevicesPayloadDO [ uploadUuid=" + uploadUuid + " ]";
    }

}
