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
 * Interface for a USB configuration descriptor.
 * <p>
 * See the USB 1.1 specification section 9.6.2.
 * @author Dan Streetman
 */
public interface UsbConfigurationDescriptor extends UsbDescriptor
{
	/**
	 * Get this descriptor's wTotalLength.
	 * @return This descriptor's wTotalLength.
	 * @see javax.usb.util.UsbUtil#unsignedInt(short) This is unsigned.
	 */
	public short wTotalLength();

    /**
	 * Get this descriptor's bNumInterfaces.
	 * @return This descriptor's bNumInterfaces.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bNumInterfaces();

    /**
	 * Get this descriptor's bConfigurationValue.
	 * @return This descriptor's bConfigurationValue.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bConfigurationValue();

    /**
	 * Get this descriptor's iConfiguration.
	 * @return This descriptor's iConfiguration.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte iConfiguration();

    /**
	 * Get this descriptor's bmAttributes.
	 * @return This descriptor's bmAttributes.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
     */
    public byte bmAttributes();

    /**
	 * Get this descriptor's bMaxPower.
	 * <p>
	 * This is specified in units of 2mA.
	 * @return This descriptor's bMaxPower.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bMaxPower();
}
