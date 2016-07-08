package com.dev.ops.common.thread.local;

import com.dev.ops.common.domain.ContextInfo;

public final class ContextThreadLocal {

	private static final ThreadLocal<ContextInfo> THREAD_LOCAL_CONTEXT_INFO = new ThreadLocal<ContextInfo>();

	private ContextThreadLocal() {
	}

	public static void set(final ContextInfo contextInfo) {
		THREAD_LOCAL_CONTEXT_INFO.set(contextInfo);
	}

	public static ContextInfo get() {
		return THREAD_LOCAL_CONTEXT_INFO.get();
	}

	public static void unset() {
		THREAD_LOCAL_CONTEXT_INFO.remove();
	}
}