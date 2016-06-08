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
 * Exception indicating a Babble error.
 * <p>
 * Babble errors are described in the USB 2.0 specification section 8.7.4.
 * @author Dan Streetman
 */
public class UsbBabbleException extends UsbException
{
	/**
	 * Constructor.
	 */
	public UsbBabbleException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbBabbleException(String s) { super(s); }
}
