package javax.usb.event;

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
 * Class for USB device events.
 * @author E. Michael Maximilien
 */
public class UsbDeviceEvent extends EventObject
{
	/**
	 * Constructor.
	 * @param source The source UsbDevice.
	 */
	public UsbDeviceEvent( UsbDevice source ) { super(source); }

	/**
	 * Get the UsbDevice.
	 * @return The associated UsbDevice.
	 */
	public UsbDevice getUsbDevice() { return (UsbDevice)getSource(); }

}
