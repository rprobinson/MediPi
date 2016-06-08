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
 * Exception indicating a device has been disconnected.
 * <p>
 * This indicates the device has been disconnected from the system.  This
 * is a terminal error; the UsbDevice and subcomponents are no longer usable.
 * If the physical device is reconnected, a new UsbDevice (and subcomponents)
 * will be created.
 * @author Dan Streetman
 */
public class UsbDisconnectedException extends RuntimeException
{
	/**
	 * Constructor.
	 */
	public UsbDisconnectedException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbDisconnectedException(String s) { super(s); }
}
