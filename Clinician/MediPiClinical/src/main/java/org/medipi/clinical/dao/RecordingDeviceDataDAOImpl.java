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
import java.util.List;
import org.medipi.clinical.entities.RecordingDeviceData;
import org.medipi.clinical.entities.Patient;
import org.medipi.clinical.entities.PatientGroup;
import org.medipi.clinical.entities.RecordingDeviceAttribute;
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
    public Date dateOfLatestMeasurement(Patient patient, String type) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceData.dateOfLatestMeasurement", Date.class)
                .setParameter("patientUuid", patient)
                .setParameter("type", type)
                .getSingleResult();

    }

    @Override
    public Date dateOfLatestPatientGroupSync(PatientGroup patientGroup) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceData.dateOfLatestPatientGroupSync", Date.class)
                .setParameter("patientGroup", patientGroup)
                .getSingleResult();

    }

    @Override
    public Date findByGroupedPatientAndScheduledTime(Patient patient, Date scheduleEffectiveTime) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceData.findByGroupedPatientAndScheduledTime", Date.class)
                .setParameter("patientUuid", patient)
                .setParameter("scheduleEffectiveTime", scheduleEffectiveTime)
                .getSingleResult();

    }

    @Override
    public RecordingDeviceData findByPatientAndScheduledTime(Patient patient, Date scheduleEffectiveTime, Date scheduleExpiryTime) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceData.findByPatientAndScheduledTime", RecordingDeviceData.class)
                .setParameter("patientUuid", patient)
                .setParameter("scheduleEffectiveTime", scheduleEffectiveTime)
                .setParameter("scheduleExpiryTime", scheduleExpiryTime)
                .getSingleResult();
    }

    @Override
    public List<RecordingDeviceData> findByPatientAndAttributeAndPeriod(String patientUuid, int attributeId, Date periodStartTime, Date periodEndTime) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceData.findByPatientAndAttributeAndPeriod", RecordingDeviceData.class)
                .setParameter("patientUuid", patientUuid)
                .setParameter("attributeId", attributeId)
                .setParameter("periodStartTime", periodStartTime)
                .setParameter("periodEndTime", periodEndTime)
                .getResultList();
    }

    @Override
    public Date findFirstEntryBeforePeriod(String patientUuid, int attributeId, Date periodStartTime) {
        return this.getEntityManager().createNamedQuery("RecordingDeviceData.findFirstEntryBeforePeriod", Date.class)
                .setParameter("patientUuid", patientUuid)
                .setParameter("attributeId", attributeId)
                .setParameter("periodStartTime", periodStartTime)
                .getSingleResult();
    }

}
