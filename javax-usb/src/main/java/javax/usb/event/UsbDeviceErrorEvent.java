package javax.usb.event;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import javax.usb.*;

/**
 * Indicates an error occurred on the Default Control Pipe.
 * <p>
 * This will be fired for all errors on the Default Control Pipe.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public class UsbDeviceErrorEvent extends UsbDeviceEvent
{
	/**
	 * Constructor.
	 * @param source The UsbDevice.
	 * @param irp The UsbControlIrp associated with this error.
	 */
	public UsbDeviceErrorEvent( UsbDevice source, UsbControlIrp irp )
	{
		super( source );
		usbControlIrp = irp;
	}

	/**
	 * Get the associated UsbException.
	 * @return The associated UsbException.
	 */
	public UsbException getUsbException() { return getUsbControlIrp().getUsbException(); }

	/**
	 * Get the UsbControlIrp associated with this event.
	 * @return The UsbControlIrp.
	 */
	public UsbControlIrp getUsbControlIrp() { return usbControlIrp; }

	private UsbControlIrp usbControlIrp = null;

}
