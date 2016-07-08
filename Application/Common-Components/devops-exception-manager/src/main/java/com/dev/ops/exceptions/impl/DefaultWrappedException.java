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