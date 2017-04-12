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

/**
 * Data Access Object interface for RecordingDeviceData
 *
 * @author rick@robinsonhq.com
 */
public interface RecordingDeviceDataDAO extends GenericDAO<RecordingDeviceData> {

    /**
     * Method to check if a data point has already been persisted to the DB
     *
     * @param rda recordingDeviceAttribute
     * @param patient patient
     * @param data data value of the data point
     * @param dataPointTime time at which the measurement was taken
     * @return
     */
    public List<RecordingDeviceData> isAlreadyStored(RecordingDeviceAttribute rda, Patient patient, String data, Date dataPointTime);

    public List<RecordingDeviceData> findByPatientUuidAfterDate(String patientUuid, Date requestDate, String type);

    public RecordingDeviceData findByTypeAttributeAndData(String patientUuid, String type, String AttributeName, Date dataValueTime, String dataValue);

    public List<RecordingDeviceData> findByPatientAndDownloadedTime(String patientUuid, Date downloadedTime, Date endTime);
}
