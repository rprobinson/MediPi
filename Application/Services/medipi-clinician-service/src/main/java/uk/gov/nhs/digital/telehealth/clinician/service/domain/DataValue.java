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

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;

public class DataValue {
	private String readingType;
	private String device;
	private String data;
	private Timestamp dataTime;
	private Timestamp submittedTime;

	public DataValue() {
		this.data = StringUtils.EMPTY;
	}

	public DataValue(final String readingType, final String device, final String data, final Timestamp dataTime, final Timestamp submittedTime) {
		this();
		this.readingType = readingType;
		this.device = device;
		this.data = null != data ? data : StringUtils.EMPTY;
		this.dataTime = dataTime;
		this.submittedTime = submittedTime;
	}

	public DataValue(final String readingType, final String device) {
		this(readingType, device, StringUtils.EMPTY, null, null);
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

	public String getData() {
		return data;
	}

	public void setData(final String data) {
		this.data = data;
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
		DataValue other = (DataValue) obj;
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
		return "DataValue [readingType=" + readingType + ", device=" + device + ", data=" + data + ", dataTime=" + dataTime + ", submittedTime=" + submittedTime + "]";
	}
}