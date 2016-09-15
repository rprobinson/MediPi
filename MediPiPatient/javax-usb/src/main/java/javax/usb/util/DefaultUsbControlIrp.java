package javax.usb.util;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.util.*;

import javax.usb.*;

/**
 * UsbControlIrp default implementation.
 * <p>
 * This extends DefaultUsbIrp with the Control-specific methods.
 * @author Dan Streetman
 */
public class DefaultUsbControlIrp extends DefaultUsbIrp implements UsbControlIrp
{
	/**
	 * Constructor.
	 * @param bmRequestType The bmRequestType.
	 * @param bRequest The bRequest.
	 * @param wValue The wValue.
	 * @param wIndex The wIndex.
	 */
	public DefaultUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex)
	{
		super();
		this.bmRequestType = bmRequestType;
		this.bRequest = bRequest;
		this.wValue = wValue;
		this.wIndex = wIndex;
	}

	/**
	 * Constructor.
	 * @param data The data.
	 * @param offset The offset.
	 * @param length The length.
	 * @param shortPacket The Short Packet policy.
	 * @param bmRequestType The bmRequestType.
	 * @param bRequest The bRequest.
	 * @param wValue The wValue.
	 * @param wIndex The wIndex.
	 */
	public DefaultUsbControlIrp(byte[] data, int offset, int length, boolean shortPacket, byte bmRequestType, byte bRequest, short wValue, short wIndex)
	{
		super(data, offset, length, shortPacket);
		this.bmRequestType = bmRequestType;
		this.bRequest = bRequest;
		this.wValue = wValue;
		this.wIndex = wIndex;
	}

	/**
	 * Get the bmRequestType.
	 * @return The bmRequestType.
	 */
	public byte bmRequestType() { return bmRequestType; }

	/**
	 * Get the bRequest.
	 * @return The bRequest.
	 */
	public byte bRequest() { return bRequest; }

	/**
	 * Get the wValue.
	 * @return The wValue.
	 */
	public short wValue() { return wValue; }

	/**
	 * Get the wIndex.
	 * @return The wIndex.
	 */
	public short wIndex() { return wIndex; }

	/**
	 * Get the wLength.
	 * @return The wLength.
	 */
	public short wLength() { return (short)getLength(); }

	protected byte bmRequestType = 0x00;
	protected byte bRequest = 0x00;
	protected short wValue = 0x0000;
	protected short wIndex = 0x0000;
}
