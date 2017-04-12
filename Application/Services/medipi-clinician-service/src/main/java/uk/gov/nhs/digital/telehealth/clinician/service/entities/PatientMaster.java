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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

//@formatter:off
@Entity
@Table(name = "patient_details")
@NamedQueries({
	@NamedQuery(name = "PatientMaster.fetchAllPatients", query = "SELECT patientMaster FROM PatientMaster patientMaster ORDER BY lastName DESC"),
	@NamedQuery(name = "PatientMaster.fetchPatientsByPatientGroupId", query = "SELECT patientMaster FROM PatientMaster patientMaster inner join patientMaster.patientGroups patientGroup where patientGroup.patientGroupId = :patientGroupId ORDER BY patientMaster.lastName DESC")
})
//@formatter:on
public class PatientMaster {

	@Id
	@Column(name = "patient_uuid")
	private String patientUUID;

	@Column(name = "nhs_number")
	private String nhsNumber;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "dob")
	private Timestamp dateOfBirth;

	@OneToMany(mappedBy = "patient", cascade = {CascadeType.ALL})
	private List<RecordingDeviceDataMaster> recordingDeviceDataList;

	@OneToMany(mappedBy = "patient", cascade = {CascadeType.ALL})
	private List<AttributeThresholdMaster> attributeThresholds;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "patient", joinColumns = {@JoinColumn(name = "patient_uuid", nullable = false, updatable = false)}, inverseJoinColumns = {@JoinColumn(name = "patient_group_uuid", nullable = false, updatable = false)})
	private List<PatientGroupMaster> patientGroups;

	public PatientMaster() {
		recordingDeviceDataList = new ArrayList<RecordingDeviceDataMaster>();
		attributeThresholds = new ArrayList<AttributeThresholdMaster>();
		patientGroups = new ArrayList<PatientGroupMaster>();
	}

	public PatientMaster(final String patientUUID, final String nhsNumber, final String firstName, final String lastName, final Timestamp dateOfBirth) {
		this();
		this.patientUUID = patientUUID;
		this.nhsNumber = nhsNumber;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
	}

	public String getPatientUUID() {
		return patientUUID;
	}

	public void setPatientUUID(final String patientUUID) {
		this.patientUUID = patientUUID;
	}

	public String getNhsNumber() {
		return nhsNumber;
	}

	public void setNhsNumber(final String nhsNumber) {
		this.nhsNumber = nhsNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public Timestamp getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(final Timestamp dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public List<RecordingDeviceDataMaster> getRecordingDeviceDataList() {
		return recordingDeviceDataList;
	}

	public void addRecordingDeviceData(final RecordingDeviceDataMaster recordingDeviceData) {
		this.recordingDeviceDataList.add(recordingDeviceData);
	}

	public List<PatientGroupMaster> getPatientGroups() {
		return patientGroups;
	}

	public void addPatientGroups(final PatientGroupMaster patientGroup) {
		this.patientGroups.add(patientGroup);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (patientUUID == null ? 0 : patientUUID.hashCode());
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
		PatientMaster other = (PatientMaster) obj;
		if(patientUUID == null) {
			if(other.patientUUID != null) {
				return false;
			}
		} else if(!patientUUID.equals(other.patientUUID)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Patient [patientUUID=" + patientUUID + ", firstName=" + firstName + ", lastName=" + lastName + "]";
	}
}