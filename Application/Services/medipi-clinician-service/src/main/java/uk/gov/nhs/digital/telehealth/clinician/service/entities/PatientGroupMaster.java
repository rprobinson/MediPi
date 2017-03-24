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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

//@formatter:off
@Entity
@Table(name = "patient_group")
@NamedQueries({
	@NamedQuery(name = "PatientGroupMaster.fetchAllPatientGroups", query = "SELECT patientGroup FROM PatientGroupMaster patientGroup"),
})
//@formatter:on
public class PatientGroupMaster {

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "patient_group_uuid")
	private String patientGroupId;

	@Column(name = "patient_group_name")
	private String patientGroupName;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "patientGroups")
	private List<PatientMaster> patientDetails;

	@OneToMany(mappedBy = "patientGroup", cascade = {CascadeType.ALL})
	private List<Clinician> clinicians;

	public PatientGroupMaster() {
		patientDetails = new ArrayList<PatientMaster>();
	}

	public String getPatientGroupId() {
		return patientGroupId;
	}

	public void setPatientGroupId(final String patientGroupId) {
		this.patientGroupId = patientGroupId;
	}

	public String getPatientGroupName() {
		return patientGroupName;
	}

	public void setPatientGroupName(final String patientGroupName) {
		this.patientGroupName = patientGroupName;
	}

	public List<PatientMaster> getPatientDetails() {
		return patientDetails;
	}

	public void addPatientDetails(final PatientMaster patientDetail) {
		this.patientDetails.add(patientDetail);
	}

	public List<Clinician> getClinicians() {
		return clinicians;
	}

	public void addClinician(final Clinician clinician) {
		this.clinicians.add(clinician);
	}
}