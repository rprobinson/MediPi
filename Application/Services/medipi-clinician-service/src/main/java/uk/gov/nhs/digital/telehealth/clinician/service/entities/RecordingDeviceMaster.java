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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

//@formatter:off
@Entity
@Table(name = "recording_device_type")
@NamedQueries({
	@NamedQuery(name = "RecordingDeviceMaster.fetchAllRecordingDevices", query = "SELECT recordingDeviceMaster FROM RecordingDeviceMaster recordingDeviceMaster")
})
//@formatter:on
public class RecordingDeviceMaster {

	@Id
	@Column(name = "type_id")
	private Integer typeId;

	@Column(name = "type")
	private String type;

	@Column(name = "display_name")
	private String subtype;

	@OneToMany(mappedBy = "recordingDevice", cascade = {CascadeType.ALL})
	private List<RecordingDeviceAttributeMaster> recordingDeviceAttributes;

	public RecordingDeviceMaster() {
		recordingDeviceAttributes = new ArrayList<RecordingDeviceAttributeMaster>();
	}

	public RecordingDeviceMaster(final Integer typeId, final String type, final String subtype) {
		this();
		this.typeId = typeId;
		this.type = type;
		this.subtype = subtype;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(final Integer typeId) {
		this.typeId = typeId;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(final String subtype) {
		this.subtype = subtype;
	}

	public List<RecordingDeviceAttributeMaster> getRecordingDeviceAttributes() {
		return recordingDeviceAttributes;
	}

	public void addRecordingDeviceAttribute(final RecordingDeviceAttributeMaster recordingDeviceAttribute) {
		this.recordingDeviceAttributes.add(recordingDeviceAttribute);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (typeId == null ? 0 : typeId.hashCode());
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
		RecordingDeviceMaster other = (RecordingDeviceMaster) obj;
		if(typeId == null) {
			if(other.typeId != null) {
				return false;
			}
		} else if(!typeId.equals(other.typeId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RecordingDeviceMaster [typeId=" + typeId + ", type=" + type + ", subtype=" + subtype + "]";
	}
}