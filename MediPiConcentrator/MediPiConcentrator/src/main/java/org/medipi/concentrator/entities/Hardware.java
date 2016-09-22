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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entity Class to manage DB access for hardware
 * @author rick@robinsonhq.com
 */

@Entity
@Table(name = "hardware")
@NamedQueries({
    @NamedQuery(name = "Hardware.findAll", query = "SELECT p FROM Hardware p"),
    @NamedQuery(name = "Hardware.findByHardwareName", query = "SELECT p FROM Hardware p WHERE p.hardwareName = :hardwareName"),
    @NamedQuery(name = "Hardware.findByMacAddress", query = "SELECT p FROM Hardware p WHERE p.macAddress = :macAddress"),
    @NamedQuery(name = "Hardware.findByCurrentSoftwareVersion", query = "SELECT p FROM Hardware p WHERE p.currentSoftwareVersion = :currentSoftwareVersion"),
    @NamedQuery(name = "Hardware.findByPatientUuid", query = "SELECT p FROM Hardware p WHERE p.patientUuid.patientUuid = :patientUuid")})
public class Hardware implements Serializable {



    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "hardware_name")
    private String hardwareName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "mac_address")
    private String macAddress;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "current_software_version")
    private String currentSoftwareVersion;
    @JoinColumn(name = "patient_uuid", referencedColumnName = "patient_uuid")
    @ManyToOne
    private Patient patientUuid;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hardwareName", fetch = FetchType.LAZY)
    private Collection<AllHardwareDownloaded> allHardwareDownloadedCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hardwareName", fetch = FetchType.LAZY)
    private Collection<HardwareDownloadable> hardwareDownloadableCollection;

    public Hardware() {
    }

    public Hardware(String hardwareName) {
        this.hardwareName = hardwareName;
    }

    public Hardware(String hardwareName, String macAddress, String currentSoftwareVersion) {
        this.hardwareName = hardwareName;
        this.macAddress = macAddress;
        this.currentSoftwareVersion = currentSoftwareVersion;
    }

    public String getHardwareName() {
        return hardwareName;
    }

    public void setHardwareName(String hardwareName) {
        this.hardwareName = hardwareName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getCurrentSoftwareVersion() {
        return currentSoftwareVersion;
    }

    public void setCurrentSoftwareVersion(String currentSoftwareVersion) {
        this.currentSoftwareVersion = currentSoftwareVersion;
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
        hash += (hardwareName != null ? hardwareName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Hardware)) {
            return false;
        }
        Hardware other = (Hardware) object;
        if ((this.hardwareName == null && other.hardwareName != null) || (this.hardwareName != null && !this.hardwareName.equals(other.hardwareName))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Hardware[ hardwareName=" + hardwareName + " ]";
    }

    public Collection<AllHardwareDownloaded> getAllHardwareDownloadedCollection() {
        return allHardwareDownloadedCollection;
    }

    public void setAllHardwareDownloadedCollection(Collection<AllHardwareDownloaded> allHardwareDownloadedCollection) {
        this.allHardwareDownloadedCollection = allHardwareDownloadedCollection;
    }

    public Collection<HardwareDownloadable> getHardwareDownloadableCollection() {
        return hardwareDownloadableCollection;
    }

    public void setHardwareDownloadableCollection(Collection<HardwareDownloadable> hardwareDownloadableCollection) {
        this.hardwareDownloadableCollection = hardwareDownloadableCollection;
    }


    
}
