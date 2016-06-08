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
 * Interface for a USB descriptor.
 * @author Dan Streetman
 */
public interface UsbDescriptor
{
    /**
	 * Get this descriptor's bLength.
	 * @return This descriptor's bLength.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bLength();

    /**
	 * Get this descriptor's bDescriptorType.
	 * @return This descriptor's bDescriptorType.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bDescriptorType();
}
