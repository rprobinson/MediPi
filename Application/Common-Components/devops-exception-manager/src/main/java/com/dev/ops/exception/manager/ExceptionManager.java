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
package com.dev.ops.exception.manager;

import java.util.ResourceBundle;

import com.dev.ops.common.domain.ContextInfo;

public interface ExceptionManager {

	void logErrorEvent(Throwable exception);

	void logErrorEvent(String messageId, Throwable exception);

	void logErrorEvent(Throwable exception, ContextInfo contextInfo);

	void logErrorEvent(String messageId, Throwable exception, ContextInfo contextInfo);

	void logFatalEvent(Throwable exception);

	void logFatalEvent(String messageId, Throwable exception);

	void logFatalEvent(Throwable exception, ContextInfo contextInfo);

	void logFatalEvent(String messageId, Throwable exception, ContextInfo contextInfo);

	ResourceBundle getMessageResourceBundle();
}
