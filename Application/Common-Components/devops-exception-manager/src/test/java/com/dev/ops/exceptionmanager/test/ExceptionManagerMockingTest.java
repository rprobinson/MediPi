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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.dev.ops.exception.manager.ExceptionManager;
import com.dev.ops.exception.manager.impl.DefaultExceptionManager;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionManagerMockingTest {

	@Test
	public void testLogErrorEvent_FATAL_NoStacktrace() {
		final ExceptionManager exceptionManager = DefaultExceptionManager.getExceptionManager();
		Assert.assertNotNull(exceptionManager);
		exceptionManager.logErrorEvent(new DefaultWrappedException(), null);
	}
}