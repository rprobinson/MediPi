/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.gov.nhs.digital.telehealth.clinician.service.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

//@formatter:off
@Entity
@Table(name = "attribute_threshold")
@NamedQueries({
	@NamedQuery(name = "AttributeThresholdMaster.fetchAllAttributeThresholds", query = "SELECT attributeThreshold FROM AttributeThresholdMaster attributeThreshold"),

	@NamedQuery(name = "AttributeThresholdMaster.findEffectiveAttributeThreshold", query = "SELECT a FROM AttributeThresholdMaster a"
			+ " WHERE a.recordingDeviceAttribute.attributeId = :attributeId"
			+ " AND a.patient.patientUUID= :patientUUID"
			+ " AND a.effectiveDate IN (SELECT MAX(b.effectiveDate) FROM AttributeThresholdMaster b"
				+ " WHERE b.recordingDeviceAttribute.attributeId = :attributeId"
				+ " AND b.patient.patientUUID = :patientUUID"
				+ " AND b.effectiveDate<= :effectiveDate)"),

	@NamedQuery(name = "AttributeThresholdMaster.fetchPatientAttributeThresholds", query = "SELECT attributeThreshold FROM AttributeThresholdMaster attributeThreshold"
			+ " JOIN attributeThreshold.patient patient"
			+ " JOIN attributeThreshold.recordingDeviceAttribute recordingDeviceAttribute"
			+ " WHERE patient.patientUUID = :patientUUID"
			+ " AND recordingDeviceAttribute.attributeId = :attributeId"
			+ " ORDER BY attributeThreshold.effectiveDate ASC"),

	@NamedQuery(name = "AttributeThresholdMaster.fetchLatestAttributeThreshold", query = "SELECT attributeThreshold FROM AttributeThresholdMaster attributeThreshold"
			+ " JOIN attributeThreshold.patient patient"
			+ " JOIN attributeThreshold.recordingDeviceAttribute recordingDeviceAttribute"
			+ " WHERE patient.patientUUID = :patientUUID"
			+ " AND recordingDeviceAttribute.attributeName = :attributeName"
			+ " ORDER BY attributeThreshold.effectiveDate DESC")
})
//@formatter:on
public class AttributeThresholdMaster {

	@Id
	@SequenceGenerator(name = "ATTRIBUTE_THRESHOLD_ID_SEQUENCE_GENERATOR", sequenceName = "ATTRIBUTE_THRESHOLD_ATTRIBUTE_THRESHOLD_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ATTRIBUTE_THRESHOLD_ID_SEQUENCE_GENERATOR")
	@Column(name = "attribute_threshold_id")
	private Integer attributeThresholdId;

	@Column(name = "threshold_type")
	private String thresholdType;

	@Column(name = "effective_date")
	private Timestamp effectiveDate;

	@Column(name = "threshold_high_value")
	private String thresholdHighValue;

	@Column(name = "threshold_low_value")
	private String thresholdLowValue;

	@ManyToOne
	@JoinColumn(name = "patient_uuid")
	private PatientMaster patient;

	@ManyToOne
	@JoinColumn(name = "attribute_id")
	private RecordingDeviceAttributeMaster recordingDeviceAttribute;

	public AttributeThresholdMaster() {
	}

	public AttributeThresholdMaster(final Integer attributeThresholdId, final String thresholdType, final Timestamp effectiveDate, final String thresholdHighValue, final String thresholdLowValue, final PatientMaster patient, final RecordingDeviceAttributeMaster recordingDeviceAttribute) {
		this();
		this.attributeThresholdId = attributeThresholdId;
		this.thresholdType = thresholdType;
		this.effectiveDate = effectiveDate;
		this.thresholdHighValue = thresholdHighValue;
		this.thresholdLowValue = thresholdLowValue;
		this.patient = patient;
		this.recordingDeviceAttribute = recordingDeviceAttribute;
	}

	public Integer getAttributeThresholdId() {
		return attributeThresholdId;
	}

	public void setAttributeThresholdId(final Integer attributeThresholdId) {
		this.attributeThresholdId = attributeThresholdId;
	}

	public String getThresholdType() {
		return thresholdType;
	}

	public void setThresholdType(final String thresholdType) {
		this.thresholdType = thresholdType;
	}

	public Timestamp getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(final Timestamp effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public String getThresholdHighValue() {
		return thresholdHighValue;
	}

	public void setThresholdHighValue(final String thresholdHighValue) {
		this.thresholdHighValue = thresholdHighValue;
	}

	public String getThresholdLowValue() {
		return thresholdLowValue;
	}

	public void setThresholdLowValue(final String thresholdLowValue) {
		this.thresholdLowValue = thresholdLowValue;
	}

	public PatientMaster getPatient() {
		return patient;
	}

	public void setPatient(final PatientMaster patient) {
		this.patient = patient;
	}

	public RecordingDeviceAttributeMaster getRecordingDeviceAttribute() {
		return recordingDeviceAttribute;
	}

	public void setRecordingDeviceAttribute(final RecordingDeviceAttributeMaster recordingDeviceAttribute) {
		this.recordingDeviceAttribute = recordingDeviceAttribute;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (effectiveDate == null ? 0 : effectiveDate.hashCode());
		result = prime * result + (patient == null ? 0 : patient.hashCode());
		result = prime * result + (recordingDeviceAttribute == null ? 0 : recordingDeviceAttribute.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		AttributeThresholdMaster other = (AttributeThresholdMaster) obj;
		if(effectiveDate == null) {
			if(other.effectiveDate != null) {
				return false;
			}
		} else if(!effectiveDate.equals(other.effectiveDate)) {
			return false;
		}
		if(patient == null) {
			if(other.patient != null) {
				return false;
			}
		} else if(!patient.equals(other.patient)) {
			return false;
		}
		if(recordingDeviceAttribute == null) {
			if(other.recordingDeviceAttribute != null) {
				return false;
			}
		} else if(!recordingDeviceAttribute.equals(other.recordingDeviceAttribute)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AttributeThresholdMaster [attributeThresholdId=" + attributeThresholdId + ", thresholdType=" + thresholdType + ", effectiveDate=" + effectiveDate + ", thresholdHighValue=" + thresholdHighValue + ", thresholdLowValue=" + thresholdLowValue + ", patient=" + patient + ", recordingDeviceAttribute=" + recordingDeviceAttribute + "]";
	}
}