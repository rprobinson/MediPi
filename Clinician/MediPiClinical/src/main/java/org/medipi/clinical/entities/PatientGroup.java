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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Entity Class to manage DB access for patient group
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "patient_group")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PatientGroup.findAll", query = "SELECT p FROM PatientGroup p"),
    @NamedQuery(name = "PatientGroup.findByPatientGroupUuid", query = "SELECT p FROM PatientGroup p WHERE p.patientGroupUuid = :patientGroupUuid")})
public class PatientGroup implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "patient_group_uuid")
    private String patientGroupUuid;
    @OneToMany(mappedBy = "patientGroupUuid")
    private Collection<Patient> patientCollection;
    @Size(max = 100)
    @Column(name = "patient_group_name")
    private String patientGroupName;


    public PatientGroup() {
    }

    public PatientGroup(String patientGroupUuid) {
        this.patientGroupUuid = patientGroupUuid;
    }

    public String getPatientGroupUuid() {
        return patientGroupUuid;
    }

    public void setPatientGroupUuid(String patientGroupUuid) {
        this.patientGroupUuid = patientGroupUuid;
    }
    public String getPatientGroupName() {
        return patientGroupName;
    }

    public void setPatientGroupName(String patientGroupName) {
        this.patientGroupName = patientGroupName;
    }
    
    @XmlTransient
    public Collection<Patient> getPatientCollection() {
        return patientCollection;
    }

    public void setPatientCollection(Collection<Patient> patientCollection) {
        this.patientCollection = patientCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (patientGroupUuid != null ? patientGroupUuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PatientGroup)) {
            return false;
        }
        PatientGroup other = (PatientGroup) object;
        if ((this.patientGroupUuid == null && other.patientGroupUuid != null) || (this.patientGroupUuid != null && !this.patientGroupUuid.equals(other.patientGroupUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.clinical.entities.PatientGroup[ patientGroupUuid=" + patientGroupUuid + " ]";
    }


}
