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
package com.dev.ops.exceptionmanager.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.exception.manager.ExceptionManager;
import com.dev.ops.exception.manager.constants.LoggingConstants;
import com.dev.ops.exception.manager.impl.DefaultExceptionManager;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

public class DefaultWrappedExceptionTest {

	private static ExceptionManager exceptionManager;

	@BeforeClass
	public static void setUp() {
		exceptionManager = DefaultExceptionManager.getExceptionManager();
	}

	@Test
	public void testDefaultWrappedException() {
		DefaultWrappedException exception = null;
		try {
			Class.forName("DummyUnknowClass");
		} catch(final Exception e) {
			exception = new DefaultWrappedException(LoggingConstants.ErrorMessageKeys.DEFAULT_MESSAGE_WITH_PARAMETERS, e, new String[] {"param-1"});
			ContextInfo contextInfo = new ContextInfo();
			contextInfo.setRequestId("requestId");
			contextInfo.setSessionId("sessionId");
			exceptionManager.logErrorEvent(exception, contextInfo);
		} finally {
			Assert.assertNotNull(exception);
			Assert.assertEquals(LoggingConstants.ErrorMessageValues.DEFAULT_MESSAGE_WITH_PARAMETERS + "param-1", exception.getMessage());
		}
	}

	@Test(expected = DefaultWrappedException.class)
	public void testDefaultWrappedExceptionWithoutId() throws DefaultWrappedException {
		throw new DefaultWrappedException();
	}

	@Test
	public void testCreateDefaultWrappedException() {
		final DefaultWrappedException exception = new DefaultWrappedException(LoggingConstants.ErrorMessageValues.DEFAULT_MESSAGE);
		Assert.assertNotNull(exception);
		Assert.assertEquals(LoggingConstants.ErrorMessageValues.DEFAULT_MESSAGE, exception.getMessage());
	}

	@Test
	public void testDefaultWrappedExceptionWithThrowable() {
		final DefaultWrappedException exception = new DefaultWrappedException(new Throwable());
		Assert.assertNotNull(exception);
	}

	@Test
	public void testDefaultWrappedExceptionWithThrowableAndExceptionId() {
		final DefaultWrappedException exception = new DefaultWrappedException(LoggingConstants.ErrorMessageKeys.DEFAULT_MESSAGE, new Throwable());
		Assert.assertNotNull(exception);
		Assert.assertEquals(LoggingConstants.ErrorMessageValues.DEFAULT_MESSAGE, exception.getMessage());
	}

	@Test
	public void testDefaultWrappedExceptionWithThrowableAndExceptionIdAndParameters() {
		final DefaultWrappedException exception = new DefaultWrappedException(LoggingConstants.ErrorMessageKeys.DEFAULT_MESSAGE_WITH_PARAMETERS, new Exception(), new String[] {"param-1"});
		Assert.assertNotNull(exception);
		Assert.assertEquals(LoggingConstants.ErrorMessageValues.DEFAULT_MESSAGE_WITH_PARAMETERS + "param-1", exception.getMessage());
		Assert.assertEquals(LoggingConstants.ErrorMessageKeys.DEFAULT_MESSAGE_WITH_PARAMETERS, exception.getExceptionId());
	}

	@Test
	public void testDefaultWrappedExceptionEvent() {
		final DefaultWrappedException exception = new DefaultWrappedException(LoggingConstants.ErrorMessageKeys.DEFAULT_MESSAGE, new Throwable(), null);
		exceptionManager.logErrorEvent(exception);
	}

	@Test
	public void testDefaultWrappedExceptionError() {
		final DefaultWrappedException exception = new DefaultWrappedException(LoggingConstants.ErrorMessageKeys.DEFAULT_MESSAGE, new Throwable(), null);
		exceptionManager.logErrorEvent(exception, new ContextInfo("Sample", "Sample"));
	}

	@Test
	public void testDefaultWrappedExceptionFatal() {
		final DefaultWrappedException exception = new DefaultWrappedException(LoggingConstants.ErrorMessageKeys.DEFAULT_MESSAGE, new Throwable(), null);
		exceptionManager.logErrorEvent(exception, new ContextInfo("Sample", "Sample"));
	}
}