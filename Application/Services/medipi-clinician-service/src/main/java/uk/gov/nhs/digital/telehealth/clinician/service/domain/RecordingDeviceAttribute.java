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


public class RecordingDeviceAttribute {

	private Integer attributeId;
	private String attributeName;
	private String attributeType;
	private RecordingDevice recordingDevice;

	public RecordingDeviceAttribute() {
	}

	public RecordingDeviceAttribute(final Integer attributeId, final String attributeName, final String attributeType, final RecordingDevice recordingDevice) {
		this();
		this.attributeId = attributeId;
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.recordingDevice = recordingDevice;
	}

	public RecordingDeviceAttribute(final Integer attributeId, final String attributeName, final String attributeType) {
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

	public RecordingDevice getRecordingDevice() {
		return recordingDevice;
	}

	public void setRecordingDevice(final RecordingDevice recordingDevice) {
		this.recordingDevice = recordingDevice;
	}

	@Override
	public String toString() {
		return "RecordingDeviceAttribute [attributeId=" + attributeId + ", attributeName=" + attributeName + ", attributeType=" + attributeType + ", recordingDevice=" + recordingDevice + "]";
	}
}