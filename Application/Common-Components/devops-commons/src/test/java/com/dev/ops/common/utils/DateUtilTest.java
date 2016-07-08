package com.dev.ops.common.utils;

import org.junit.Test;

public class DateUtilTest {

	@Test(expected = RuntimeException.class)
	public void testDateException() {
		final String faultDateString = "faultDateString";
		DateUtil.toDate(faultDateString, "dd-MMM-yyyy");
	}
}