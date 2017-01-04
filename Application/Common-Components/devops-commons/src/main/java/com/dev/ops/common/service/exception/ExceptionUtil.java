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

import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.exception.manager.ExceptionManager;
import com.dev.ops.exception.manager.impl.DefaultExceptionManager;
import com.dev.ops.exceptions.impl.DefaultWrappedException;

public final class ExceptionUtil {

	private static ExceptionManager exceptionManager = DefaultExceptionManager.getExceptionManager();

	public interface ExceptionCodes {
		public interface Database {
			String PERSISTENCE_EXCEPTION = "DATABASE_PERSISTENCE_EXCEPTION";
		}

		public interface Connection {
			String CONNECTION_EXCEPTION = "CONNECTION_EXCEPTION";
		}
	}

	private ExceptionUtil() {
	}

	public static DefaultWrappedException createException(final String exceptionId, final Exception exception, final Object[] param) {
		final DefaultWrappedException wrappedException = new DefaultWrappedException(exceptionId, exception, param);
		logException(wrappedException);
		return wrappedException;
	}

	public static void logException(final Exception exception, final ContextInfo contextInfo) {
		exceptionManager.logErrorEvent(exception, contextInfo);
	}

	public static void logException(final Exception exception) {
		exceptionManager.logErrorEvent(exception);
	}
}