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
 * Exception specific to USB.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public class UsbException extends Exception
{
	/**
	 * Constructor.
	 */
	public UsbException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbException(String s) { super(s); }
}
