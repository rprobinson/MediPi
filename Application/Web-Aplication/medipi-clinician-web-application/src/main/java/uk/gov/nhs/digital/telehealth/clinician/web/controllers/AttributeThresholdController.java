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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.AttributeThreshold;
import uk.gov.nhs.digital.telehealth.clinician.service.url.mappings.ServiceURLMappings;
import uk.gov.nhs.digital.telehealth.clinician.web.constants.WebConstants;

import com.dev.ops.common.constants.CommonConstants;
import com.dev.ops.common.utils.HttpUtil;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Controller
@RequestMapping("/clinician/attributeThreshold")
public class AttributeThresholdController extends BaseController {

	@Autowired
	@Value(value = "${medipi.clinician.service.url}")
	private String clinicianServiceURL;

	private static final Logger LOGGER = LogManager.getLogger(AttributeThresholdController.class);

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public AttributeThreshold getAttributeThreshold(@RequestParam("patientUUID") final String patientUUID, @RequestParam("attributeName") final String attributeName, final HttpServletRequest request) throws DefaultWrappedException {
		final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(WebConstants.Operations.AttributeThreshold.READ, null);
		LOGGER.debug("Getting attribute threshold for patientUUID:<" + patientUUID + "> and  attributeName:<" + attributeName + ">");
		return this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.AttributeThresholdServiceController.CONTROLLER_MAPPING + ServiceURLMappings.AttributeThresholdServiceController.GET_ATTRIBUTE_THRESHOLD + CommonConstants.Separators.URL_SEPARATOR + patientUUID + CommonConstants.Separators.URL_SEPARATOR + attributeName, HttpMethod.GET, entity, AttributeThreshold.class).getBody();
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	@ResponseBody
	public AttributeThreshold saveAttributeThreshold(@ModelAttribute final AttributeThreshold attributeThreshold, final HttpServletRequest request) throws DefaultWrappedException {
		final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(WebConstants.Operations.AttributeThreshold.SAVE, attributeThreshold);
		AttributeThreshold savedAttributeThreshold = this.restTemplate.exchange(this.clinicianServiceURL + ServiceURLMappings.AttributeThresholdServiceController.CONTROLLER_MAPPING + ServiceURLMappings.AttributeThresholdServiceController.SAVE_ATTRIBUTE_THRESHOLD, HttpMethod.POST, entity, AttributeThreshold.class).getBody();
		LOGGER.info("The saved attribute threshold: " + savedAttributeThreshold);
		return savedAttributeThreshold;
	}
}