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
package uk.gov.nhs.digital.telehealth.clinician.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import uk.gov.nhs.digital.telehealth.clinician.web.constants.WebConstants;

import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.common.thread.local.ContextThreadLocal;

@Component
public class ContextInfoInterceptor implements HandlerInterceptor {

	@Override
	public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object object, final Exception exception) throws Exception {

	}

	@Override
	public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object object, final ModelAndView modelAndView) throws Exception {

	}

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object object) throws Exception {
		final boolean shouldProceedWithRequest = true;
		final ContextInfo contextInfo = new ContextInfo(WebConstants.Modules.MEDIPI_CLINICIAN_WEB_APPLICATION, WebConstants.Operations.INITIALIZE_OPERATION);
		contextInfo.setTransactionRequestedByUserId(WebConstants.AdminUser.USER_ID);
		contextInfo.setTransactionRequestedByUsername(WebConstants.AdminUser.USERNAME);
		ContextThreadLocal.set(contextInfo);
		return shouldProceedWithRequest;
	}
}