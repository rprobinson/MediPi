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
 * Exception indicating an UsbInterface is already natively claimed.
 * @author Dan Streetman
 */
public class UsbNativeClaimException extends UsbClaimException
{
	/**
	 * Constructor.
	 */
	public UsbNativeClaimException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbNativeClaimException(String s) { super(s); }
}
