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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.nhs.digital.telehealth.clinician.service.entities.DataValueEntity;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceDataMaster;

import com.dev.ops.common.dao.generic.GenericDAOImpl;
import com.dev.ops.common.thread.local.ContextThreadLocal;

@Service("recordingDevicesDataDAO")
public class RecordingDeviceDataDAOImpl extends GenericDAOImpl<RecordingDeviceDataMaster> implements RecordingDeviceDataDAO {

	private static final Logger LOGGER = LogManager.getLogger(RecordingDeviceDataDAOImpl.class);

	private static String FETCH_RECENT_MEASUREMENTS_NATIVE_POSTGRESQL_QUERY;

	static {
		StringBuilder query = new StringBuilder();
		query.append("SELECT rdd.data_id as \"data_id\", rdt.type as \"reading_type\", rdt.display_name as \"device\", rda.attribute_name as \"attribute_name\", rda.attribute_id as \"attribute_id\",");
		query.append(" rdd.data_value as \"data\", rdt.type_id as \"type_id\", rdd.data_value_time as \"data_time\", rdd.downloaded_time as \"submitted_time\",");
		query.append(" rdd.schedule_effective_time as \"schedule_effective_time\", rdd.schedule_expiry_time as \"schedule_expiry_time\", rdd.alert_status as \"alert_status\"");
		query.append(" FROM recording_device_data rdd");
		query.append(" JOIN recording_device_attribute rda ON rdd.attribute_id = rda.attribute_id");
		query.append(" JOIN recording_device_type rdt ON rda.type_id = rdt.type_id");
		query.append(" JOIN  (SELECT MAX(rddd.data_value_time) as data_value_time, rddd.attribute_id as attribute_id FROM recording_device_data rddd");
		query.append(" WHERE patient_uuid = :patientUUID");
		query.append(" GROUP BY rddd.attribute_id) latest_device_data ON rdd.attribute_id = latest_device_data.attribute_id AND rdd.data_value_time = latest_device_data.data_value_time");
		query.append(" WHERE rdd.patient_uuid = :patientUUID");
		query.append(" ORDER BY rdt.type_id ASC");

		FETCH_RECENT_MEASUREMENTS_NATIVE_POSTGRESQL_QUERY = query.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RecordingDeviceDataMaster> fetchRecentMeasurementsHQL(final String patientUUID) {
		LOGGER.debug("Get recent measurements for patient:" + patientUUID + " " + ContextThreadLocal.get());
		final Query query = this.getEntityManager().createNamedQuery("RecordingDeviceDataMaster.fetchRecentMeasurements", RecordingDeviceDataMaster.class);
		query.setParameter("patientUUID", patientUUID);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DataValueEntity> fetchRecentMeasurementsSQL(final String patientUUID) {
		LOGGER.debug("Get recent measurements for patient:" + patientUUID + " " + ContextThreadLocal.get());
		final Query query = this.getEntityManager().createNativeQuery(FETCH_RECENT_MEASUREMENTS_NATIVE_POSTGRESQL_QUERY, DataValueEntity.class);
		query.setParameter("patientUUID", patientUUID);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RecordingDeviceDataMaster> fetchPatientMeasurementsByAttribute(final String patientUUID, final String attributeName) {
		final Query query = this.getEntityManager().createNamedQuery("RecordingDeviceDataMaster.fetchPatientMeasurementsByAttributeName", RecordingDeviceDataMaster.class);
		query.setParameter("patientUUID", patientUUID);
		query.setParameter("attributeName", attributeName);
		return query.getResultList();
	}
}