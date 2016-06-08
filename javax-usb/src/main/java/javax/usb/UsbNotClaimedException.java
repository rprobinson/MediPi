package javax.usb;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/claimedsource/license-cpl.html
 */

/**
 * Exception indicating an operation was attempted on a
 * {@link javax.usb.UsbInterface#isClaimed() unclaimed UsbInterface}.
 * @author Dan Streetman
 */
public class UsbNotClaimedException extends RuntimeException
{
	/**
	 * Constructor.
	 */
	public UsbNotClaimedException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbNotClaimedException(String s) { super(s); }

}
