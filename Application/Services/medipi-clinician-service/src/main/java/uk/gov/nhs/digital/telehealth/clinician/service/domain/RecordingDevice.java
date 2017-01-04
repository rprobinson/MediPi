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
package uk.gov.nhs.digital.telehealth.clinician.service.domain;


public class RecordingDevice {

	private Integer typeId;
	private String type;
	private String displayName;

	public RecordingDevice() {
	}

	public RecordingDevice(final Integer typeId, final String type, final String displayName) {
		this();
		this.typeId = typeId;
		this.type = type;
		this.displayName = displayName;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
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
		RecordingDevice other = (RecordingDevice) obj;
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
		return "RecordingDevice [typeId=" + typeId + ", type=" + type + ", displayName=" + displayName + "]";
	}

}