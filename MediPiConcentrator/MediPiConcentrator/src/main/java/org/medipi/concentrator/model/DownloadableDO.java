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
package org.medipi.concentrator.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import org.springframework.hateoas.ResourceSupport;

/**
 * This data object encapsulates the data to be sent for each downloadable
 * entity for the MediPi Patient device.These downloadable data objects can be
 * Hardware, AllHardware or Patient types
 *
 * @author rick@robinsonhq.com
 */
public class DownloadableDO extends ResourceSupport implements Serializable {

    private static final long serialVersionUID = 1L;
    private String downloadableUuid;
    private String version;
    private String versionAuthor;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    private Date versionDate;
    private String signature;
    private String fileName;
    private String downloadType;

    private Date downloadedDate;

    public DownloadableDO() {
    }

    public DownloadableDO(String downloadableUuid) {
        this.downloadableUuid = downloadableUuid;
    }

    public DownloadableDO(String downloadableUuid, String version, String versionAuthor, Date versionDate) {
        this.downloadableUuid = downloadableUuid;
        this.version = version;
        this.versionAuthor = versionAuthor;
        this.versionDate = versionDate;
    }

    public String getDownloadableUuid() {
        return downloadableUuid;
    }

    public void setDownloadableUuid(String downloadableUuid) {
        this.downloadableUuid = downloadableUuid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionAuthor() {
        return versionAuthor;
    }

    public void setVersionAuthor(String versionAuthor) {
        this.versionAuthor = versionAuthor;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getFileName() {
        File f = new File(fileName);
        return f.getName();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getDownloadedDate() {
        return downloadedDate;
    }

    public void setDownloadedDate(Date downloadedDate) {
        this.downloadedDate = downloadedDate;
    }

    public String getDownloadType() {
        return downloadType;
    }

    public void setDownloadType(String downloadType) {
        this.downloadType = downloadType;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (downloadableUuid != null ? downloadableUuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DownloadableDO)) {
            return false;
        }
        DownloadableDO other = (DownloadableDO) object;
        if ((this.downloadableUuid == null && other.downloadableUuid != null) || (this.downloadableUuid != null && !this.downloadableUuid.equals(other.downloadableUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.concentrator.entities.DownloadableDO[ downloadableUuid=" + downloadableUuid + " ]";
    }

}
