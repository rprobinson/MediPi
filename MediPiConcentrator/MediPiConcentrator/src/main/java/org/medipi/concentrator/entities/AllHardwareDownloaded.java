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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity Class to manage DB access for all_hardware_downloaded
 *
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "all_hardware_downloaded")
@NamedQueries({
    //Added Queries
    @NamedQuery(name = "AllHardwareDownloaded.findByDownloadableAndHardwareName", query = "SELECT a FROM AllHardwareDownloaded a WHERE a.downloadableUuid.downloadableUuid = :downloadableUuid AND a.hardwareName.hardwareName = :hardwareName"),
    //
    @NamedQuery(name = "AllHardwareDownloaded.findAll", query = "SELECT a FROM AllHardwareDownloaded a"),
    @NamedQuery(name = "AllHardwareDownloaded.findByAllHardwareDownloadedId", query = "SELECT a FROM AllHardwareDownloaded a WHERE a.allHardwareDownloadedId = :allHardwareDownloadedId"),
    @NamedQuery(name = "AllHardwareDownloaded.findByDownloadedDate", query = "SELECT a FROM AllHardwareDownloaded a WHERE a.downloadedDate = :downloadedDate")})
public class AllHardwareDownloaded implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "all_hardware_downloaded_id")
    private Integer allHardwareDownloadedId;
    @Column(name = "downloaded_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date downloadedDate;
    @JoinColumn(name = "downloadable_uuid", referencedColumnName = "downloadable_uuid")
    @ManyToOne(optional = false)
    private AllHardwareDownloadable downloadableUuid;
    @JoinColumn(name = "hardware_name", referencedColumnName = "hardware_name")
    @ManyToOne(optional = false)
    private Hardware hardwareName;

    public AllHardwareDownloaded() {
    }

    public AllHardwareDownloaded(Integer allHardwareDownloadedId) {
        this.allHardwareDownloadedId = allHardwareDownloadedId;
    }

    public Integer getAllHardwareDownloadedId() {
        return allHardwareDownloadedId;
    }

    public void setAllHardwareDownloadedId(Integer allHardwareDownloadedId) {
        this.allHardwareDownloadedId = allHardwareDownloadedId;
    }

    public Date getDownloadedDate() {
        return downloadedDate;
    }

    public void setDownloadedDate(Date downloadedDate) {
        this.downloadedDate = downloadedDate;
    }

    public AllHardwareDownloadable getDownloadableUuid() {
        return downloadableUuid;
    }

    public void setDownloadableUuid(AllHardwareDownloadable downloadableUuid) {
        this.downloadableUuid = downloadableUuid;
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
        hash += (allHardwareDownloadedId != null ? allHardwareDownloadedId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AllHardwareDownloaded)) {
            return false;
        }
        AllHardwareDownloaded other = (AllHardwareDownloaded) object;
        if ((this.allHardwareDownloadedId == null && other.allHardwareDownloadedId != null) || (this.allHardwareDownloadedId != null && !this.allHardwareDownloadedId.equals(other.allHardwareDownloadedId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.concentrator.entities.AllHardwareDownloaded[ allHardwareDownloadedId=" + allHardwareDownloadedId + " ]";
    }

}
