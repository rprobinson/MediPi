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
package org.medipi.clinical.entities;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Entity Class to manage DB access for Patient
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "patient")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Patient.findAll", query = "SELECT p FROM Patient p"),
    @NamedQuery(name = "Patient.findByPatientUuid", query = "SELECT p FROM Patient p WHERE p.patientUuid = :patientUuid")})
public class Patient implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientUuid")
    private Collection<Alert> alertCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "patient_uuid")
    private String patientUuid;
    @JoinColumn(name = "patient_group_uuid", referencedColumnName = "patient_group_uuid")
    @ManyToOne
    private PatientGroup patientGroupUuid;

    public Patient() {
    }

    public Patient(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public PatientGroup getPatientGroupUuid() {
        return patientGroupUuid;
    }

    public void setPatientGroupUuid(PatientGroup patientGroupUuid) {
        this.patientGroupUuid = patientGroupUuid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (patientUuid != null ? patientUuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Patient)) {
            return false;
        }
        Patient other = (Patient) object;
        if ((this.patientUuid == null && other.patientUuid != null) || (this.patientUuid != null && !this.patientUuid.equals(other.patientUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.clinical.entities.Patient[ patientUuid=" + patientUuid + " ]";
    }

    @XmlTransient
    public Collection<Alert> getAlertCollection() {
        return alertCollection;
    }

    public void setAlertCollection(Collection<Alert> alertCollection) {
        this.alertCollection = alertCollection;
    }
    
}
