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
package com.dev.ops.common.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.dev.ops.common.domain.ContextInfo;

/**
 * This class has default implementations for checking hash code and equals method.
 * @author Krishna Kuntala
 *
 */
public class BaseTestClass {

	public static final List<String> stringValuesToTest = Arrays.asList("a-value", " ", "", null);

	protected ContextInfo getContextInfo() {
		final ContextInfo contextInfo = new ContextInfo("Sample", "Sample");
		contextInfo.setSsoTicket("qqqqqqq344xxxxxxxertrtswfgf");
		contextInfo.setRequestId("1111134556");
		contextInfo.setSessionId("sessionId");
		contextInfo.setDeviceId("deviceId");
		return contextInfo;
	}

	public static final <T> void assertEqualsAndHashcodeAreSymmetric(final String message, final T expected, final T actual) {
		Assert.assertEquals("Expected should equal Actual: " + message, expected, actual);
		Assert.assertEquals("Expected hashcode should equal Actual hashcode: " + message, hashCodeOrNull(expected), hashCodeOrNull(actual));
		Assert.assertEquals("Actual should equal Expected: " + message, actual, expected);
		Assert.assertEquals("Actual hashcode should equal Expected hashcode: " + message, hashCodeOrNull(actual), hashCodeOrNull(expected));

	}

	public static final <T> void assertNotEqualsAndHashcodesDoNotMatch(final String message, final T expected, final T actual) {
		if(expected == null && actual == null) {
			Assert.fail("Did not expect both objects to be null, but they were: " + message);
		} else if(actual == null) {
			Assert.assertFalse("Expected should NOT equal Actual: " + message, expected.equals(actual));
		} else if(expected == null) {
			Assert.assertFalse("Expected should NOT equal Actual: " + message, actual.equals(expected));
		} else {
			Assert.assertFalse("Expected should NOT equal Actual: " + message, expected.equals(actual));
			Assert.assertFalse("Expected hashcode should NOT equal Actual hashcode: " + message, expected.hashCode() == actual.hashCode());
			Assert.assertFalse("Actual should NOT equal Expected: " + message, actual.equals(expected));
			Assert.assertFalse("Actual hashcode should NOT equal Expected hashcode: " + message, actual.hashCode() == expected.hashCode());
		}
	}

	public static final <T> Integer hashCodeOrNull(final T expected) {
		return expected != null ? expected.hashCode() : null;
	}
}