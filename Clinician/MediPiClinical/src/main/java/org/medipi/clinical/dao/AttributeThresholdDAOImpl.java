/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.clinical.dao;

import java.util.Date;
import org.medipi.clinical.entities.AttributeThreshold;
import org.springframework.stereotype.Repository;

/**
 * Implementation of data access object for RecordingDeviceAttribute
 *
 * @author rick@robinsonhq.com
 */
@Repository
public class AttributeThresholdDAOImpl extends GenericDAOImpl<AttributeThreshold> implements AttributeThresholdDAO {

    @Override
    public AttributeThreshold findLatestByAttributeAndPatientAndDate(int attributeId, String patientUuid, Date measurementDate) {
        return this.getEntityManager().createNamedQuery("AttributeThreshold.findByAttribute", AttributeThreshold.class)
                .setParameter("attributeId", attributeId)
                .setParameter("patientUuid", patientUuid)
                .setParameter("measurementDate", measurementDate)
                .getSingleResult();
    }


}
