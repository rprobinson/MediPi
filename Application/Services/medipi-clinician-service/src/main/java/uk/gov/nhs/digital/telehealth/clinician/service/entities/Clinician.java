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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@SuppressWarnings("serial")
@Entity
@Table(name = "clinician_details")
public class Clinician implements Serializable {

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "clinician_uuid")
	private String clinicianId;

	@Column(name = "clinician_username")
	private String userName;

	@Column(name = "password")
	private String password;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@ManyToOne
	@JoinColumn(name = "patient_group_uuid")
	private PatientGroupMaster patientGroup;

	public Clinician() {

	}

	public Clinician(final Clinician clinician) {
		this.clinicianId = clinician.getClinicianId();
		this.firstName = clinician.getFirstName();
		this.lastName = clinician.getLastName();
		this.password = clinician.getPassword();
		this.patientGroup = clinician.getPatientGroup();
		this.userName = clinician.getUserName();
	}

	public String getClinicianId() {
		return clinicianId;
	}

	public void setClinicianId(final String clinicianId) {
		this.clinicianId = clinicianId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
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

	public PatientGroupMaster getPatientGroup() {
		return patientGroup;
	}

	public void setPatientGroup(final PatientGroupMaster patientGroup) {
		this.patientGroup = patientGroup;
	}

	@Override
	public String toString() {
		return "Clinician [clinicianId=" + clinicianId + ", firstName=" + firstName + ", lastName=" + lastName + ", patientGroup=" + patientGroup + "]";
	}
}