package com.dev.ops.common.utils;

public class MessagesUtil {

	private static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = "Unable to construct '{0}' object with invalid '{1}' attribute.";

	private MessagesUtil() {
	}

	public static String getIllegalArgumentExceptionMessage(final Class<?> clazz, final String attributeName) {
		return ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE.replace("{0}", clazz.getName()).replace("{1}", attributeName);
	}
}
