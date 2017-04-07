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
import java.text.DecimalFormat;
import java.util.List;

public class Measurement implements Comparable<Measurement> {

	private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("0.##");

	private Timestamp dataTime;
	private String value;
	private String minValue;
	private String maxValue;
	private String alertStatus;

	public Measurement() {
	}

	public Measurement(final Timestamp dataTime, final String value, final String minValue, final String maxValue) {
		this();
		this.dataTime = dataTime;
		this.value = value;
		this.minValue = minValue != null ? minValue : this.minValue;
		this.maxValue = maxValue != null ? maxValue : this.maxValue;
	}

	public Measurement(final Timestamp dataTime, final String value) {
		this(dataTime, value, null, null);
		this.dataTime = dataTime;
		this.value = value;
	}

	public Timestamp getDataTime() {
		return dataTime;
	}

	public void setDataTime(final Timestamp dataTime) {
		this.dataTime = dataTime;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(final String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(final String maxValue) {
		this.maxValue = maxValue;
	}

	public String getAlertStatus() {
		return alertStatus;
	}

	public void setAlertStatus(final String alertStatus) {
		this.alertStatus = alertStatus;
	}

	/*public void setMinMaxValues(final List<AttributeThreshold> attributeThresholds) {
		AttributeThreshold oldestThreshold = null;
		boolean isValueSet = false;

		for(AttributeThreshold attributeThreshold : attributeThresholds) {
			//Find out the oldest threshold values. This in used in case the measurements are submitted before setting the threshold.
			if(oldestThreshold != null && attributeThreshold.getEffectiveDate().before(oldestThreshold.getEffectiveDate())) {
				oldestThreshold = attributeThreshold;
			} else if(oldestThreshold == null) {
				oldestThreshold = attributeThreshold;
			}

			if(this.dataTime.after(attributeThreshold.getEffectiveDate())) {
				this.minValue = attributeThreshold.getThresholdLowValue();
				this.maxValue = attributeThreshold.getThresholdHighValue();
				isValueSet = true;
			}
		}

		//Set the oldest threshold values as min, max if the readings are taken before setting the threshold for the patient.
		if(!isValueSet && oldestThreshold != null) {
			this.minValue = oldestThreshold.getThresholdLowValue();
			this.maxValue = oldestThreshold.getThresholdHighValue();
		}
	}*/

	public void setMinMaxValues(final List<Double> thresholds) {
		if(thresholds != null && thresholds.size() == 2) {
			this.minValue = DECIMAL_FORMATTER.format(thresholds.get(0)).toString();
			this.maxValue = DECIMAL_FORMATTER.format(thresholds.get(1)).toString();
		}

	}

	@Override
	public int compareTo(final Measurement measurement) {
		int returnValue = 0;
		if(this.dataTime.after(measurement.getDataTime())) {
			returnValue = 1;
		} else if(this.dataTime.before(measurement.getDataTime())) {
			returnValue = -1;
		}
		return returnValue;
	}
}