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
 * Exception indicating an operation was attempted on an
 * {@link javax.usb.UsbConfiguration#isActive() inactive UsbConfiguration},
 * {@link javax.usb.UsbInterface#isActive() inactive UsbInterface}, and/or
 * {@link javax.usb.UsbPipe#isActive() inactive UsbPipe}.
 * @author Dan Streetman
 */
public class UsbNotActiveException extends RuntimeException
{
	/**
	 * Constructor.
	 */
	public UsbNotActiveException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbNotActiveException(String s) { super(s); }

}
