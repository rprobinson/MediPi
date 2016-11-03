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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

public class AttributeThreshold {

	private Integer attributeThresholdId;

	private String thresholdType;

	private Timestamp effectiveDate;

	@NotNull
	@Pattern(regexp = "^[+-]?(\\d*\\.)?\\d+$", message = "{Pattern.attributeThreshold.thresholdHighValue}")
	@Length(min = 1, max = 10, message = "{Length.attributeThreshold.thresholdHighValue}")
	private String thresholdHighValue;

	@NotNull
	@Pattern(regexp = "^[+-]?(\\d*\\.)?\\d+$", message = "{Pattern.attributeThreshold.thresholdLowValue}")
	@Length(min = 1, max = 10, message = "{Length.attributeThreshold.thresholdLowValue}")
	private String thresholdLowValue;

	@NotNull
	@Length(min = 36, max = 36, message = "{Length.attributeThreshold.patientUUID}")
	private String patientUUID;

	private Integer attributeId;

	@NotNull
	private String attributeName;

	public AttributeThreshold() {
	}

	public AttributeThreshold(final Integer attributeThresholdId, final String thresholdType, final Timestamp effectiveDate, final String thresholdHighValue, final String thresholdLowValue, final String patientUUID, final Integer attributeId, final String attributeName) {
		this();
		this.attributeThresholdId = attributeThresholdId;
		this.thresholdType = thresholdType;
		this.effectiveDate = effectiveDate;
		this.thresholdHighValue = thresholdHighValue;
		this.thresholdLowValue = thresholdLowValue;
		this.patientUUID = patientUUID;
		this.attributeId = attributeId;
		this.attributeName = attributeName;
	}

	public Integer getAttributeThresholdId() {
		return attributeThresholdId;
	}

	public void setAttributeThresholdId(final Integer attributeThresholdId) {
		this.attributeThresholdId = attributeThresholdId;
	}

	public String getThresholdType() {
		return thresholdType;
	}

	public void setThresholdType(final String thresholdType) {
		this.thresholdType = thresholdType;
	}

	public Timestamp getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(final Timestamp effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public String getThresholdHighValue() {
		return thresholdHighValue;
	}

	public void setThresholdHighValue(final String thresholdHighValue) {
		this.thresholdHighValue = thresholdHighValue;
	}

	public String getThresholdLowValue() {
		return thresholdLowValue;
	}

	public void setThresholdLowValue(final String thresholdLowValue) {
		this.thresholdLowValue = thresholdLowValue;
	}

	public String getPatientUUID() {
		return patientUUID;
	}

	public void setPatientUUID(final String patientUUID) {
		this.patientUUID = patientUUID;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (attributeThresholdId == null ? 0 : attributeThresholdId.hashCode());
		result = prime * result + (effectiveDate == null ? 0 : effectiveDate.hashCode());
		result = prime * result + (patientUUID == null ? 0 : patientUUID.hashCode());
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
		AttributeThreshold other = (AttributeThreshold) obj;
		if(attributeThresholdId == null) {
			if(other.attributeThresholdId != null) {
				return false;
			}
		} else if(!attributeThresholdId.equals(other.attributeThresholdId)) {
			return false;
		}
		if(effectiveDate == null) {
			if(other.effectiveDate != null) {
				return false;
			}
		} else if(!effectiveDate.equals(other.effectiveDate)) {
			return false;
		}
		if(patientUUID == null) {
			if(other.patientUUID != null) {
				return false;
			}
		} else if(!patientUUID.equals(other.patientUUID)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AttributeThreshold [attributeThresholdId=" + attributeThresholdId + ", thresholdType=" + thresholdType + ", effectiveDate=" + effectiveDate + ", thresholdHighValue=" + thresholdHighValue + ", thresholdLowValue=" + thresholdLowValue + ", patientUUID=" + patientUUID + ", attributeId=" + attributeId + ", attributeName=" + attributeName + "]";
	}
}