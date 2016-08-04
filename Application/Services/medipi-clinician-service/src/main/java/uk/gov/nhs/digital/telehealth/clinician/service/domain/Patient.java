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
package uk.gov.nhs.digital.telehealth.clinician.service.domain;

import java.sql.Timestamp;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.enums.PatientStatus;

public class Patient {

	private String patientId;
	private String nhsNumber;
	private String firstName;
	private String lastName;
	private Timestamp dateOfBirth;
	private PatientStatus patientStatus;

	public Patient() {
		setPatientStatus(PatientStatus.INCOMPLETE_SCHEDULE);
	}

	public Patient(final String patientId, final String nhsNumber, final String firstName, final String lastName, final Timestamp dateOfBirth, final PatientStatus patientStatus) {
		this();
		this.patientId = patientId;
		this.nhsNumber = nhsNumber;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
		setPatientStatus(patientStatus);
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(final String patientId) {
		this.patientId = patientId;
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

	public PatientStatus getPatientStatus() {
		return patientStatus;
	}

	public void setPatientStatus(final PatientStatus patientStatus) {
		if(patientStatus != null) {
			this.patientStatus = patientStatus;
		} else {
			this.patientStatus = PatientStatus.INCOMPLETE_SCHEDULE;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (patientId == null ? 0 : patientId.hashCode());
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
		Patient other = (Patient) obj;
		if(patientId == null) {
			if(other.patientId != null) {
				return false;
			}
		} else if(!patientId.equals(other.patientId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Patient [patientId=" + patientId + ", firstName=" + firstName + ", lastName=" + lastName + ", patientStatus=" + patientStatus + "]";
	}
}