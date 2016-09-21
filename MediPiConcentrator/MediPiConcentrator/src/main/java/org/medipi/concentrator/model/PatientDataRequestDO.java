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
package org.medipi.concentrator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.medipi.concentrator.entities.RecordingDeviceData;

/**
 * This is the container data object for requests coming into the concentrator
 * from Clinical systems. 
 *
 * @author rick@robinsonhq.com
 */
public class PatientDataRequestDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String patientUuid;
    private List<RecordingDeviceData> recordingDeviceDataList = new ArrayList<>();

    public PatientDataRequestDO() {
    }

    public PatientDataRequestDO(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public PatientDataRequestDO(String patientUuid, List<RecordingDeviceData> recordingDeviceDataList) {
        this.patientUuid = patientUuid;
        this.recordingDeviceDataList = recordingDeviceDataList;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public List<RecordingDeviceData> getRecordingDeviceDataList() {
        return recordingDeviceDataList;
    }

    public void setRecordingDeviceDataList(List<RecordingDeviceData> RecordingDeviceDataList) {
        this.recordingDeviceDataList = RecordingDeviceDataList;
    }

    public void addRecordingDeviceData(RecordingDeviceData recordingDeviceData) {
        this.recordingDeviceDataList.add(recordingDeviceData);
    }

}
