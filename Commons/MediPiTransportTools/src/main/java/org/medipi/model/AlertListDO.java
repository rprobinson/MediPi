/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author riro
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
