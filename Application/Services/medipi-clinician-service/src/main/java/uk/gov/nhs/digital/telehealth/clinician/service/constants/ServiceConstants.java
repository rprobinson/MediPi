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
package uk.gov.nhs.digital.telehealth.clinician.service.constants;


public interface ServiceConstants {

	interface Attributes {
		String WEIGHT = "weight";
	}

	interface AttributeThresholdTypes {
		String SIMPLE_HIGH_LOW_INCLUSIVE_TEST = "org.medipi.clinical.threshold.SimpleInclusiveHighLowTest";
		String CHANGE_OVER_TIME_TEST = "org.medipi.clinical.threshold.ChangeOverTimeTest";
	}
}
