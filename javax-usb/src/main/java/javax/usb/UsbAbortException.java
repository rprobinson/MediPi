package javax.usb;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

/**
 * Exception indicating a submission was aborted.
 * <p>
 * Submissions are normally aborted via the
 * {@link javax.usb.UsbPipe#abortAllSubmissions() abortAllSubmissions} method.
 * @author Dan Streetman
 */
public class UsbAbortException extends UsbException
{
	/**
	 * Constructor.
	 */
	public UsbAbortException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbAbortException(String s) { super(s); }
}
