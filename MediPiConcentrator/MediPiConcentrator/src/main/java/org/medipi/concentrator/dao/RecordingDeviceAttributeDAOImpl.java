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
package org.medipi.concentrator.dao;

import org.medipi.concentrator.entities.RecordingDeviceAttribute;
import org.medipi.concentrator.entities.RecordingDeviceType;
import org.springframework.stereotype.Repository;

/**
 * Implementation of data access object for RecordingDeviceAttribute
 *
 * @author rick@robinsonhq.com
 */
@Repository
public class RecordingDeviceAttributeDAOImpl extends GenericDAOImpl<RecordingDeviceAttribute> implements RecordingDeviceAttributeDAO {

    @Override
    public RecordingDeviceAttribute findByTypeUnitsFormatAndAttributeName(RecordingDeviceType typeId, String column, String units, String format) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceAttribute.findByTypeUnitsFormatAndAttributeName", RecordingDeviceAttribute.class)
                .setParameter("attributeName", column)
                .setParameter("typeId", typeId)
                .setParameter("units", units)
                .setParameter("format", format)
                .getSingleResult();
    }
}
