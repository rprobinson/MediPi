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
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity Class to manage DB access for PatientDetails
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "patient_details")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PatientDetails.findAll", query = "SELECT p FROM PatientDetails p"),
    @NamedQuery(name = "PatientDetails.findByPatientUuid", query = "SELECT p FROM PatientDetails p WHERE p.patientUuid = :patientUuid"),
    @NamedQuery(name = "PatientDetails.findByNhsNumber", query = "SELECT p FROM PatientDetails p WHERE p.nhsNumber = :nhsNumber"),
    @NamedQuery(name = "PatientDetails.findByFirstName", query = "SELECT p FROM PatientDetails p WHERE p.firstName = :firstName"),
    @NamedQuery(name = "PatientDetails.findByLastName", query = "SELECT p FROM PatientDetails p WHERE p.lastName = :lastName"),
    @NamedQuery(name = "PatientDetails.findByDob", query = "SELECT p FROM PatientDetails p WHERE p.dob = :dob")})
public class PatientDetails implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "patient_uuid")
    private String patientUuid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "nhs_number")
    private String nhsNumber;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "first_name")
    private String firstName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "last_name")
    private String lastName;
    @Basic(optional = false)
    @NotNull
    @Column(name = "dob")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dob;
    @JoinColumn(name = "patient_uuid", referencedColumnName = "patient_uuid", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Patient patient;

    public PatientDetails() {
    }

    public PatientDetails(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public PatientDetails(String patientUuid, String nhsNumber, String firstName, String lastName, Date dob) {
        this.patientUuid = patientUuid;
        this.nhsNumber = nhsNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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
        if (!(object instanceof PatientDetails)) {
            return false;
        }
        PatientDetails other = (PatientDetails) object;
        if ((this.patientUuid == null && other.patientUuid != null) || (this.patientUuid != null && !this.patientUuid.equals(other.patientUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.clinical.entities.PatientDetails[ patientUuid=" + patientUuid + " ]";
    }
    
}
