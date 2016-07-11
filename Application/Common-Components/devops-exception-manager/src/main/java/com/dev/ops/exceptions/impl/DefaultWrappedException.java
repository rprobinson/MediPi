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
package com.dev.ops.exceptions.impl;

import java.io.Serializable;

import com.dev.ops.exception.manager.impl.DefaultExceptionManager;
import com.dev.ops.exceptions.WrappedException;

@SuppressWarnings("serial")
public class DefaultWrappedException extends Exception implements Serializable, WrappedException {
	private final String exceptionId;

	public DefaultWrappedException() {
		super();
		this.exceptionId = null;
	}

	public DefaultWrappedException(final String exceptionId, final Throwable throwable, final Object[] messageParameters) {
		super(DefaultExceptionManager.getExceptionDescription(exceptionId, messageParameters), throwable);
		this.exceptionId = exceptionId;
	}

	public DefaultWrappedException(final String exceptionId, final Throwable throwable) {
		this(exceptionId, throwable, null);
	}

	public DefaultWrappedException(final Throwable throwable) {
		this(null, throwable);
	}

	public DefaultWrappedException(final String exceptionId) {
		this(exceptionId, null);
	}

	@Override
	public String getExceptionId() {
		return this.exceptionId;
	}
}