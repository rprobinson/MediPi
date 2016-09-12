/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author riro
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

    public Date getTransmitSuccessDate() {
        return transmitSuccessDate;
    }

    public void setTransmitSuccessDate(Date transmitSuccessDate) {
        this.transmitSuccessDate = transmitSuccessDate;
    }


}
