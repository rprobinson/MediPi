package com.dev.ops.common.utils;

import java.sql.Timestamp;

public final class TimestampUtil {

	private TimestampUtil() {
	}

	public static Timestamp getCurentTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	public static Timestamp getCurentPlusAdditionalTimestamp(final long additionalTime) {
		return new Timestamp(System.currentTimeMillis() + additionalTime);
	}

}