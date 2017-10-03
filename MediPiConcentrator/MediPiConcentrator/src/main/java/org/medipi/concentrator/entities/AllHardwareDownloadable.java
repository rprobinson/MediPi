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
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * Entity Class to manage DB access for all_hardware_downloadable
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "all_hardware_downloadable")
@NamedQueries({
    //Added queries
    @NamedQuery(name = "AllHardwareDownloadable.findAllDownloadable", query = "SELECT c FROM AllHardwareDownloadable c WHERE c.downloadableUuid NOT IN (SELECT a.downloadableUuid FROM AllHardwareDownloadable a, AllHardwareDownloaded b WHERE a.downloadableUuid = b.downloadableUuid AND b.hardwareName.hardwareName = :hname)"), 
    //
    @NamedQuery(name = "AllHardwareDownloadable.findAll", query = "SELECT a FROM AllHardwareDownloadable a"),
    @NamedQuery(name = "AllHardwareDownloadable.findByDownloadableUuid", query = "SELECT a FROM AllHardwareDownloadable a WHERE a.downloadableUuid = :downloadableUuid"),
    @NamedQuery(name = "AllHardwareDownloadable.findByVersion", query = "SELECT a FROM AllHardwareDownloadable a WHERE a.version = :version"),
    @NamedQuery(name = "AllHardwareDownloadable.findByVersionAuthor", query = "SELECT a FROM AllHardwareDownloadable a WHERE a.versionAuthor = :versionAuthor"),
    @NamedQuery(name = "AllHardwareDownloadable.findByVersionDate", query = "SELECT a FROM AllHardwareDownloadable a WHERE a.versionDate = :versionDate"),
    @NamedQuery(name = "AllHardwareDownloadable.findByScriptLocation", query = "SELECT a FROM AllHardwareDownloadable a WHERE a.scriptLocation = :scriptLocation"),
    @NamedQuery(name = "AllHardwareDownloadable.findBySignature", query = "SELECT a FROM AllHardwareDownloadable a WHERE a.signature = :signature")})
public class AllHardwareDownloadable implements Serializable {

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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "downloadableUuid")
    private Collection<AllHardwareDownloaded> allHardwareDownloadedCollection;

    public AllHardwareDownloadable() {
    }

    public AllHardwareDownloadable(String downloadableUuid) {
        this.downloadableUuid = downloadableUuid;
    }

    public AllHardwareDownloadable(String downloadableUuid, String version, String versionAuthor, Date versionDate, String scriptLocation, String signature) {
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

    public Collection<AllHardwareDownloaded> getAllHardwareDownloadedCollection() {
        return allHardwareDownloadedCollection;
    }

    public void setAllHardwareDownloadedCollection(Collection<AllHardwareDownloaded> allHardwareDownloadedCollection) {
        this.allHardwareDownloadedCollection = allHardwareDownloadedCollection;
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
        if (!(object instanceof AllHardwareDownloadable)) {
            return false;
        }
        AllHardwareDownloadable other = (AllHardwareDownloadable) object;
        if ((this.downloadableUuid == null && other.downloadableUuid != null) || (this.downloadableUuid != null && !this.downloadableUuid.equals(other.downloadableUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.concentrator.entities.AllHardwareDownloadable[ downloadableUuid=" + downloadableUuid + " ]";
    }

}
