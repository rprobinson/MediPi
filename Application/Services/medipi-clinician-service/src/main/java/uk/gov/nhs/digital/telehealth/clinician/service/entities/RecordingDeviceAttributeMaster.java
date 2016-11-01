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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

//@formatter:off
@Entity
@Table(name = "recording_device_attribute")
@NamedQueries({
	@NamedQuery(name = "RecordingDeviceAttributeMaster.fetchAllRecordingDeviceAttributes", query = "SELECT recordingDeviceAttributeMaster FROM RecordingDeviceAttributeMaster recordingDeviceAttributeMaster"),
	@NamedQuery(name = "RecordingDeviceAttributeMaster.fetchByAttributeName", query = "SELECT recordingDeviceAttributeMaster FROM RecordingDeviceAttributeMaster recordingDeviceAttributeMaster WHERE recordingDeviceAttributeMaster.attributeName = :attributeName")
})
//@formatter:on
public class RecordingDeviceAttributeMaster {

	@Id
	@Column(name = "attribute_id")
	private Integer attributeId;

	@Column(name = "attribute_name")
	private String attributeName;

	@Column(name = "attribute_type")
	private String attributeType;

	@ManyToOne
	@JoinColumn(name = "type_id")
	private RecordingDeviceMaster recordingDevice;

	@OneToMany(mappedBy = "recordingDeviceAttribute", cascade = {CascadeType.ALL})
	private List<RecordingDeviceDataMaster> recordingDeviceDataList;

	public RecordingDeviceAttributeMaster() {
		recordingDeviceDataList = new ArrayList<RecordingDeviceDataMaster>();
	}

	public RecordingDeviceAttributeMaster(final Integer attributeId, final String attributeName, final String attributeType, final RecordingDeviceMaster recordingDevice) {
		this();
		this.attributeId = attributeId;
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.recordingDevice = recordingDevice;
	}

	public RecordingDeviceAttributeMaster(final Integer attributeId, final String attributeName, final String attributeType) {
		this(attributeId, attributeName, attributeType, null);
	}

	public Integer getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(final Integer attributeId) {
		this.attributeId = attributeId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(final String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(final String attributeType) {
		this.attributeType = attributeType;
	}

	public RecordingDeviceMaster getRecordingDevice() {
		return recordingDevice;
	}

	public void setRecordingDevice(final RecordingDeviceMaster recordingDevice) {
		this.recordingDevice = recordingDevice;
	}

	public List<RecordingDeviceDataMaster> getRecordingDeviceDataList() {
		return recordingDeviceDataList;
	}

	public void addRecordingDeviceData(final RecordingDeviceDataMaster recordingDeviceData) {
		this.recordingDeviceDataList.add(recordingDeviceData);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (attributeId == null ? 0 : attributeId.hashCode());
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
		RecordingDeviceAttributeMaster other = (RecordingDeviceAttributeMaster) obj;
		if(attributeId == null) {
			if(other.attributeId != null) {
				return false;
			}
		} else if(!attributeId.equals(other.attributeId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RecordingDeviceAttributeMaster [attributeId=" + attributeId + ", attributeName=" + attributeName + ", attributeType=" + attributeType + ", recordingDevice=" + recordingDevice + "]";
	}
}