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
 * Exception indicating a Bit-Stuff violation.
 * <p>
 * Bit stuff violations are described in the USB 2.0 specification section 7.1.9.
 * @author Dan Streetman
 */
public class UsbBitStuffException extends UsbException
{
	/**
	 * Constructor.
	 */
	public UsbBitStuffException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbBitStuffException(String s) { super(s); }
}
