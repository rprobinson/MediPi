/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.gov.nhs.digital.telehealth.clinician.service.entities;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;

@Entity
public class DataValueEntity {

	@Id
	private Long dataId;
	private String readingType;
	private String device;
	private Integer attributeId;
	private String attributeName;
	private String data;
	private Integer typeId;
	private Timestamp dataTime;
	private Timestamp submittedTime;
	private Timestamp scheduleEffectiveTime;
	private Timestamp scheduleExpiryTime;
	private String alertStatus;

	public DataValueEntity() {
		this.data = StringUtils.EMPTY;
	}

	public DataValueEntity(final String readingType, final String device, final Integer attributeId, final String attributeName, final String data, final Integer typeId, final Timestamp dataTime, final Timestamp submittedTime, final Timestamp scheduleEffectiveTime, final Timestamp scheduleExpiryTime, final String alertStatus) {
		this();
		this.readingType = readingType;
		this.device = device;
		this.attributeId = attributeId;
		this.attributeName = attributeName;
		this.data = data;
		this.typeId = typeId;
		this.dataTime = dataTime;
		this.submittedTime = submittedTime;
		this.scheduleEffectiveTime = scheduleEffectiveTime;
		this.scheduleExpiryTime = scheduleExpiryTime;
		this.alertStatus = alertStatus;
	}

	public DataValueEntity(final String readingType, final String device, final Integer attributeId, final String attributeName, final String data, final Integer typeId, final Timestamp dataTime, final Timestamp submittedTime) {
		this(readingType, device, attributeId, attributeName, data, typeId, dataTime, submittedTime, null, null, null);
	}

	public DataValueEntity(final String readingType, final String device) {
		this(readingType, device, null, null, null, null, null, null);
	}

	public Long getDataId() {
		return dataId;
	}

	public void setDataId(final Long dataId) {
		this.dataId = dataId;
	}

	public String getReadingType() {
		return readingType;
	}

	public void setReadingType(final String readingType) {
		this.readingType = readingType;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(final String device) {
		this.device = device;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(final String attributeName) {
		this.attributeName = attributeName;
	}

	public Integer getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(final Integer attributeId) {
		this.attributeId = attributeId;
	}

	public String getData() {
		return data;
	}

	public void setData(final String data) {
		this.data = data;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(final Integer typeId) {
		this.typeId = typeId;
	}

	public Timestamp getDataTime() {
		return dataTime;
	}

	public void setDataTime(final Timestamp dataTime) {
		this.dataTime = dataTime;
	}

	public Timestamp getSubmittedTime() {
		return submittedTime;
	}

	public void setSubmittedTime(final Timestamp submittedTime) {
		this.submittedTime = submittedTime;
	}

	public Timestamp getScheduleEffectiveTime() {
		return scheduleEffectiveTime;
	}

	public void setScheduleEffectiveTime(final Timestamp scheduleEffectiveTime) {
		this.scheduleEffectiveTime = scheduleEffectiveTime;
	}

	public Timestamp getScheduleExpiryTime() {
		return scheduleExpiryTime;
	}

	public void setScheduleExpiryTime(final Timestamp scheduleExpiryTime) {
		this.scheduleExpiryTime = scheduleExpiryTime;
	}

	public String getAlertStatus() {
		return alertStatus;
	}

	public void setAlertStatus(final String alertStatus) {
		this.alertStatus = alertStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (device == null ? 0 : device.hashCode());
		result = prime * result + (readingType == null ? 0 : readingType.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		DataValueEntity other = (DataValueEntity) obj;
		if(device == null) {
			if(other.device != null) {
				return false;
			}
		} else if(!device.equals(other.device)) {
			return false;
		}
		if(readingType == null) {
			if(other.readingType != null) {
				return false;
			}
		} else if(!readingType.equals(other.readingType)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DataValueEntity [dataId=" + dataId + ", readingType=" + readingType + ", device=" + device + ", attributeId=" + attributeId + ", attributeName=" + attributeName + ", data=" + data + ", typeId=" + typeId + ", dataTime=" + dataTime + ", submittedTime=" + submittedTime + ", scheduleEffectiveTime=" + scheduleEffectiveTime + ", scheduleExpiryTime=" + scheduleExpiryTime + ", alertStatus=" + alertStatus + "]";
	}
}