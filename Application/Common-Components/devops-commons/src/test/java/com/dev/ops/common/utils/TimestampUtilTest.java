package com.dev.ops.common.utils;

import static org.junit.Assert.assertNotNull;
import net.sf.qualitytest.CoverageForPrivateConstructor;

import org.junit.Test;

public class TimestampUtilTest {
	@Test
	public void testCurentTimestampTest() {
		assertNotNull(TimestampUtil.getCurentTimestamp());
	}

	@Test
	public void privateConstructorTest() {
		CoverageForPrivateConstructor.giveMeCoverage(TimestampUtil.class);
	}
}