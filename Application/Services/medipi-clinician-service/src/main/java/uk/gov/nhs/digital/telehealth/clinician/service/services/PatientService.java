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

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.MapperFacade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.PatientDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.RecordingDeviceDataDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.DataValue;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.Patient;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.DataValueEntity;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.PatientMaster;

import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Component
public class PatientService {

	@Autowired
	private MapperFacade mapperFacade;

	@Autowired
	private PatientDAO patientDAO;

	@Autowired
	private RecordingDeviceDataDAO recordingDeviceDataDAO;

	@Transactional(rollbackFor = {Exception.class})
	public Patient getPatientDetails(final String patientId, final ContextInfo contextInfo) throws DefaultWrappedException {
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Transactional(rollbackFor = {Exception.class})
	public List<DataValue> getPatientsRecentReadings(final String patientId, final ContextInfo contextInfo) {
		/*
		 * As per the conversation with Richard on 11/07/2016 below are the 2 options suggested for fetching the recent data readings
		 *
		 * 1. To work with hibernate hql we can have JOINs only on the pre-defined tables/entities. We can't have joins with derived tables
		 * but we can have subqueries. To use subqueries, we need to provide primary/unique key. As downloaded time is neither primary nor unique
		 * hence we cannot use it in the subquery. Instead we need to use data_id as the key to fetch results in subquery.
		 *
		 * 2. Write native sql query which can have derived tables in the JOINs. The drawback with writing native queries is we would face problems
		 * in database portability.
		 *
		 * After discussion with Richard we have agreed to go with option 2 because we are not using data_id as a fundamental way of identifying
		 * which is the latest entry and I think it would be dodgy to have 2 differing ways of deriving the last value.
		 *
		 * In case if we want to go with option 1 then just uncomment below 2 lines and comment last 2 lines.
		 */

		/*List<RecordingDeviceDataMaster> recordingDeviceDataList = recordingDeviceDataDAO.fetchRecentReadingsHQL(patientId, contextInfo);
		return this.mapperFacade.map(recordingDeviceDataList, (Class<List<DataValue>>) (Class) List.class);*/

		List<DataValueEntity> recordingDeviceDataList = recordingDeviceDataDAO.fetchRecentReadingsSQL(patientId, contextInfo);
		return this.mapperFacade.map(recordingDeviceDataList, (Class<List<DataValue>>) (Class) List.class);
	}
}