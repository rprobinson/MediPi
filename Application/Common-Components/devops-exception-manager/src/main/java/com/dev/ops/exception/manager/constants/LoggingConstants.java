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

package com.dev.ops.exception.manager.constants;

public interface LoggingConstants {

	String AUDIT_MESSAGE = "AUDIT_MESSAGE";
	String ERROR_MESSAGE = "errorMessage";

	interface MessageBundles {
		String LOG_MESSAGE_RESOURCE_BUNDLE = "log-messages-store";
	}

	interface WhitespaceLiterals {
		String BLANK_SPACE = " ";
		String ATTRIBUTES_SEPERATOR = " | ";
		String NAME_VALUE_SEPERATOR = " : ";
		String EQUAL_TO = "==";
		String HYPHEN = "-";
	}

	interface ErrorMessageKeys {
		String DEFAULT_MESSAGE = "DEFAULT_MESSAGE";
		String DEFAULT_MESSAGE_WITH_PARAMETERS = "DEFAULT_MESSAGE_WITH_PARAMETERS";
	}

	interface ErrorMessageValues {
		String DEFAULT_MESSAGE = ErrorMessageKeys.DEFAULT_MESSAGE + LoggingConstants.WhitespaceLiterals.BLANK_SPACE + LoggingConstants.WhitespaceLiterals.HYPHEN + LoggingConstants.WhitespaceLiterals.BLANK_SPACE + "Exception occurred";
		String DEFAULT_MESSAGE_WITH_PARAMETERS = ErrorMessageKeys.DEFAULT_MESSAGE_WITH_PARAMETERS + LoggingConstants.WhitespaceLiterals.BLANK_SPACE + LoggingConstants.WhitespaceLiterals.HYPHEN + LoggingConstants.WhitespaceLiterals.BLANK_SPACE + "Exception occurred with parameter1=";
	}

	public enum LoggingPoint {
		START, END, ERROR
	}
}
