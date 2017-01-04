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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.medipi.clinical.threshold.AttributeThresholdTest;
import org.medipi.clinical.threshold.ThresholdTestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.AttributeThresholdDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.PatientDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.RecordingDeviceDataDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.DataValue;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.Measurement;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.Patient;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.RecordingDeviceAttribute;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.enums.PatientStatus;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.AttributeThresholdMaster;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.DataValueEntity;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.PatientMaster;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceAttributeMaster;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceDataMaster;

import com.dev.ops.common.thread.local.ContextThreadLocal;
import com.dev.ops.common.utils.TimestampUtil;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Component
public class PatientService {

	@Autowired
	private MapperFacade mapperFacade;

	@Autowired
	@Qualifier("patientsDAO")
	private PatientDAO patientDAO;

	@Autowired
	@Qualifier("recordingDevicesDataDAO")
	private RecordingDeviceDataDAO recordingDeviceDataDAO;

	@Autowired
	@Qualifier("attributeThresholdsDAO")
	private AttributeThresholdDAO attributeThresholdDAO;

	@Autowired
	private ThresholdTestFactory thresholdTestFactory;

	@Value("#{'${medipi.patient.required.device.attributes}'.split(',')}")
	private List<String> requiredDeviceAttributes;

	private static final Logger LOGGER = LogManager.getLogger(PatientService.class);

	@Transactional(rollbackFor = {Exception.class})
	public Patient getPatientDetails(final String patientUUID) throws DefaultWrappedException {
		final PatientMaster patientMaster = this.patientDAO.findByPrimaryKey(patientUUID, ContextThreadLocal.get());
		Patient patientDetails = null;
		if(null != patientMaster) {
			patientDetails = this.mapperFacade.map(patientMaster, Patient.class);
		} else {
			throw new DefaultWrappedException("PATIENT_WITH_ID_NOT_FOUND_EXCEPTION", null, new Object[] {patientUUID});
		}
		return patientDetails;
	}

