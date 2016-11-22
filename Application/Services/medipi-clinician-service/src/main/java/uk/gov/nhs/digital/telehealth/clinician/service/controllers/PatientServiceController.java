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
package uk.gov.nhs.digital.telehealth.clinician.service.controllers;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.Measurement;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.Patient;
import uk.gov.nhs.digital.telehealth.clinician.service.services.PatientService;
import uk.gov.nhs.digital.telehealth.clinician.service.url.mappings.ServiceURLMappings;

import com.dev.ops.common.constants.CommonConstants;
import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.common.thread.local.ContextThreadLocal;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Controller
@RequestMapping(ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING)
public class PatientServiceController {

	@Autowired
	private PatientService patientService;

	private static final Logger LOGGER = LogManager.getLogger(PatientServiceController.class);

	@RequestMapping(value = ServiceURLMappings.PatientServiceController.GET_PATIENT + "{patientUUID}", method = RequestMethod.GET)
	@ResponseBody
	public Patient getPatientDetails(@PathVariable final String patientUUID, @RequestHeader(CommonConstants.CONTEXT_INFORMATION_REQUEST_PARAMETER) final String context) throws DefaultWrappedException {
		ContextThreadLocal.set(ContextInfo.toContextInfo(context));
		LOGGER.debug("Get Patient details for:" + patientUUID);
		final Patient patient = this.patientService.getPatientDetails(patientUUID);
		LOGGER.debug("The Patient details: " + patient);
		return patient;
	}

	@RequestMapping(value = ServiceURLMappings.PatientServiceController.GET_ALL_PATIENTS, method = RequestMethod.GET)
	@ResponseBody
	public List<Patient> getAllPatients(@RequestHeader(CommonConstants.CONTEXT_INFORMATION_REQUEST_PARAMETER) final String context) throws DefaultWrappedException {
		ContextThreadLocal.set(ContextInfo.toContextInfo(context));
		final List<Patient> patients = this.patientService.getAllPatients();
		return patients;
	}

	/*@RequestMapping(value = ServiceURLMappings.PatientServiceController.GET_PATIENT_RECENT_MEASURMENTS + "{patientUUID}", method = RequestMethod.GET)
	@ResponseBody
	public List<DataValue> getPatientRecentReadings(@PathVariable final String patientUUID, @RequestHeader(CommonConstants.CONTEXT_INFORMATION_REQUEST_PARAMETER) final String context) throws DefaultWrappedException {
		ContextThreadLocal.set(ContextInfo.toContextInfo(context));
		final List<DataValue> recentReadings = this.patientService.getPatientsRecentMeasurements(patientUUID);
		return recentReadings;
	}*/

	@RequestMapping(value = ServiceURLMappings.PatientServiceController.GET_PATIENT_MEASURMENTS + "{patientUUID}" + "/{attributeName}", method = RequestMethod.GET)
	@ResponseBody
	public List<Measurement> getPatientMeasurements(@PathVariable final String patientUUID, @PathVariable final String attributeName, @RequestHeader(CommonConstants.CONTEXT_INFORMATION_REQUEST_PARAMETER) final String context) throws Exception {
		ContextThreadLocal.set(ContextInfo.toContextInfo(context));
		LOGGER.debug("Get Patient measurements for patient id:<" + patientUUID + "> and attributeName:<" + attributeName + ">");
		final List<Measurement> measurements = this.patientService.getPatientMeasurements(patientUUID, attributeName);
		return measurements;
	}

}