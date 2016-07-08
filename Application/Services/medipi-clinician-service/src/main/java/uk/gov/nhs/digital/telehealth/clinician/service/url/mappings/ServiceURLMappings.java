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
package uk.gov.nhs.digital.telehealth.clinician.service.url.mappings;

import com.dev.ops.common.constants.CommonConstants;

public interface ServiceURLMappings {

	String APPLICATION_ROOT_MAPPING = "/clinician";

	interface PatientServiceController {
		String CONTROLLER_MAPPING = APPLICATION_ROOT_MAPPING + "/patient";
		String GET_ALL_PATIENTS = CommonConstants.Separators.URL_SEPARATOR;
		String GET_PATIENT = CommonConstants.Separators.URL_SEPARATOR;
	}
}
