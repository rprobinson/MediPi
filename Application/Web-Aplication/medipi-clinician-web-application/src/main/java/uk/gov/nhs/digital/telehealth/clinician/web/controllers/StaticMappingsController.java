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
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dev.ops.exceptions.impl.DefaultWrappedException;

@Controller
@RequestMapping("/clinician")
public class StaticMappingsController extends BaseController {
	//private static final Logger LOGGER = LogManager.getLogger(StaticMappingsController.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView homePage(final ModelAndView modelAndView, final HttpServletRequest request, final HttpServletResponse response) throws DefaultWrappedException {
		modelAndView.setViewName("misc/home");
		return modelAndView;
	}

	@RequestMapping(value = "about", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView aboutPage(final ModelAndView modelAndView) throws DefaultWrappedException {
		modelAndView.setViewName("misc/pageUnderConstruction");
		return modelAndView;
	}

	@RequestMapping(value = "logout", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView logout(final ModelAndView modelAndView) throws DefaultWrappedException {
		modelAndView.setViewName("misc/logout");
		return modelAndView;
	}
}