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
package com.dev.ops.common.service.exception;

import org.junit.Assert;
import org.junit.Test;

import com.dev.ops.exceptions.impl.DefaultWrappedException;

public class ExceptionUtilTest {

	@Test
	public void createExceptionTest() {
		final String userUUID = "123412341234";
		final DefaultWrappedException exception = new DefaultWrappedException("USER_WITH_UUID_NOT_FOUND_EXCEPTION", null, new Object[] {userUUID});
		final DefaultWrappedException returnedException = ExceptionUtil.createException(exception.getExceptionId(), exception, null);
		Assert.assertEquals("Exception objects should be equal.", exception.getExceptionId(), returnedException.getExceptionId());
	}
}