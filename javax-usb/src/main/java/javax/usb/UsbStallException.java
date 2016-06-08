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
 * Exception indicating a stall.
 * <p>
 * Stalls are described in the USB 2.0 specification section 8.4.5.
 * @author Dan Streetman
 */
public class UsbStallException extends UsbException
{
	/**
	 * Constructor.
	 */
	public UsbStallException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbStallException(String s) { super(s); }
}
