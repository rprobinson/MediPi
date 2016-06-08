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
 * Class for a USB services event.
 * @author E. Michael Maximilien
 * @author Dan Streetman
 */
public class UsbServicesEvent extends EventObject
{
	/**
     * Constructor.
	 * @param source The source UsbServices.
	 * @param device The UsbDevice involved in the event.
	 */
	public UsbServicesEvent( UsbServices source, UsbDevice device )
	{
		super(source);
		usbDevice = device;
	}

    /**
	 * Get the UsbServices.
	 * @return The associated UsbServices.
	 **/
    public UsbServices getUsbServices() { return (UsbServices)getSource(); }

	/**
	 * Get the UsbDevice.
	 * @return The associated UsbDevice.
	 */
	public UsbDevice getUsbDevice() { return usbDevice; }

	private UsbDevice usbDevice = null;
}
