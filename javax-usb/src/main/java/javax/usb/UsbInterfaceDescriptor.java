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
 * Interface for a USB interface descriptor.
 * <p>
 * See the USB 1.1 specification section 9.6.3.
 * @author Dan Streetman
 */
public interface UsbInterfaceDescriptor extends UsbDescriptor
{
    /**
	 * Get this descriptor's bInterfaceNumber.
	 * @return This descriptor's bInterfaceNumber.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bInterfaceNumber();

    /**
	 * Get this descriptor's bAlternateSetting.
	 * @return This descriptor's bAlternateSetting.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bAlternateSetting();

    /**
	 * Get this descriptor's bNumEndpoints.
	 * @return This descriptor's bNumEndpoints.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bNumEndpoints();

    /**
	 * Get this descriptor's bInterfaceClass.
	 * @return This descriptor's bInterfaceClass.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bInterfaceClass();

    /**
	 * Get this descriptor's bInterfaceSubClass.
	 * @return This descriptor's bInterfaceSubClass.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bInterfaceSubClass();

    /**
	 * Get this descriptor's bInterfaceProtocol.
	 * @return This descriptor's bInterfaceProtocol.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte bInterfaceProtocol();

    /**
	 * Get this descriptor's iInterface.
	 * @return This descriptor's iInterface.
	 * @see javax.usb.util.UsbUtil#unsignedInt(byte) This is unsigned.
	 */
    public byte iInterface();
}
