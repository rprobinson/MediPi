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
 * Interface for a USB device descriptor.
 * <p>
 * See the USB 1.1 specification section 9.6.1.
 * @author Dan Streetman
 */
public interface UsbDeviceDescriptor extends UsbDescriptor
{
    /**
	 * Get this descriptor's bcdUSB.
	 * @return This descriptor's bcdUSB.
	 * @see javax.usb.util.UsbUtil#unsignedInt(short) This is unsigned.
	 */
    public short bcdUSB();

    /**
	 * Get this descriptor's bDeviceClass.
	 * @return This descriptor's bDeviceClass.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bDeviceClass();

	/**
	 * Get this descriptor's bDeviceSubClass.
	 * @return This descriptor's bDeviceSubClass.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bDeviceSubClass();

    /**
	 * Get this descriptor's bDeviceProtocol.
	 * @return This descriptor's bDeviceProtocol.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bDeviceProtocol();

    /**
	 * Get this descriptor's bMaxPacketSize.
	 * @return This descriptor's bMaxPacketSize.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bMaxPacketSize0();

    /**
	 * Get this descriptor's idVendor.
	 * @return This descriptor's idVendor.
	 * @see javax.usb.util.UsbUtil#unsignedInt(short) This is unsigned.
	 */
    public short idVendor();

    /**
	 * Get this descriptor's idProduct.
	 * @return This descriptor's idProduct.
	 * @see javax.usb.util.UsbUtil#unsignedInt(short) This is unsigned.
	 */
    public short idProduct();

    /**
	 * Get this descriptor's bcdDevice.
	 * @return This descriptor's bcdDevice.
	 * @see javax.usb.util.UsbUtil#unsignedInt(short) This is unsigned.
	 */
    public short bcdDevice();

    /**
	 * Get this descriptor's iManufacturer.
	 * @return This descriptor's iManufacturer.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte iManufacturer();

    /**
	 * Get this descriptor's iProduct.
	 * @return This descriptor's iProduct.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte iProduct();

    /**
	 * Get this descriptor's iSerialNumber.
	 * @return This descriptor's iSerialNumber.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte iSerialNumber();

    /**
	 * Get this descriptor's bNumConfigurations.
	 * @return This descriptor's bNumConfigurations.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bNumConfigurations();
}
