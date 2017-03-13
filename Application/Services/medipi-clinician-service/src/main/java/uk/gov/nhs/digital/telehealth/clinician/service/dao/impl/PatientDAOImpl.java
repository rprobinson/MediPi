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
package uk.gov.nhs.digital.telehealth.clinician.service.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import uk.gov.nhs.digital.telehealth.clinician.service.entities.PatientMaster;

import com.dev.ops.common.dao.generic.GenericDAOImpl;

@Service("patientsDAO")
public class PatientDAOImpl extends GenericDAOImpl<PatientMaster> implements PatientDAO {

	@Override
	public List<PatientMaster> fetchAllPatients() {
		return this.getEntityManager().createNamedQuery("PatientMaster.fetchAllPatients", PatientMaster.class).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PatientMaster> fetchPatientsByPatientGroupId(final String patientGroupId) {
		Query query = getEntityManager().createNamedQuery("PatientMaster.fetchPatientsByPatientGroupId", PatientMaster.class);
		query.setParameter("patientGroupId", patientGroupId);
		return query.getResultList();
	}

}