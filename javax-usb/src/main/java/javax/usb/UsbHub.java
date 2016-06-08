package javax.usb;

/*
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.util.*;

/**
 * Interface for a USB hub.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public interface UsbHub extends UsbDevice
{
	/**
	 * Get the number of (downstream) ports this hub has.
	 * <p>
	 * This is only the number of ports on the hub, not
	 * all ports are necessarily enabled, available, usable, or
	 * in some cases physically present.  This only represents
	 * the number of downstream ports the hub claims to have.
	 * Note that all hubs have exactly one upstream port, which
	 * allows it to connect to the system (or another upstream hub).
	 * There is also a internal port which is generally only used
	 * by the hub itself.  See the USB 1.1 specification sec 11.4
	 * for details on the internal port, sec 11.5 for details on the
	 * downstream ports, and sec 11.6 for details on the upstream port.
	 * @return The number of (downstream) ports for this hub.
	 */
	public byte getNumberOfPorts();

	/**
	 * Get all the ports this hub has.
	 * <p>
	 * The port numbering is 1-based.
	 * <p>
	 * The List will be unmodifiable.
	 * @return All ports this hub has.
	 * @see #getUsbPort( byte number )
	 */
	public List getUsbPorts();

	/**
	 * Get a specific UsbPort by port number.
	 * <p>
	 * This gets the UsbPort with the specified number.
	 * The port numbering is 1-based (not 0-based), and
	 * the max port number is 255.  See the USB 1.1 specification
	 * table 11.8 offset 7.
	 * <p>
	 * If the specified port does not exist, this returns null.
	 * @param number The number (1-based) of the port to get.
	 * @return The specified port, or null.
	 */
	public UsbPort getUsbPort( byte number );

	/**
	 * Get all attached UsbDevices.
	 * <p>
	 * The List will be unmodifiable.
	 * @return All devices currently attached to this hub.
	 */
	public List getAttachedUsbDevices();

	/**
	 * If this is the {@link javax.usb.UsbServices#getRootUsbHub() virtual root hub}.
	 * @return If this is the virtual root hub.
	 */
	public boolean isRootUsbHub();

}
