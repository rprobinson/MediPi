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

import javax.validation.Valid;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.AttributeThreshold;
import uk.gov.nhs.digital.telehealth.clinician.service.services.AttributeThresholdService;
import uk.gov.nhs.digital.telehealth.clinician.service.url.mappings.ServiceURLMappings;

import com.dev.ops.common.constants.CommonConstants;
import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.common.thread.local.ContextThreadLocal;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Controller
@RequestMapping(ServiceURLMappings.AttributeThresholdServiceController.CONTROLLER_MAPPING)
public class AttributeThresholdServiceController {

	@Autowired
	private AttributeThresholdService attributeThresholdService;

	private static final Logger LOGGER = LogManager.getLogger(AttributeThresholdServiceController.class);

	@RequestMapping(value = ServiceURLMappings.AttributeThresholdServiceController.GET_ATTRIBUTE_THRESHOLD + "{patientUUID}" + CommonConstants.Separators.URL_SEPARATOR + "{attributeName}", method = RequestMethod.GET)
	@ResponseBody
	public AttributeThreshold getPatientDetails(@PathVariable final String patientUUID, @PathVariable final String attributeName, @RequestHeader(CommonConstants.CONTEXT_INFORMATION_REQUEST_PARAMETER) final String context) throws DefaultWrappedException {
		ContextThreadLocal.set(ContextInfo.toContextInfo(context));
		LOGGER.debug("Get Attribute threshold for attributeName:<" + attributeName + "> and patient UUID:<" + patientUUID + ">");
		final AttributeThreshold attributeThreshold = attributeThresholdService.getAttributeThreshold(patientUUID, attributeName);
		LOGGER.debug("The Attribute threshold: " + attributeThreshold);
		return attributeThreshold;
	}

	@RequestMapping(value = ServiceURLMappings.AttributeThresholdServiceController.SAVE_ATTRIBUTE_THRESHOLD, method = RequestMethod.POST)
	@ResponseBody
	public AttributeThreshold saveAttributeThreshold(@RequestBody @Valid final AttributeThreshold attributeThreshold, @RequestHeader(CommonConstants.CONTEXT_INFORMATION_REQUEST_PARAMETER) final String context) throws DefaultWrappedException {
		ContextThreadLocal.set(ContextInfo.toContextInfo(context));
		LOGGER.debug("Save attribute thresholds for: " + attributeThreshold);
		AttributeThreshold savedAttributeThreshold = attributeThresholdService.saveAttributeThreshold(attributeThreshold);
		LOGGER.info("The saved attribute threshold: " + savedAttributeThreshold);
		return savedAttributeThreshold;
	}
}