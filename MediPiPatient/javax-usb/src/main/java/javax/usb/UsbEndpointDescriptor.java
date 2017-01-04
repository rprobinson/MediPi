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
 * Interface for a USB endpoint descriptor.
 * <p>
 * See the USB 1.1 specification section 9.6.4.
 * @author Dan Streetman
 */
public interface UsbEndpointDescriptor extends UsbDescriptor
{
    /**
	 * Get this descriptor's bEndpointAddress.
	 * @return This descriptor's bEndpointAddress.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bEndpointAddress();

    /**
	 * Get this descriptor's bmAttributes.
	 * @return This descriptor's bmAttributes.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bmAttributes();

    /**
	 * Get this descriptor's wMaxPacketSize.
	 * @return This descriptor's wMaxPacketSize.
	 * @see javax.usb.util.UsbUtil#unsignedInt(short) This is unsigned.
	 */
    public short wMaxPacketSize();

    /**
	 * Get this descriptor's bInterval.
	 * @return This descriptor's bInterval.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bInterval();
}
