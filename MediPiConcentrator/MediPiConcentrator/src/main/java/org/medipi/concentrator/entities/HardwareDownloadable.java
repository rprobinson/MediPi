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
package org.medipi.concentrator.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entity Class to manage DB access for hardware_downloadable
 * @author rick@robinsonhq.com
 */

@Entity
@Table(name = "hardware_downloadable")
@NamedQueries({
    //Added
    @NamedQuery(name = "HardwareDownloadable.findByHardware", query = "SELECT p FROM HardwareDownloadable p WHERE p.hardwareName.hardwareName = :hname AND p.downloadedDate IS NULL"),
    @NamedQuery(name = "HardwareDownloadable.findByDownloadableUuidAndOpen", query = "SELECT p FROM HardwareDownloadable p WHERE p.downloadableUuid = :downloadableUuid AND p.downloadedDate IS NULL"),
    // 
    @NamedQuery(name = "HardwareDownloadable.findAll", query = "SELECT h FROM HardwareDownloadable h"),
    @NamedQuery(name = "HardwareDownloadable.findByDownloadableUuid", query = "SELECT h FROM HardwareDownloadable h WHERE h.downloadableUuid = :downloadableUuid"),
    @NamedQuery(name = "HardwareDownloadable.findByVersion", query = "SELECT h FROM HardwareDownloadable h WHERE h.version = :version"),
    @NamedQuery(name = "HardwareDownloadable.findByVersionAuthor", query = "SELECT h FROM HardwareDownloadable h WHERE h.versionAuthor = :versionAuthor"),
    @NamedQuery(name = "HardwareDownloadable.findByVersionDate", query = "SELECT h FROM HardwareDownloadable h WHERE h.versionDate = :versionDate"),
    @NamedQuery(name = "HardwareDownloadable.findByDownloadedDate", query = "SELECT h FROM HardwareDownloadable h WHERE h.downloadedDate = :downloadedDate"),
    @NamedQuery(name = "HardwareDownloadable.findByScriptLocation", query = "SELECT h FROM HardwareDownloadable h WHERE h.scriptLocation = :scriptLocation"),
    @NamedQuery(name = "HardwareDownloadable.findBySignature", query = "SELECT h FROM HardwareDownloadable h WHERE h.signature = :signature")})
public class HardwareDownloadable implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "downloadable_uuid")
    private String downloadableUuid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "version")
    private String version;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "version_author")
    private String versionAuthor;
    @Basic(optional = false)
    @NotNull
    @Column(name = "version_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date versionDate;
    @Column(name = "downloaded_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date downloadedDate;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "script_location")
    private String scriptLocation;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10000)
    @Column(name = "signature")
    private String signature;
    @JoinColumn(name = "hardware_name", referencedColumnName = "hardware_name")
    @ManyToOne(optional = false)
    private Hardware hardwareName;

    public HardwareDownloadable() {
    }

    public HardwareDownloadable(String downloadableUuid) {
        this.downloadableUuid = downloadableUuid;
    }

    public HardwareDownloadable(String downloadableUuid, String version, String versionAuthor, Date versionDate, String scriptLocation, String signature) {
        this.downloadableUuid = downloadableUuid;
        this.version = version;
        this.versionAuthor = versionAuthor;
        this.versionDate = versionDate;
        this.scriptLocation = scriptLocation;
        this.signature = signature;
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

    public Date getDownloadedDate() {
        return downloadedDate;
    }

    public void setDownloadedDate(Date downloadedDate) {
        this.downloadedDate = downloadedDate;
    }

    public String getScriptLocation() {
        return scriptLocation;
    }

    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Hardware getHardwareName() {
        return hardwareName;
    }

    public void setHardwareName(Hardware hardwareName) {
        this.hardwareName = hardwareName;
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
        if (!(object instanceof HardwareDownloadable)) {
            return false;
        }
        HardwareDownloadable other = (HardwareDownloadable) object;
        if ((this.downloadableUuid == null && other.downloadableUuid != null) || (this.downloadableUuid != null && !this.downloadableUuid.equals(other.downloadableUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.concentrator.entities.HardwareDownloadable[ downloadableUuid=" + downloadableUuid + " ]";
    }
    
}
