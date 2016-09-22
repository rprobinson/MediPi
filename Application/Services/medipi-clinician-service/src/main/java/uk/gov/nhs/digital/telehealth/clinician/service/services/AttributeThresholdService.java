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

import uk.gov.nhs.digital.telehealth.clinician.service.dao.impl.AttributeThresholdDAO;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.AttributeThreshold;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.AttributeThresholdMaster;

import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Component
public class AttributeThresholdService {

	@Autowired
	private MapperFacade mapperFacade;

	@Autowired
	@Qualifier("attributeThresholdsDAO")
	private AttributeThresholdDAO attributeThresholdDAO;

	private static final Logger LOGGER = LogManager.getLogger(AttributeThresholdService.class);

	@Transactional(rollbackFor = {Exception.class})
	public AttributeThreshold getAttributeThreshold(final String patientUUID, final String attributeName) throws DefaultWrappedException {
		final AttributeThresholdMaster attributeThresholdMaster = this.attributeThresholdDAO.fetchLatestAttributeThreshold(patientUUID, attributeName);
		AttributeThreshold attributeThreshold = null;
		if(null != attributeThresholdMaster) {
			attributeThreshold = this.mapperFacade.map(attributeThresholdMaster, AttributeThreshold.class);
		} else {
			throw new DefaultWrappedException("ATTRIBUTE_THRESHOLD_WITH_ATTRIBUTE_NAME_NOT_FOUND_EXCEPTION", null, new Object[] {attributeName, patientUUID});
		}
		return attributeThreshold;
	}
}