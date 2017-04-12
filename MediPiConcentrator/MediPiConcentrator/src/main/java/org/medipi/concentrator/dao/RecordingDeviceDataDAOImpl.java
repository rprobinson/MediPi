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

import java.util.Date;
import java.util.List;
import org.medipi.concentrator.entities.RecordingDeviceData;
import org.medipi.concentrator.entities.Patient;
import org.medipi.concentrator.entities.RecordingDeviceAttribute;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the RecordingDeviceData data access object
 * 
 * @author rick@robinsonhq.com
 */
@Repository
public class RecordingDeviceDataDAOImpl extends GenericDAOImpl<RecordingDeviceData> implements RecordingDeviceDataDAO {

    @Override
    public List<RecordingDeviceData> isAlreadyStored(RecordingDeviceAttribute rda, Patient patient, String data, Date dataPointTime) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceData.isAlreadyStored", RecordingDeviceData.class)
                .setParameter("attributeId", rda)
                .setParameter("patientUuid", patient)
                .setParameter("dataValue", data)
                .setParameter("dataValueTime", dataPointTime)
                .getResultList();

    }
    @Override
    public List<RecordingDeviceData> findByPatientUuidAfterDate(String patientUuid, Date requestDate, String type) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceData.findByPatientUuidAfterDate", RecordingDeviceData.class)
                .setParameter("patientUuid", patientUuid)
                .setParameter("requestDate", requestDate)
                .setParameter("type", type)
                .getResultList();

    }
    @Override
    public RecordingDeviceData findByTypeAttributeAndData(String patientUuid, String type, String AttributeName, Date dataValueTime, String dataValue){
            return this.getEntityManager().createNamedQuery("RecordingDeviceData.findByTypeAttributeAndData", RecordingDeviceData.class)
                .setParameter("patientUuid", patientUuid)
                .setParameter("type", type)
                .setParameter("attributeName", AttributeName)
                .setParameter("dataValueTime", dataValueTime)
                .setParameter("dataValue", dataValue)
                .getSingleResult();

    }   
    @Override
    public List<RecordingDeviceData> findByPatientAndDownloadedTime(String patientUuid, Date downloadedTime, Date endTime){
            return this.getEntityManager().createNamedQuery("RecordingDeviceData.findByPatientAndDownloadedTime", RecordingDeviceData.class)
                .setParameter("patientUuid", patientUuid)
                .setParameter("downloadedTime", downloadedTime)
                .setParameter("endTime", endTime)
                .getResultList();

    }
}
