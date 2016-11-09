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
package com.dev.ops.common.logging.interceptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.dev.ops.common.constants.CommonConstants;
import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.common.thread.local.ContextThreadLocal;
import com.dev.ops.common.utils.LoggingUtil;
import com.dev.ops.exception.manager.constants.LoggingConstants.LoggingPoint;

@Aspect
@Component
public class LoggingInterceptor {

	private static final Logger LOGGER = LogManager.getLogger(LoggingInterceptor.class);

	@Before("execution(* uk.gov.nhs.digital.telehealth.clinician.service..controllers..*.*(..))")
	public void addContextInfoToThreadLocal(final JoinPoint joinPoint) {
		ContextInfo contextInfo = getContextInfoFromMethod(joinPoint);
		ContextThreadLocal.set(contextInfo);
	}

	@Before("execution(* uk.gov.nhs.digital.telehealth..*.*(..)) && !execution(* com.dev.ops.common.orika..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.configurations..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.interceptors.ContextInfoInterceptor..*(..))")
	public void logMethodStart(final JoinPoint joinPoint) {
		String contextInfo = getContextInfo();
		String methodSignature = getMethodSignature(joinPoint).toString();
		LOGGER.info(LoggingUtil.getMessageDescription(LoggingPoint.START.toString(), new Object[] {contextInfo, methodSignature}));
	}

	@AfterReturning("execution(* uk.gov.nhs.digital.telehealth..*.*(..)) && !execution(* com.dev.ops.common.orika..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.configurations..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.interceptors.ContextInfoInterceptor..*(..))")
	public void logMethodEnd(final JoinPoint joinPoint) {
		String contextInfo = getContextInfo();
		String methodSignature = getMethodSignature(joinPoint).toString();
		LOGGER.info(LoggingUtil.getMessageDescription(LoggingPoint.END.toString(), new Object[] {contextInfo, methodSignature}));
	}

	@AfterThrowing("execution(* uk.gov.nhs.digital.telehealth..*.*(..)) && !execution(* com.dev.ops.common.orika..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.configurations..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.interceptors.ContextInfoInterceptor..*(..))")
	public void logMethodException(final JoinPoint joinPoint) {
		String contextInfo = getContextInfo();
		String methodSignature = getMethodSignature(joinPoint).toString();
		LOGGER.info(LoggingUtil.getMessageDescription(LoggingPoint.ERROR.toString(), new Object[] {contextInfo, methodSignature}));
	}

	public Object logTimeMethod(final ProceedingJoinPoint joinPoint) throws Throwable {
		final StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		final Object retVal = joinPoint.proceed();
		stopWatch.stop();

		final StringBuilder logMessage = new StringBuilder(getMethodSignature(joinPoint));
		logMessage.append(" execution time: ");
		logMessage.append(stopWatch.getTotalTimeMillis());
		logMessage.append(" ms");
		LOGGER.debug("PERF_LOG=" + logMessage.toString());
		return retVal;
	}

	private String getContextInfo() {
		String contextInfoString = StringUtils.EMPTY;
		ContextInfo contextInfo = ContextThreadLocal.get();
		if(contextInfo != null && StringUtils.isNotEmpty(contextInfo.toString())) {
			contextInfoString = contextInfo.toString();
		}
		return contextInfoString;
	}

	private String getMethodSignature(final JoinPoint joinPoint) {
		final StringBuilder logMessage = new StringBuilder();
		logMessage.append(joinPoint.getTarget().getClass().getName());
		logMessage.append(".");
		logMessage.append(joinPoint.getSignature().getName());
		logMessage.append("(");

		final Object[] args = joinPoint.getArgs();
		for(final Object arg : args) {
			if(null != arg) {
				logMessage.append(arg.getClass().getSimpleName()).append(", ");
			}
		}

		if(args.length > 0) {
			logMessage.delete(logMessage.length() - 2, logMessage.length());
		}

		logMessage.append(")");
		return logMessage.toString();
	}

	private ContextInfo getContextInfoFromMethod(final JoinPoint joinPoint) {
		ContextInfo contextInfo = ContextInfo.toContextInfo(StringUtils.EMPTY);
		int contextInfoArgumentNumber = -1;
		int counter = 0;
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		final Annotation[][] annotations = method.getParameterAnnotations();
		for(final Annotation[] annotation : annotations) {
			if(null != annotation && annotation.length > 0 && null != annotation[0]) {
				String argumentSignature = annotation[0].toString();
				if(StringUtils.contains(argumentSignature, "org.springframework.web.bind.annotation.RequestHeader") && StringUtils.contains(argumentSignature, "value=" + CommonConstants.CONTEXT_INFORMATION_REQUEST_PARAMETER)) {
					contextInfoArgumentNumber = counter;
					break;
				}
			}
			counter++;
		}

		if(contextInfoArgumentNumber > -1) {
			contextInfo = ContextInfo.toContextInfo(joinPoint.getArgs()[contextInfoArgumentNumber].toString());
		}
		return contextInfo;
	}
}