	@Transactional(rollbackFor = {Exception.class})
	public List<Patient> getAllPatients() throws DefaultWrappedException {
		final List<Patient> patients = new ArrayList<Patient>();
		final List<PatientMaster> patientMasters = this.patientDAO.fetchAllPatients();
		for(final PatientMaster patientMaster : patientMasters) {
			final Patient patient = this.mapperFacade.map(patientMaster, Patient.class);
			List<DataValueEntity> patientMeasurements = recordingDeviceDataDAO.fetchRecentMeasurementsSQL(patient.getPatientUUID());

			//Clone the required attributes so that we can compare whether all the required attributes has data against it
			List<String> requiredAttributes = new ArrayList<String>(requiredDeviceAttributes);

			//Set the default status as incomplete schedule
			PatientStatus patientStatus = PatientStatus.INCOMPLETE_SCHEDULE;

			for(DataValueEntity patientMeasurement : patientMeasurements) {
				//Check the alert status and scheduled expiry date only if they are listed as required/mandatory attributes.
				if(requiredAttributes.contains(patientMeasurement.getAttributeName())) {
					requiredAttributes.remove(patientMeasurement.getAttributeName());

					//break if the patient did not submit the data after the last expiry schedule time
					if(patientMeasurement.getScheduleExpiryTime().before(TimestampUtil.getCurentTimestamp())) {
						break;
					} else if(patientMeasurement.getAlertStatus() != null && PatientStatus.valueOf(patientMeasurement.getAlertStatus()) == PatientStatus.OUT_OF_THRESHOLD) {
						//if the alert status is out of threshold for any single required attribute then set patient status as OUT_OF_THRESHOLD and break. No need to check the data for next attribute
						patientStatus = PatientStatus.OUT_OF_THRESHOLD;
						break;
					} else if(patientMeasurement.getAlertStatus() != null && PatientStatus.valueOf(patientMeasurement.getAlertStatus()) == PatientStatus.CANNOT_CALCULATE) {
						//if the alert status is out of threshold for any single required attribute then set patient status as OUT_OF_THRESHOLD and break. No need to check the data for next attribute
						patientStatus = PatientStatus.CANNOT_CALCULATE;
						break;
					}
				}
			}

			if(requiredAttributes.isEmpty()) {
				patientStatus = PatientStatus.IN_THRESHOLD;
			}

			patient.setPatientStatus(patientStatus);
			patients.add(patient);
		}
		return patients;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Transactional(rollbackFor = {Exception.class})
	public List<DataValue> getPatientsRecentMeasurements(final String patientUUID) {
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

		/*List<RecordingDeviceDataMaster> recordingDeviceDataList = recordingDeviceDataDAO.fetchRecentMeasurementsHQL(patientUUID, contextInfo);
		return this.mapperFacade.map(recordingDeviceDataList, (Class<List<DataValue>>) (Class) List.class);*/

		List<DataValueEntity> recordingDeviceDataList = recordingDeviceDataDAO.fetchRecentMeasurementsSQL(patientUUID);
		return this.mapperFacade.map(recordingDeviceDataList, (Class<List<DataValue>>) (Class) List.class);
	}

	public List<Measurement> getPatientMeasurements(final String patientUUID, final Integer attributeId) throws Exception {
		/*List<AttributeThresholdMaster> attributeThresholdMasterList = attributeThresholdDAO.fetchPatientAttributeThresholds(patientUUID, attributeId);
		List<AttributeThreshold> attributeThresholds = new ArrayList<AttributeThreshold>();
		for(AttributeThresholdMaster attributeThresholdMaster : attributeThresholdMasterList) {
			AttributeThreshold attributeThreshold = this.mapperFacade.map(attributeThresholdMaster, AttributeThreshold.class);
			AttributeThresholdTest thresholdTest = thresholdTestFactory.getInstance(attributeThreshold.getThresholdType());
			RecordingDeviceData recordingDeviceData = this.mapperFacade.map(attributeThresholdMaster.g, destinationClass)
			thresholdTest.getThreshold(rdd)
			attributeThresholds.add(attributeThreshold);
		}*/

		List<RecordingDeviceDataMaster> patientData = recordingDeviceDataDAO.fetchPatientMeasurementsByAttribute(patientUUID, attributeId);
		List<Measurement> measurements = new ArrayList<Measurement>();
		for(RecordingDeviceDataMaster data : patientData) {
			Measurement measurement = this.mapperFacade.map(data, Measurement.class);
			AttributeThresholdMaster attributeThresholdMaster = attributeThresholdDAO.findEffectiveAttributeThreshold(data.getRecordingDeviceAttribute().getAttributeId(), patientUUID, data.getDataValueTime());

			if(attributeThresholdMaster != null) {
				AttributeThresholdTest thresholdTest = thresholdTestFactory.getInstance(attributeThresholdMaster.getThresholdType());
				List<Double> thresholds = thresholdTest.getThreshold(data.getRecordingDeviceAttribute().getAttributeId(), patientUUID, data.getDataValueTime(), data.getDataValue());
				if(thresholds != null && (thresholds.get(0) == null || thresholds.get(1) == null)) {
					LOGGER.debug("AttributeThresholdMaster:<thresholdLowValue=" + attributeThresholdMaster.getThresholdLowValue() + " thresholdHighValue: " + attributeThresholdMaster.getThresholdHighValue() + "> thresholds:<" + thresholds + ">");
				}
				measurement.setMinMaxValues(thresholds);
			}
			measurements.add(measurement);
		}
		return measurements;
	}

	@Transactional(rollbackFor = {Exception.class})
	public List<RecordingDeviceAttribute> getPatientAttributesWithDevices(final String patientUUID, final List<String> attributeNames) {
		List<RecordingDeviceAttribute> recordingDeviceAttributes = new ArrayList<RecordingDeviceAttribute>();
		List<RecordingDeviceAttributeMaster> patientAttributesWithData = recordingDeviceDataDAO.fetchPatientAttributesHavingData(patientUUID, attributeNames);
		for(RecordingDeviceAttributeMaster patientAttribute : patientAttributesWithData) {
			recordingDeviceAttributes.add(mapperFacade.map(patientAttribute, RecordingDeviceAttribute.class));
		}
		return recordingDeviceAttributes;
	}
}