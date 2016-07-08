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
package uk.gov.nhs.digital.telehealth.clinician.service.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.MapperFacade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.PatientDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.Patient;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.PatientMaster;

import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Component
public class PatientService {

	@Autowired
	private MapperFacade mapperFacade;

	@Autowired
	private PatientDAO patientDAO;

	@Transactional(rollbackFor = {Exception.class})
	public Patient getPatientDetails(final BigDecimal patientId, final ContextInfo contextInfo) throws DefaultWrappedException {
		final PatientMaster patientMaster = this.patientDAO.findByPrimaryKey(patientId, contextInfo);
		Patient patientDetails = null;
		if(null != patientMaster) {
			patientDetails = this.mapperFacade.map(patientMaster, Patient.class);
		} else {
			throw new DefaultWrappedException("PATIENT_WITH_ID_NOT_FOUND_EXCEPTION", null, new Object[] {patientId});
		}
		return patientDetails;
	}

	@Transactional(rollbackFor = {Exception.class})
	public List<Patient> getAllPatients(final ContextInfo contextInfo) throws DefaultWrappedException {
		final List<Patient> patients = new ArrayList<Patient>();
		final List<PatientMaster> patientMasters = this.patientDAO.fetchAllPatients(contextInfo);
		for(final PatientMaster patientMaster : patientMasters) {
			final Patient patient = this.mapperFacade.map(patientMaster, Patient.class);
			patients.add(patient);
		}
		return patients;
	}
}