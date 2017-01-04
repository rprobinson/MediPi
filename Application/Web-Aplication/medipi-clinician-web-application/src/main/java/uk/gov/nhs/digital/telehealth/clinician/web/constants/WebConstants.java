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
package uk.gov.nhs.digital.telehealth.clinician.web.constants;

public interface WebConstants {

	interface SessionVariables {
		String LOGGED_IN_USER = "loggedInUser";
	}

	interface AdminUser {
		String USER_ID = "000000000000";
		String USERNAME = "ADMIN";
	}

	interface Modules {
		String MEDIPI_CLINICIAN_WEB_APPLICATION = "MediPiClinicianWebApp";
	}

	interface Operations {
		String INITIALIZE_OPERATION = "initialize context info";
		String SAVE = "Save ";
		String READ = "Read ";
		String READ_ALL = "Read all ";
		String DELETE = "Delete ";

		interface Patient {
			String SAVE = WebConstants.Operations.SAVE + Patient.class.getSimpleName();
			String READ = WebConstants.Operations.READ + Patient.class.getSimpleName();
			String READ_ALL = WebConstants.Operations.READ_ALL + Patient.class.getSimpleName();
			String DELETE = WebConstants.Operations.DELETE + Patient.class.getSimpleName();
			String PATIENT_MEASUREMENTS = "Patient Measurements " + Patient.class.getSimpleName();
		}

		interface AttributeThreshold {
			String SAVE = WebConstants.Operations.SAVE + AttributeThreshold.class.getSimpleName();
			String READ = WebConstants.Operations.READ + AttributeThreshold.class.getSimpleName();
			String READ_ALL = WebConstants.Operations.READ_ALL + AttributeThreshold.class.getSimpleName();
			String DELETE = WebConstants.Operations.DELETE + AttributeThreshold.class.getSimpleName();
		}
	}
}
