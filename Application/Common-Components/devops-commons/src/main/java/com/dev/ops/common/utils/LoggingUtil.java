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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.exception.manager.constants.LoggingConstants;

public class LoggingUtil {
	private static ResourceBundle resourceBundle;

	static {
		resourceBundle = ResourceBundle.getBundle(LoggingConstants.MessageBundles.LOG_MESSAGE_RESOURCE_BUNDLE);
	}

	public static String getMessageDescription(final String messageId, final Object[] param, final ContextInfo contextInfo) {
		final StringBuilder messageBuffer = new StringBuilder();

		if(contextInfo != null) {
			messageBuffer.append(contextInfo.toString());
		}

		if(resourceBundle != null) {
			if(param != null) {
				messageBuffer.append(MessageFormat.format(resourceBundle.getString(messageId), param));
			} else {
				messageBuffer.append(resourceBundle.getString(messageId));
			}
		} else {
			messageBuffer.append(messageId);
		}
		return messageBuffer.toString();
	}

	public static String getMessageDescription(final String messageId, final Object[] param) {
		return getMessageDescription(messageId, param, null);
	}
}
