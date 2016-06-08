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
 * Exception indicating an UsbInterface claim state prevents the current operation.
 * <p>
 * This is thrown, for example, when trying to claim an already-claimed interface,
 * or trying to release an unclaimed interface.
 * @author Dan Streetman
 */
public class UsbClaimException extends UsbException
{
	/**
	 * Constructor.
	 */
	public UsbClaimException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbClaimException(String s) { super(s); }
}
