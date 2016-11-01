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

import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceAttributeMaster;

import com.dev.ops.common.dao.generic.GenericDAOImpl;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Service("recordingDevicesAttributeDAO")
public class RecordingDeviceAttributeDAOImpl extends GenericDAOImpl<RecordingDeviceAttributeMaster> implements RecordingDeviceAttributeDAO {

	private static final Logger LOGGER = LogManager.getLogger(RecordingDeviceAttributeDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public RecordingDeviceAttributeMaster fetchRecordingDeviceAttributeByName(final String attributeName) throws DefaultWrappedException {
		LOGGER.debug("Get attribute for attributeName:<" + attributeName + ">");
		Query query = getEntityManager().createNamedQuery("RecordingDeviceAttributeMaster.fetchByAttributeName", RecordingDeviceAttributeMaster.class);
		query.setParameter("attributeName", attributeName);

		List<RecordingDeviceAttributeMaster> recordingDeviceAttributes = query.getResultList();
		if(recordingDeviceAttributes.isEmpty()) {
			throw new DefaultWrappedException("NO_RECORDING_DEVICE_ATTRIBUTE_FOUND_EXCEPTION", null, new Object[] {attributeName});
		} else if(recordingDeviceAttributes.size() > 1) {
			throw new DefaultWrappedException("MORE_THAN_ONE_RECORDING_DEVICE_ATTRIBUTE_FOUND_EXCEPTION", null, new Object[] {attributeName});
		} else {
			return recordingDeviceAttributes.get(0);
		}
	}

}