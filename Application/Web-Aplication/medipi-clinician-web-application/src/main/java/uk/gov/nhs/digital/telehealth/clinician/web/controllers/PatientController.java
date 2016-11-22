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
package uk.gov.nhs.digital.telehealth.clinician.web.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.Measurement;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.Patient;
import uk.gov.nhs.digital.telehealth.clinician.service.url.mappings.ServiceURLMappings;
import uk.gov.nhs.digital.telehealth.clinician.web.constants.WebConstants;

import com.dev.ops.common.utils.HttpUtil;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Controller
@RequestMapping("/clinician/patient")
public class PatientController extends BaseController {

	@Autowired
	@Value(value = "${medipi.clinician.service.url}")
	private String clinicianServiceURL;

	@Autowired
	@Value(value = "${all.patients.view.refresh.frequency}")
	private String refreshViewFrequency;

	private static final Logger LOGGER = LogManager.getLogger(PatientController.class);

	@RequestMapping(value = "/patients", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView showPatients(final ModelAndView modelAndView, final HttpServletRequest request) {
		modelAndView.addObject("refreshViewFrequency", Integer.valueOf(refreshViewFrequency) * 1000);
		//modelAndView.setViewName("patient/patients");
		modelAndView.setViewName("patient/allPatients");
		return modelAndView;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/patientsJSON", method = RequestMethod.GET)
	@ResponseBody
	public List<Patient> getPatients(final HttpServletRequest request) throws DefaultWrappedException {
		final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(WebConstants.Operations.Patient.READ_ALL, null);
		return this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_ALL_PATIENTS, HttpMethod.GET, entity, List.class).getBody();
	}

	@RequestMapping(value = "/{patientUUID}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView getPatient(@PathVariable final String patientUUID, final ModelAndView modelAndView, final HttpServletRequest request) throws DefaultWrappedException, IOException {
		LOGGER.debug("Get patient details for patient id:<" + patientUUID + ">.");
		final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(WebConstants.Operations.Patient.READ, null);
		final Patient patient = this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_PATIENT + patientUUID, HttpMethod.GET, entity, Patient.class).getBody();
		modelAndView.addObject("patient", patient);
		modelAndView.setViewName("patient/viewPatient");
		return modelAndView;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@RequestMapping(value = "/patientMeasurements", method = RequestMethod.GET)
	@ResponseBody
	public List<Measurement> patientMeasurements(@RequestParam("patientUUID") final String patientUUID, @RequestParam("attributeName") final String attributeName, final HttpServletRequest request) throws DefaultWrappedException {
		final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(WebConstants.Operations.Patient.PATIENT_MEASUREMENTS, null);
		final List<Measurement> measurements = this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_PATIENT_MEASURMENTS + patientUUID + "/" + attributeName, HttpMethod.GET, entity, (Class<List<Measurement>>) (Class) List.class).getBody();
		return measurements;
	}
}