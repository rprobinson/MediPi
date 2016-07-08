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
package com.dev.ops.common.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import com.dev.ops.common.constants.CommonConstants;
import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.common.thread.local.ContextThreadLocal;

public class HttpUtil {

	public static HttpEntity<?> getHeaders() {
		final ContextInfo contextInfo = ContextThreadLocal.get();
		final Object entity = null;
		return getEntityWithHeaders(entity, contextInfo);
	}

	public static HttpEntity<?> getHeaders(final ContextInfo contextInfo, final Object entityToSubmintInRequest) {
		return getEntityWithHeaders(entityToSubmintInRequest, contextInfo);
	}

	public static HttpEntity<?> getEntityWithHeaders(final ContextInfo contextInfo) {
		final Object entity = null;
		return getEntityWithHeaders(entity, contextInfo);
	}

	public static HttpEntity<?> getEntityWithHeaders(final Object entityToSubmintInRequest) {
		final ContextInfo contextInfo = ContextThreadLocal.get();
		return getEntityWithHeaders(entityToSubmintInRequest, contextInfo);
	}

	public static HttpEntity<?> getEntityWithHeaders(final ContextInfo contextInfo, final Object entityToSubmintInRequest) {
		return getEntityWithHeaders(entityToSubmintInRequest, contextInfo);
	}

	public static HttpEntity<?> getEntityWithHeaders(final String operation, final Object entityToSubmintInRequest) {
		final ContextInfo contextInfo = ContextThreadLocal.get();
		contextInfo.setOperation(operation);
		return getEntityWithHeaders(entityToSubmintInRequest, contextInfo);
	}

	public static HttpEntity<?> getEntityWithHeaders(final String moduleName, final String operation, final Object entityToSubmintInRequest) {
		final ContextInfo contextInfo = ContextThreadLocal.get();
		contextInfo.setModuleName(moduleName);
		contextInfo.setOperation(operation);

		return getEntityWithHeaders(entityToSubmintInRequest, contextInfo);
	}

	private static HttpEntity<?> getEntityWithHeaders(final Object entityToSubmintInRequest, final ContextInfo contextInfo) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(CommonConstants.CONTEXT_INFORMATION_REQUEST_PARAMETER, contextInfo.toString());

		HttpEntity<?> httpEntity = null;

		if(entityToSubmintInRequest == null) {
			httpEntity = new HttpEntity<String>(headers);
		} else {
			httpEntity = new HttpEntity<>(entityToSubmintInRequest, headers);
		}
		return httpEntity;
	}
}
