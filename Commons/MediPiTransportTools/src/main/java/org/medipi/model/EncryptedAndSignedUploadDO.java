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
 * Class to contain the encrypted and signed representation of messages to and from MediPi Concentrator
 * @author rick@robinsonhq.com
 */
public class EncryptedAndSignedUploadDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String uploadUuid;
    private String encryptedKey;
    private String cipherData;

    /**
     * Constructor
     */
    public EncryptedAndSignedUploadDO() {
    }

    /**
     * Constructor
     * @param uploadUuid
     */
    public EncryptedAndSignedUploadDO(String uploadUuid) {
        this.uploadUuid = uploadUuid;
    }

    /**
     * Constructor
     * @param payloadUuid
     * @param encryptedKey
     * @param cipherData
     */
    public EncryptedAndSignedUploadDO(String payloadUuid, String encryptedKey, String cipherData) {
        this.uploadUuid = payloadUuid;
        this.encryptedKey = encryptedKey;
        this.cipherData = cipherData;
    }

    public String getUploadUuid() {
        return uploadUuid;
    }

    public void setUploadUuid(String uploadUuid) {
        this.uploadUuid = uploadUuid;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getCipherData() {
        return cipherData;
    }

    public void setCipherData(String cipherData) {
        this.cipherData = cipherData;
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
        if (!(object instanceof EncryptedAndSignedUploadDO)) {
            return false;
        }
        EncryptedAndSignedUploadDO other = (EncryptedAndSignedUploadDO) object;
        if ((this.uploadUuid == null && other.uploadUuid != null) || (this.uploadUuid != null && !this.uploadUuid.equals(other.uploadUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.model.encryptedAndSignedUploadDO [ uploadUuid=" + uploadUuid + " ]";
    }

}
