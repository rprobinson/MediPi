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

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import uk.gov.nhs.digital.telehealth.clinician.service.entities.AttributeThresholdMaster;

import com.dev.ops.common.dao.generic.GenericDAOImpl;

@Service("attributeThresholdsDAO")
public class AttributeThresholdDAOImpl extends GenericDAOImpl<AttributeThresholdMaster> implements AttributeThresholdDAO {

	private static final Logger LOGGER = LogManager.getLogger(AttributeThresholdDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<AttributeThresholdMaster> fetchPatientAttributeThresholds(final String patientUUID, final Integer attributeId) {
		/*String queryString = "SELECT attributeThreshold FROM AttributeThresholdMaster attributeThreshold"
				+ " JOIN attributeThreshold.patient patient"
				+ " JOIN attributeThreshold.recordingDeviceAttribute recordingDeviceAttribute"
				+ " WHERE patient.patientUUID = :patientUUID"
				+ " AND recordingDeviceAttribute.attributeId = :attributeId"
				+ " ORDER BY attributeThreshold.effectiveDate ASC";
		final Query query = this.getEntityManager().createNamedQuery(queryString, AttributeThresholdMaster.class);*/

		final Query query = this.getEntityManager().createNamedQuery("AttributeThresholdMaster.fetchPatientAttributeThresholds", AttributeThresholdMaster.class);
		query.setParameter("patientUUID", patientUUID);
		query.setParameter("attributeId", attributeId);
		return query.getResultList();
	}

	@Override
	public AttributeThresholdMaster findEffectiveAttributeThreshold(final int attributeId, final String patientUUID, final Timestamp effectiveDate) {
		final Query query = this.getEntityManager().createNamedQuery("AttributeThresholdMaster.findEffectiveAttributeThreshold", AttributeThresholdMaster.class);
		query.setParameter("attributeId", attributeId);
		query.setParameter("patientUUID", patientUUID);
		query.setParameter("effectiveDate", effectiveDate);

		AttributeThresholdMaster attributeThreshold = null;
		try {
			attributeThreshold = (AttributeThresholdMaster) query.getSingleResult();
		} catch(NoResultException e) {
			LOGGER.error("No attribute threshold found for patientUUID:<" + patientUUID + ">, attributeId:<" + attributeId + "> and effectiveDate:<" + effectiveDate + ">", e);
		}
		return attributeThreshold;
	}

	@Override
	public AttributeThresholdMaster fetchLatestAttributeThreshold(final String patientUUID, final Integer attributeId) {
		final Query query = this.getEntityManager().createNamedQuery("AttributeThresholdMaster.fetchLatestAttributeThreshold", AttributeThresholdMaster.class);
		query.setParameter("attributeId", attributeId);
		query.setParameter("patientUUID", patientUUID);
		query.setMaxResults(1);

		AttributeThresholdMaster attributeThreshold = null;
		try {
			attributeThreshold = (AttributeThresholdMaster) query.getSingleResult();
		} catch(NoResultException e) {
			LOGGER.error("No attribute threshold set for patientUUID:<" + patientUUID + "> and attributeId:<" + attributeId + ">", e);
		}
		return attributeThreshold;
	}
}