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
package uk.gov.nhs.digital.telehealth.clinician.web.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.RecordingDeviceAttribute;

public class BloodPressureDeviceAttributes {
	private RecordingDeviceAttribute systolic;
	private RecordingDeviceAttribute diastolic;

	private static final Logger LOGGER = LogManager.getLogger(BloodPressureDeviceAttributes.class);

	public BloodPressureDeviceAttributes() {
	}

	public BloodPressureDeviceAttributes(final RecordingDeviceAttribute systolic, final RecordingDeviceAttribute diastolic) {
		this();
		this.systolic = systolic;
		this.diastolic = diastolic;
	}

	public RecordingDeviceAttribute getSystolic() {
		return systolic;
	}

	public void setSystolic(final RecordingDeviceAttribute systolic) {
		this.systolic = systolic;
	}

	public RecordingDeviceAttribute getDiastolic() {
		return diastolic;
	}

	public void setDiastolic(final RecordingDeviceAttribute diastolic) {
		this.diastolic = diastolic;
	}

	@Override
	public String toString() {
		return "BloodPressureDeviceAttributes [systolic=" + systolic + ", diastolic=" + diastolic + "]";
	}

	public static List<BloodPressureDeviceAttributes> getBloodPressureDeviceAttributes(final RecordingDeviceAttribute[] recordingDeviceAttributes, final String systolic, final String diastolic) {
		List<BloodPressureDeviceAttributes> bloodPressureDeviceAttributesList = new ArrayList<BloodPressureDeviceAttributes>();
		for(RecordingDeviceAttribute recordingDeviceAttribute : recordingDeviceAttributes) {
			BloodPressureDeviceAttributes bloodPressureDeviceAttributes = getBloodPressureDeviceAttributeIfExists(bloodPressureDeviceAttributesList, recordingDeviceAttribute);

			if(bloodPressureDeviceAttributes == null) {
				bloodPressureDeviceAttributes = new BloodPressureDeviceAttributes();
				bloodPressureDeviceAttributesList.add(bloodPressureDeviceAttributes);
			}

			if(recordingDeviceAttribute.getAttributeName().equals(systolic)) {
				bloodPressureDeviceAttributes.setSystolic(recordingDeviceAttribute);
			} else if(recordingDeviceAttribute.getAttributeName().equals(diastolic)) {
				bloodPressureDeviceAttributes.setDiastolic(recordingDeviceAttribute);
			}
		}

		LOGGER.debug("The list of BloodPressureDeviceAttributes before removing invalid attributes:" + bloodPressureDeviceAttributesList);

		List<BloodPressureDeviceAttributes> invalidBloodPressureDeviceAttributes = getInvalidBloodPressureDeviceAttributes(bloodPressureDeviceAttributesList);
		bloodPressureDeviceAttributesList.removeAll(invalidBloodPressureDeviceAttributes);

		LOGGER.debug("The list of BloodPressureDeviceAttributes after removing invalid attributes:" + bloodPressureDeviceAttributesList);
		return bloodPressureDeviceAttributesList;
	}

	private static List<BloodPressureDeviceAttributes> getInvalidBloodPressureDeviceAttributes(final List<BloodPressureDeviceAttributes> bloodPressureDeviceAttributesList) {
		List<BloodPressureDeviceAttributes> invalidBloodPressureDeviceAttributes = new ArrayList<BloodPressureDeviceAttributes>();
		for(BloodPressureDeviceAttributes bloodPressureDeviceAttributes : bloodPressureDeviceAttributesList) {
			if(bloodPressureDeviceAttributes.getSystolic() == null || bloodPressureDeviceAttributes.getDiastolic() == null) {
				invalidBloodPressureDeviceAttributes.add(bloodPressureDeviceAttributes);
			}
		}
		LOGGER.debug("The list of invalid BloodPressureDeviceAttributes:" + invalidBloodPressureDeviceAttributes);
		return invalidBloodPressureDeviceAttributes;
	}

	private static BloodPressureDeviceAttributes getBloodPressureDeviceAttributeIfExists(final List<BloodPressureDeviceAttributes> bloodPressureDeviceAttributesList, final RecordingDeviceAttribute recordingDeviceAttribute) {
		BloodPressureDeviceAttributes existingBloodPressureDeviceAttributes = null;
		for(BloodPressureDeviceAttributes bloodPressureDeviceAttributes : bloodPressureDeviceAttributesList) {
			if(bloodPressureDeviceAttributes.getSystolic() != null && bloodPressureDeviceAttributes.getSystolic().getRecordingDevice().equals(recordingDeviceAttribute.getRecordingDevice())) {
				existingBloodPressureDeviceAttributes = bloodPressureDeviceAttributes;
				break;
			} else if(bloodPressureDeviceAttributes.getDiastolic() != null && bloodPressureDeviceAttributes.getDiastolic().getRecordingDevice().equals(recordingDeviceAttribute.getRecordingDevice())) {
				existingBloodPressureDeviceAttributes = bloodPressureDeviceAttributes;
				break;
			}
		}
		return existingBloodPressureDeviceAttributes;
	}
}