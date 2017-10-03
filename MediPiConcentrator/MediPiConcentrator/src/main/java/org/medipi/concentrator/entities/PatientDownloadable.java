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
 * Entity Class to manage DB access for patient_downloadable
 * @author rick@robinsonhq.com
 */

@Entity
@Table(name = "patient_downloadable")
@NamedQueries({
//Added
    @NamedQuery(name = "PatientDownloadable.findByPatientUuid", query = "SELECT p FROM PatientDownloadable p WHERE p.patientUuid.patientUuid = :patientUuid AND p.downloadedDate IS NULL"),
    @NamedQuery(name = "PatientDownloadable.findByDownloadableUuidAndOpen", query = "SELECT p FROM PatientDownloadable p WHERE p.downloadableUuid = :downloadableUuid AND p.downloadedDate IS NULL"),
// 
    @NamedQuery(name = "PatientDownloadable.findAll", query = "SELECT p FROM PatientDownloadable p"),
    @NamedQuery(name = "PatientDownloadable.findByDownloadableUuid", query = "SELECT p FROM PatientDownloadable p WHERE p.downloadableUuid = :downloadableUuid"),
    @NamedQuery(name = "PatientDownloadable.findByVersion", query = "SELECT p FROM PatientDownloadable p WHERE p.version = :version"),
    @NamedQuery(name = "PatientDownloadable.findByVersionAuthor", query = "SELECT p FROM PatientDownloadable p WHERE p.versionAuthor = :versionAuthor"),
    @NamedQuery(name = "PatientDownloadable.findByVersionDate", query = "SELECT p FROM PatientDownloadable p WHERE p.versionDate = :versionDate"),
    @NamedQuery(name = "PatientDownloadable.findByDownloadedDate", query = "SELECT p FROM PatientDownloadable p WHERE p.downloadedDate = :downloadedDate"),
    @NamedQuery(name = "PatientDownloadable.findByScriptLocation", query = "SELECT p FROM PatientDownloadable p WHERE p.scriptLocation = :scriptLocation"),
    @NamedQuery(name = "PatientDownloadable.findBySignature", query = "SELECT p FROM PatientDownloadable p WHERE p.signature = :signature")})
public class PatientDownloadable implements Serializable {


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
    @Size(min = 1, max = 255)
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
    @JoinColumn(name = "patient_uuid", referencedColumnName = "patient_uuid")
    @ManyToOne(optional = false)
    private Patient patientUuid;


    public PatientDownloadable() {
    }

    public PatientDownloadable(String downloadableUuid) {
        this.downloadableUuid = downloadableUuid;
    }

    public PatientDownloadable(String downloadableUuid, String version, String versionAuthor, Date versionDate, String scriptLocation, String signature) {
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

    public Patient getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(Patient patientUuid) {
        this.patientUuid = patientUuid;
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
        if (!(object instanceof PatientDownloadable)) {
            return false;
        }
        PatientDownloadable other = (PatientDownloadable) object;
        if ((this.downloadableUuid == null && other.downloadableUuid != null) || (this.downloadableUuid != null && !this.downloadableUuid.equals(other.downloadableUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.concentrator.entities.PatientDownloadable[ downloadableUuid=" + downloadableUuid + " ]";
    }


}
