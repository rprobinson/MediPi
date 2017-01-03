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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity Class to manage DB access for attribute_threshold
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "attribute_threshold")
@XmlRootElement
@NamedQueries({
    // Added
    @NamedQuery(name = "AttributeThreshold.findByAttribute",
            query = "SELECT a FROM AttributeThreshold a WHERE a.attributeId.attributeId = :attributeId AND a.patientUuid.patientUuid = :patientUuid AND a.effectiveDate IN (SELECT MAX(b.effectiveDate) FROM AttributeThreshold b WHERE b.attributeId.attributeId = :attributeId AND b.patientUuid.patientUuid = :patientUuid AND b.effectiveDate<= :measurementDate)"),
//
    @NamedQuery(name = "AttributeThreshold.findAll", query = "SELECT a FROM AttributeThreshold a"),
    @NamedQuery(name = "AttributeThreshold.findByAttributeThresholdId", query = "SELECT a FROM AttributeThreshold a WHERE a.attributeThresholdId = :attributeThresholdId"),
    @NamedQuery(name = "AttributeThreshold.findByThresholdType", query = "SELECT a FROM AttributeThreshold a WHERE a.thresholdType = :thresholdType"),
    @NamedQuery(name = "AttributeThreshold.findByEffectiveDate", query = "SELECT a FROM AttributeThreshold a WHERE a.effectiveDate = :effectiveDate")

})
public class AttributeThreshold implements Serializable {



    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "threshold_high_value")
    private String thresholdHighValue;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "threshold_low_value")
    private String thresholdLowValue;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "attribute_threshold_id")
    private Integer attributeThresholdId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "threshold_type")
    private String thresholdType;
    @Basic(optional = false)
    @NotNull
    @Column(name = "effective_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date effectiveDate;
    @JoinColumn(name = "patient_uuid", referencedColumnName = "patient_uuid")
    @ManyToOne(optional = false)
    private Patient patientUuid;
    @JoinColumn(name = "attribute_id", referencedColumnName = "attribute_id")
    @ManyToOne(optional = false)
    private RecordingDeviceAttribute attributeId;

    public AttributeThreshold() {
    }

    public AttributeThreshold(Integer attributeThresholdId) {
        this.attributeThresholdId = attributeThresholdId;
    }

    public AttributeThreshold(Integer attributeThresholdId, String thresholdType, String thresholdValue, Date effectiveDate) {
        this.attributeThresholdId = attributeThresholdId;
        this.thresholdType = thresholdType;
        this.effectiveDate = effectiveDate;
    }

    public Integer getAttributeThresholdId() {
        return attributeThresholdId;
    }

    public void setAttributeThresholdId(Integer attributeThresholdId) {
        this.attributeThresholdId = attributeThresholdId;
    }

    public String getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(String thresholdType) {
        this.thresholdType = thresholdType;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Patient getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(Patient patientUuid) {
        this.patientUuid = patientUuid;
    }

    public RecordingDeviceAttribute getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(RecordingDeviceAttribute attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (attributeThresholdId != null ? attributeThresholdId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AttributeThreshold)) {
            return false;
        }
        AttributeThreshold other = (AttributeThreshold) object;
        if ((this.attributeThresholdId == null && other.attributeThresholdId != null) || (this.attributeThresholdId != null && !this.attributeThresholdId.equals(other.attributeThresholdId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.clinical.entities.AttributeThreshold[ attributeThresholdId=" + attributeThresholdId + " ]";
    }

    public String getThresholdHighValue() {
        return thresholdHighValue;
    }

    public void setThresholdHighValue(String thresholdHighValue) {
        this.thresholdHighValue = thresholdHighValue;
    }

    public String getThresholdLowValue() {
        return thresholdLowValue;
    }

    public void setThresholdLowValue(String thresholdLowValue) {
        this.thresholdLowValue = thresholdLowValue;
    }


}
