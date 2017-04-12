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

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.Measurement;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.Patient;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.RecordingDeviceAttribute;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.Clinician;
import uk.gov.nhs.digital.telehealth.clinician.service.url.mappings.ServiceURLMappings;
import uk.gov.nhs.digital.telehealth.clinician.web.constants.WebConstants;
import uk.gov.nhs.digital.telehealth.clinician.web.domain.BloodPressureDeviceAttributes;

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

	@Autowired
	@Value("${medipi.clinician.web.similar.plot.attributes}")
	private String similarPlotAttributes;

	@Autowired
	@Value("${medipi.clinician.web.blood.pressure.systol.attribute}")
	private String bloodPressureSystolAttribute;

	@Autowired
	@Value("${medipi.clinician.web.blood.pressure.diastol.attribute}")
	private String bloodPressureDiastolAttribute;

	@Autowired
	@Value("${medipi.clinician.web.questionnnaire.attributes}")
	private String questionnnaireAttributes;

	private static final Logger LOGGER = LogManager.getLogger(PatientController.class);

	@RequestMapping(value = "/patients", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView showPatients(final ModelAndView modelAndView, final HttpServletRequest request) {
		modelAndView.addObject("refreshViewFrequency", Integer.valueOf(refreshViewFrequency) * 1000);
		modelAndView.setViewName("patient/allPatients");
		return modelAndView;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/patientsJSON", method = RequestMethod.GET)
	@ResponseBody
	public List<Patient> getPatients(final HttpServletRequest request) throws DefaultWrappedException {
		final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(WebConstants.Operations.Patient.READ_ALL, null);
		Clinician clinician = getClinicianFromSecurityContext();
		return this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_PATIENTS_BY_GROUP + clinician.getPatientGroup().getPatientGroupId(), HttpMethod.GET, entity, List.class).getBody();
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{patientUUID}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView getPatient(@PathVariable final String patientUUID, final ModelAndView modelAndView, final HttpServletRequest request) throws DefaultWrappedException, IOException {
		LOGGER.debug("Get patient details for patient id:<" + patientUUID + ">.");
		Clinician clinician = getClinicianFromSecurityContext();
		final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(WebConstants.Operations.Patient.READ, null);
		final Patient patient = this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_PATIENT + patientUUID, HttpMethod.GET, entity, Patient.class).getBody();
		if(!StringUtils.equals(clinician.getPatientGroup().getPatientGroupId(), patient.getPatientGroupId())) {
			LOGGER.warn("The " + clinician + " tried to access " + patient + " details from other group.");
			throw new DefaultWrappedException("You are not authorized to access the patient details.");
		}
		modelAndView.addObject("patient", patient);

		final List<RecordingDeviceAttribute> similarDeviceAttributes = this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_PATIENT_ATTRIBUTES + patientUUID + "/" + similarPlotAttributes, HttpMethod.GET, entity, List.class).getBody();
		modelAndView.addObject("similarDeviceAttributes", similarDeviceAttributes);

		final RecordingDeviceAttribute[] bloodPressureDeviceAttributes = this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_PATIENT_ATTRIBUTES + patientUUID + "/" + bloodPressureSystolAttribute + "," + bloodPressureDiastolAttribute, HttpMethod.GET, entity, RecordingDeviceAttribute[].class).getBody();
		modelAndView.addObject("bloodPressureDeviceAttributesList", BloodPressureDeviceAttributes.getBloodPressureDeviceAttributes(bloodPressureDeviceAttributes, bloodPressureSystolAttribute, bloodPressureDiastolAttribute));

		final List<RecordingDeviceAttribute> questionnaireDeviceAttributes = this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_PATIENT_ATTRIBUTES + patientUUID + "/" + questionnnaireAttributes, HttpMethod.GET, entity, List.class).getBody();
		modelAndView.addObject("questionnaireDeviceAttributes", questionnaireDeviceAttributes);

		modelAndView.setViewName("patient/viewPatient");
		return modelAndView;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@RequestMapping(value = "/patientMeasurements/{patientUUID}/{attributeId}", method = RequestMethod.GET)
	@ResponseBody
	public List<Measurement> patientMeasurements(@PathVariable final String patientUUID, @PathVariable final Integer attributeId, final HttpServletRequest request) throws DefaultWrappedException {
		final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(WebConstants.Operations.Patient.PATIENT_MEASUREMENTS, null);
		final List<Measurement> measurements = this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.PatientServiceController.CONTROLLER_MAPPING + ServiceURLMappings.PatientServiceController.GET_PATIENT_MEASURMENTS + patientUUID + "/" + attributeId, HttpMethod.GET, entity, (Class<List<Measurement>>) (Class) List.class).getBody();
		return measurements;
	}
}