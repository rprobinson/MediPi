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
 * Indicates data was successfully transferred over the Default Control Pipe.
 * <p>
 * This event will be fired on all successful transfers of data over the DCP.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public class UsbDeviceDataEvent extends UsbDeviceEvent
{
	/**
	 * Constructor.
	 * @param source The UsbDevice.
	 * @param irp The UsbControlIrp.
	 */
	public UsbDeviceDataEvent( UsbDevice source, UsbControlIrp irp )
	{
		super( source );
		usbControlIrp = irp;
	}

	/**
	 * Get the data.
	 * <p>
	 * This is a new byte[] whose length is the actual amount of transferred data.
	 * The contents is a copy of the transferred data.
	 * @return The transferred data.
	 */
	public byte[] getData()
	{
		byte[] data = new byte[getUsbControlIrp().getActualLength()];
		System.arraycopy(getUsbControlIrp().getData(), getUsbControlIrp().getOffset(), data, 0, data.length);
		return data;
	}

	/**
	 * Get the UsbControlIrp associated with this event.
	 * @return The UsbControlIrp.
	 */
	public UsbControlIrp getUsbControlIrp() { return usbControlIrp; }

	private UsbControlIrp usbControlIrp = null;

}
