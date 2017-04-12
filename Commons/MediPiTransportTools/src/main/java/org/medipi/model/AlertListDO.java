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
package org.medipi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Object for a list of Alerts
 * @author rick@robinsonhq.com
 */
public class AlertListDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String patientUuid;
    private List<AlertDO> alertList = new ArrayList<>();

    public AlertListDO() {
    }

    public AlertListDO(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public AlertListDO(String patientUuid, List<AlertDO> alertList) {
        this.patientUuid = patientUuid;
        this.alertList = alertList;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public List<AlertDO> getAlert() {
        return alertList;
    }

    public void setAlert(List<AlertDO> lastDownload) {
        this.alertList = lastDownload;
    }

    public void addAlert(AlertDO alert) {
        this.alertList.add(alert);
    }



}
