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
package org.medipi.concentrator.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity Class to manage DB access for alert
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "alert")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Alert.findAll", query = "SELECT a FROM Alert a"),
    @NamedQuery(name = "Alert.findByAlertId", query = "SELECT a FROM Alert a WHERE a.alertId = :alertId"),
    @NamedQuery(name = "Alert.findByAlertTime", query = "SELECT a FROM Alert a WHERE a.alertTime = :alertTime"),
    @NamedQuery(name = "Alert.findByAlertText", query = "SELECT a FROM Alert a WHERE a.alertText = :alertText")})
public class Alert implements Serializable {


    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "alert_id")
    private Long alertId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "alert_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date alertTime;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 5000)
    @Column(name = "alert_text")
    private String alertText;
    @Column(name = "transmit_success_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transmitSuccessDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "retry_attempts")
    private int retryAttempts;
    @JoinColumn(name = "patient_uuid", referencedColumnName = "patient_uuid")
    @ManyToOne(optional = false)
    private Patient patientUuid;
    @JoinColumn(name = "data_id", referencedColumnName = "data_id")
    @ManyToOne(optional = false)
    private RecordingDeviceData dataId;

    public Alert() {
    }

    public Alert(Long alertId) {
        this.alertId = alertId;
    }

    public Alert(Long alertId, Date alertTime, String alertText) {
        this.alertId = alertId;
        this.alertTime = alertTime;
        this.alertText = alertText;
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

    public Patient getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(Patient patientUuid) {
        this.patientUuid = patientUuid;
    }
    public RecordingDeviceData getDataId() {
        return dataId;
    }

    public void setDataId(RecordingDeviceData dataId) {
        this.dataId = dataId;
    }

    public Date getTransmitSuccessDate() {
        return transmitSuccessDate;
    }

    public void setTransmitSuccessDate(Date transmitSuccessDate) {
        this.transmitSuccessDate = transmitSuccessDate;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (alertId != null ? alertId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Alert)) {
            return false;
        }
        Alert other = (Alert) object;
        if ((this.alertId == null && other.alertId != null) || (this.alertId != null && !this.alertId.equals(other.alertId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.concentrator.entities.Alert[ alertId=" + alertId + " ]";
    }


    
}
