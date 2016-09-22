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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity Class to manage DB access for patient_certificate
 * @author rick@robinsonhq.com
 */

@Entity
@Table(name = "patient_certificate")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PatientCertificate.findAll", query = "SELECT p FROM PatientCertificate p"),
    @NamedQuery(name = "PatientCertificate.findByPatientUuid", query = "SELECT p FROM PatientCertificate p WHERE p.patientUuid = :patientUuid"),
    @NamedQuery(name = "PatientCertificate.findByCertificateLocation", query = "SELECT p FROM PatientCertificate p WHERE p.certificateLocation = :certificateLocation")})
public class PatientCertificate implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "patient_uuid")
    private String patientUuid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "certificate_location")
    private String certificateLocation;

    public PatientCertificate() {
    }

    public PatientCertificate(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public PatientCertificate(String patientUuid, String certificateLocation) {
        this.patientUuid = patientUuid;
        this.certificateLocation = certificateLocation;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getCertificateLocation() {
        return certificateLocation;
    }

    public void setCertificateLocation(String certificateLocation) {
        this.certificateLocation = certificateLocation;
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
        if (!(object instanceof PatientCertificate)) {
            return false;
        }
        PatientCertificate other = (PatientCertificate) object;
        if ((this.patientUuid == null && other.patientUuid != null) || (this.patientUuid != null && !this.patientUuid.equals(other.patientUuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.concentrator.entities.PatientCertificate[ patientUuid=" + patientUuid + " ]";
    }
    
}
