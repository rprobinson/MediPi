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
import org.hibernate.annotations.GenerationTime;

/**
 * Entity class encapsulating RecordingDeviceData database table
 *
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "recording_device_data")
@NamedQueries({
    //Added
    @NamedQuery(name = "RecordingDeviceData.isAlreadyStored", query = "SELECT d FROM RecordingDeviceData d WHERE d.attributeId = :attributeId AND d.dataValue = :dataValue AND d.dataValueTime = :dataValueTime AND d.patientUuid = :patientUuid"),
    @NamedQuery(name = "RecordingDeviceData.findBypatientUuidAfterDate", query = "SELECT d FROM RecordingDeviceData d, RecordingDeviceAttribute a, RecordingDeviceType t WHERE d.attributeId = a.attributeId AND a.typeId =t.typeId AND d.patientUuid.patientUuid = :patientUuid AND d.dataValueTime > :requestDate AND t.type = :type ORDER BY d.dataValueTime"),
    @NamedQuery(name = "RecordingDeviceData.findByTypeAttributeAndData", query = "SELECT d FROM RecordingDeviceData d, RecordingDeviceAttribute a, RecordingDeviceType t WHERE d.attributeId = a.attributeId AND a.typeId =t.typeId AND d.patientUuid.patientUuid = :patientUuid AND t.type = :type AND a.attributeName = :attributeName AND d.dataValueTime = :dataValueTime AND d.dataValue = :dataValue"),
    @NamedQuery(name = "RecordingDeviceData.findByPatientAndDownloadedTime", query = "SELECT d FROM RecordingDeviceData d, Patient p WHERE d.patientUuid.patientUuid = p.patientUuid AND p.patientUuid = :patientUuid AND d.downloadedTime > :downloadedTime AND d.downloadedTime<= :endTime"),
    //
    @NamedQuery(name = "RecordingDeviceData.findAll", query = "SELECT d FROM RecordingDeviceData d"),
    @NamedQuery(name = "RecordingDeviceData.findByDataId", query = "SELECT d FROM RecordingDeviceData d WHERE d.dataId = :dataId"),
    @NamedQuery(name = "RecordingDeviceData.findByDataValue", query = "SELECT d FROM RecordingDeviceData d WHERE d.dataValue = :dataValue"),
    @NamedQuery(name = "RecordingDeviceData.findByDataValueTime", query = "SELECT d FROM RecordingDeviceData d WHERE d.dataValueTime = :dataValueTime"),
    @NamedQuery(name = "RecordingDeviceData.findByDownloadedTime", query = "SELECT d FROM RecordingDeviceData d WHERE d.downloadedTime = :downloadedTime")})
public class RecordingDeviceData implements Serializable {

    @Column(name = "schedule_effective_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduleEffectiveTime;
    @Column(name = "schedule_expiry_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduleExpiryTime;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "data_id")
    private Integer dataId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "data_value")
    private String dataValue;
    @Basic(optional = false)
    @NotNull
    @Column(name = "data_value_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataValueTime;
//    @Basic(optional = false)
//    @NotNull
    @Column(name = "downloaded_time", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP(3)")
    @org.hibernate.annotations.Generated(value = GenerationTime.INSERT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date downloadedTime;
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataId")
//    private Collection<Alert> alertCollection;
    @JoinColumn(name = "patient_uuid", referencedColumnName = "patient_uuid")
    @ManyToOne(optional = false)
    private Patient patientUuid;
    @JoinColumn(name = "attribute_id", referencedColumnName = "attribute_id")
    @ManyToOne(optional = false)
    private RecordingDeviceAttribute attributeId;

    public RecordingDeviceData() {
    }

    public RecordingDeviceData(Integer dataId) {
        this.dataId = dataId;
    }

    public RecordingDeviceData(Integer dataId, String dataValue, Date dataValueTime, Date downloadedTime, String dataLink) {
        this.dataId = dataId;
        this.dataValue = dataValue;
        this.dataValueTime = dataValueTime;
        this.downloadedTime = downloadedTime;
    }

    public Integer getDataId() {
        return dataId;
    }

    public void setDataId(Integer dataId) {
        this.dataId = dataId;
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

    public Date getDownloadedTime() {
        return downloadedTime;
    }

    public void setDownloadedTime(Date downloadedTime) {
        this.downloadedTime = downloadedTime;
    }

    public Patient getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(Patient patientUuid) {
        this.patientUuid = patientUuid;
    }

    public RecordingDeviceAttribute getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(RecordingDeviceAttribute attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dataId != null ? dataId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordingDeviceData)) {
            return false;
        }
        RecordingDeviceData other = (RecordingDeviceData) object;
        if ((this.dataId == null && other.dataId != null) || (this.dataId != null && !this.dataId.equals(other.dataId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.RecordingDeviceData[ dataId=" + dataId + " ]";
    }

    public Date getScheduleEffectiveTime() {
        return scheduleEffectiveTime;
    }

    public void setScheduleEffectiveTime(Date scheduleEffectiveTime) {
        this.scheduleEffectiveTime = scheduleEffectiveTime;
    }

    public Date getScheduleExpiryTime() {
        return scheduleExpiryTime;
    }

    public void setScheduleExpiryTime(Date scheduleExpiryTime) {
        this.scheduleExpiryTime = scheduleExpiryTime;
    }

//    @XmlTransient
//    public Collection<Alert> getAlertCollection() {
//        return alertCollection;
//    }
//
//    public void setAlertCollection(Collection<Alert> alertCollection) {
//        this.alertCollection = alertCollection;
//    }
}
