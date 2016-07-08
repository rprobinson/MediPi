package com.dev.ops.common.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.dev.ops.exceptions.impl.WrappedSystemException;

public final class DateUtil {

	public static final String DD_MMM_YYYY = "dd-MMM-yyyy";

	public static final String DD_MMM_YYYY_AT_HH_MM = "dd-MMM-yyyy 'at' HH:mm";

	private DateUtil() {
	}

	public static String dateTodayString() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(DD_MMM_YYYY);
		return dateFormat.format(new Date());
	}

	public static String toDateString(final Date date, final String format) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}

	public static String toDateString(final Date date) {
		return toDateString(date, DD_MMM_YYYY);
	}

	public static Date toDate(final String date, final String format) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		try {
			return dateFormat.parse(date);
		} catch(final ParseException e) {
			throw new WrappedSystemException("Error occurred while parsing the date string into format:" + format, e);
		}
	}

	public static Date toDate(final String date) {
		return toDate(date, DD_MMM_YYYY);
	}

	public static String getTimeDifference(final Timestamp startTime, final Timestamp endTime) {
		final long milliseconds = endTime.getTime() - startTime.getTime();

		final String executionTime = String.format("%02d:%02d:%02d.%03d",
													getHours(milliseconds),
													getMinutes(milliseconds),
													getSeconds(milliseconds),
													getMilliSeconds(milliseconds));
		return executionTime;
	}

	private static long getHours(final long milliseconds) {
		return TimeUnit.MILLISECONDS.toHours(milliseconds);
	}

	private static long getMinutes(final long milliseconds) {
		return TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(getHours(milliseconds));
	}

	private static long getSeconds(final long milliseconds) {
		return TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
	}

	private static long getMilliSeconds(final long milliseconds) {
		return TimeUnit.MILLISECONDS.toMillis(milliseconds) - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(milliseconds));
	}

	public static String getPerformanceTime(final Timestamp startTime) {
		return getTimeDifference(startTime, new Timestamp(System.currentTimeMillis()));
	}

	//TODO create a function which will take 3 input params (input time type, output time type, input time) which returns output time in output time type.
	public static long convertTime(final TimeUnit from, final TimeUnit to, final long time) {
		return 0L;
	}
}