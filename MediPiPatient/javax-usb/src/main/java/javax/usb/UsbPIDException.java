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
 * Exception indicating a PID error.
 * <p>
 * PID errors are described in the USB 2.0 specification section 8.3.1.
 * @author Dan Streetman
 */
public class UsbPIDException extends UsbException
{
	/**
	 * Constructor.
	 */
	public UsbPIDException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbPIDException(String s) { super(s); }
}
