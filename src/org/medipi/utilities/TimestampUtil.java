package org.medipi.utilities;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This utility class consists the methods related to Timestamp.
 */
public class TimestampUtil {

	/**
	 * Convert String dateTime to java.sql.Timestamp
	 *
	 * @param format the format of the date string
	 * @param dateString the date string
	 * @return the java.sql.Timestamp
	 * @throws ParseException the parse exception
	 */
	public static Timestamp getTimestamp(final String format, final String dateString) throws ParseException {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		final Date parsedDate = dateFormat.parse(dateString);
		final Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
		return timestamp;
	}

	/**
	 * Converts java.sql.Timestamp to string date
	 *
	 * @param format the format of the date string
	 * @param date the java.sql.Timestamp object to be converted to string
	 * @return the converted string date time
	 * @throws ParseException the parse exception
	 */
	public static String getStringDate(final String format, final Timestamp date) throws ParseException {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
}
