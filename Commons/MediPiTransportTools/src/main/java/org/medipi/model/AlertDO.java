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
import java.util.Date;

/**
 * Data Object for Alerts
 * @author rick@robinsonhq.com
 */
public class AlertDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long alertId;
    private Date alertTime;
    private String alertText;
    private String patientUuid;
    private String dataValue;
    private Date dataValueTime;
    private String attributeName;
    private String type;
    private String make;
    private String model;
    private String status;
    
    private Date transmitSuccessDate;

    public AlertDO() {
    }

    public AlertDO(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public Date getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Date alertTime) {
        this.alertTime = alertTime;
    }

    public String getAlertText() {
        return alertText;
    }

    public void setAlertText(String alertText) {
        this.alertText = alertText;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public Date getDataValueTime() {
        return dataValueTime;
    }

    public void setDataValueTime(Date dataValueTime) {
        this.dataValueTime = dataValueTime;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTransmitSuccessDate() {
        return transmitSuccessDate;
    }

    public void setTransmitSuccessDate(Date transmitSuccessDate) {
        this.transmitSuccessDate = transmitSuccessDate;
    }


}
