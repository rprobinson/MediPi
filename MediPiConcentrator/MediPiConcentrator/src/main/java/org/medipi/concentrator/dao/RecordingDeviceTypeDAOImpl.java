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

import java.util.List;
import org.medipi.concentrator.entities.RecordingDeviceType;
import org.springframework.stereotype.Repository;

/**
 * Implementation of data access object for RecordingDeviceType

 * @author rick@robinsonhq.com
 */
@Repository
public class RecordingDeviceTypeDAOImpl extends GenericDAOImpl<RecordingDeviceType> implements RecordingDeviceTypeDAO {

    @Override
    public RecordingDeviceType findByTypeMakeModelDisplayName(String type, String make, String model, String displayName) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceType.findByTypeMakeModelDisplayName", RecordingDeviceType.class)
                .setParameter("make", make)
                .setParameter("model", model)
                .setParameter("displayname", displayName)
                .setParameter("type", type)
                .getSingleResult();
    }       
    @Override
    public List<String> findByPatient(String patientUuid) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceType.findByPatient", String.class)
                .setParameter("patientUuid", patientUuid)
                .getResultList();
    }           
    @Override
    public RecordingDeviceType findByType(String type) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceType.findByType", RecordingDeviceType.class)
                .setParameter("type", type)
                .getSingleResult();
    }
}