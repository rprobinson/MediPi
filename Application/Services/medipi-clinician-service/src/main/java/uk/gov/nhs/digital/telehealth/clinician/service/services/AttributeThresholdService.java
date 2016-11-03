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

import ma.glasnost.orika.MapperFacade;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nhs.digital.telehealth.clinician.service.constants.ServiceConstants;
import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.AttributeThresholdDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.PatientDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.RecordingDeviceAttributeDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.AttributeThreshold;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.AttributeThresholdMaster;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.PatientMaster;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceAttributeMaster;

import com.dev.ops.common.utils.TimestampUtil;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Component
public class AttributeThresholdService {

	@Autowired
	private MapperFacade mapperFacade;

	@Autowired
	@Qualifier("attributeThresholdsDAO")
	private AttributeThresholdDAO attributeThresholdDAO;

	@Autowired
	@Qualifier("patientsDAO")
	private PatientDAO patientDAO;

	@Autowired
	@Qualifier("recordingDevicesAttributeDAO")
	private RecordingDeviceAttributeDAO recordingDeviceAttributeDAO;

	private static final Logger LOGGER = LogManager.getLogger(AttributeThresholdService.class);

	@Transactional(rollbackFor = {Exception.class})
	public AttributeThreshold getAttributeThreshold(final String patientUUID, final String attributeName) throws DefaultWrappedException {
		final AttributeThresholdMaster attributeThresholdMaster = this.attributeThresholdDAO.fetchLatestAttributeThreshold(patientUUID, attributeName);
		AttributeThreshold attributeThreshold = null;
		if(null != attributeThresholdMaster) {
			attributeThreshold = this.mapperFacade.map(attributeThresholdMaster, AttributeThreshold.class);
		}
		return attributeThreshold;
	}

	@Transactional(rollbackFor = {Exception.class})
	public AttributeThreshold saveAttributeThreshold(final AttributeThreshold attributeThreshold) throws DefaultWrappedException {
		AttributeThreshold returnThreshold = null;
		RecordingDeviceAttributeMaster recordingDeviceAttribute = recordingDeviceAttributeDAO.fetchRecordingDeviceAttributeByName(attributeThreshold.getAttributeName());

		PatientMaster patient = patientDAO.findByPrimaryKey(attributeThreshold.getPatientUUID());
		if(null == patient) {
			throw new DefaultWrappedException("PATIENT_WITH_ID_NOT_FOUND_EXCEPTION", null, new Object[] {attributeThreshold.getPatientUUID()});
		}

		final AttributeThresholdMaster latestAttributeThreshold = this.attributeThresholdDAO.fetchLatestAttributeThreshold(attributeThreshold.getPatientUUID(), attributeThreshold.getAttributeName());
		if(null != latestAttributeThreshold && latestAttributeThreshold.getThresholdLowValue().equals(attributeThreshold.getThresholdLowValue()) && latestAttributeThreshold.getThresholdHighValue().equals(attributeThreshold.getThresholdHighValue())) {
			//Do nothing and return the fetched attribute threshold as is because there are no changes in thresholds.
			returnThreshold = mapperFacade.map(latestAttributeThreshold, AttributeThreshold.class);
		} else {

			String thresholdType = null;
			if(ServiceConstants.Attributes.WEIGHT.equalsIgnoreCase(attributeThreshold.getAttributeName().trim())) {
				thresholdType = ServiceConstants.AttributeThresholdTypes.CHANGE_OVER_TIME_TEST;
			} else {
				thresholdType = ServiceConstants.AttributeThresholdTypes.SIMPLE_HIGH_LOW_INCLUSIVE_TEST;
			}

			AttributeThresholdMaster attributeThresholdMaster = new AttributeThresholdMaster(null, thresholdType, TimestampUtil.getCurentTimestamp(), attributeThreshold.getThresholdHighValue(), attributeThreshold.getThresholdLowValue(), patient, recordingDeviceAttribute);
			attributeThresholdDAO.save(attributeThresholdMaster);
			LOGGER.debug("Saved Attribute Threshold with id:<" + attributeThresholdMaster.getAttributeThresholdId() + ">");
			returnThreshold = mapperFacade.map(attributeThresholdMaster, AttributeThreshold.class);
		}
		return returnThreshold;
	}
}