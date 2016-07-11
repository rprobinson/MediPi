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

public class MessagesUtil {

	private static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = "Unable to construct '{0}' object with invalid '{1}' attribute.";

	private MessagesUtil() {
	}

	public static String getIllegalArgumentExceptionMessage(final Class<?> clazz, final String attributeName) {
		return ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE.replace("{0}", clazz.getName()).replace("{1}", attributeName);
	}
}
