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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.dev.ops.common.utils.LoggingUtil;
import com.dev.ops.exception.manager.constants.LoggingConstants.LoggingPoint;

@Aspect
@Component
public class LoggingInterceptor {

	private static final Logger LOGGER = LogManager.getLogger(LoggingInterceptor.class);

	@Before("execution(* uk.gov.nhs.digital.telehealth..*.*(..)) && !execution(* com.dev.ops.common.orika..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.configurations..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.interceptors.ContextInfoInterceptor..*(..))")
	public void logMethodStart(final JoinPoint joinPoint) {
		final StringBuilder logMessage = this.getMethodSignature(joinPoint);
		LOGGER.info(LoggingUtil.getMessageDescription(LoggingPoint.START.toString(), new Object[] {logMessage}));
	}

	@After("execution(* uk.gov.nhs.digital.telehealth..*.*(..)) && !execution(* com.dev.ops.common.orika..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.configurations..*.*(..)) && !execution(* uk.gov.nhs.digital.telehealth.clinician.web.interceptors.ContextInfoInterceptor..*(..))")
	public void addContextInfoToThreadLocal(final JoinPoint joinPoint) {
		final StringBuilder logMessage = this.getMethodSignature(joinPoint);
		LOGGER.info(LoggingUtil.getMessageDescription(LoggingPoint.END.toString(), new Object[] {logMessage}));
	}

	@Around("execution(* uk.gov.nhs.digital.telehealth..controllers..*.*(..))")
	public Object logTimeMethod(final ProceedingJoinPoint joinPoint) throws Throwable {
		final StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		final Object retVal = joinPoint.proceed();
		stopWatch.stop();

		final StringBuilder logMessage = this.getMethodSignature(joinPoint);
		logMessage.append(" execution time: ");
		logMessage.append(stopWatch.getTotalTimeMillis());
		logMessage.append(" ms");
		LOGGER.debug("PERF_LOG=" + logMessage.toString());
		return retVal;
	}

	private StringBuilder getMethodSignature(final JoinPoint joinPoint) {
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
		return logMessage;
	}
}