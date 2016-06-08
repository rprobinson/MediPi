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
 * Exception indicating a short packet was detected.
 * <p>
 * Short packets are described in the USB 2.0 specification section 5.3.2.
 * @author Dan Streetman
 * @see javax.usb.UsbIrp#getAcceptShortPacket() UsbIrps indicate if this UsbException should be generated or not.
 */
public class UsbShortPacketException extends UsbException
{
	/**
	 * Constructor.
	 */
	public UsbShortPacketException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbShortPacketException(String s) { super(s); }
}
