package javax.usb;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Interface for a USB configuration.
 * <p>
 * This represents a configuration of a USB device.  The device may have multiple
 * configurations, and must have at least one configuration; only one configuration
 * (if any) can be currently active.  If the device is in an unconfigured state
 * none of its configurations are active.  If this configuration is not
 * active, its device model (UsbInterfaces, UsbEndpoints, and UsbPipes) may be browsed,
 * but no action can be taken.
 * <p>
 * See the USB 1.1 specification sec 9.6.2 for more information on USB device configurations.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public interface UsbConfiguration
{
	/**
	 * If this UsbConfiguration is active.
	 * @return if this UsbConfiguration is active.
	 */
	public boolean isActive();

    /**
	 * Get all UsbInterfaces for this configuration.
	 * <p>
	 * The returned UsbInterface settings depend on
	 * whether this configuration (and by association its contained interfaces)
	 * is active or not:
	 * <ul>
	 * <li>If this configuration is active, all UsbInterfaces will
	 * be the active alternate setting for that interface.</li>
	 * <li>If this configuration is not active, no contained interfaces
	 * are active, so they have no active alternate settings.  The UsbInterfaces
	 * will then be an implementation-dependent alternate setting UsbInterface
	 * for each iterface.  To get a specific alternate setting, use
	 * {@link javax.usb.UsbInterface#getSetting(byte)
	 * UsbInterface.getSetting(byte number)}.</li>
	 * </ul>
	 * @return All UsbInterfaces for this configuration.
	 */
    public List getUsbInterfaces();

	/**
	 * Get the UsbInterface with the specified interface number.
	 * <p>
	 * The returned interface setting will be the current active
	 * alternate setting if this configuration (and thus the contained interface)
	 * is {@link #isActive() active}.  If this configuration is not active,
	 * the returned interface setting will be an implementation-dependent alternate setting.
	 * To get a specific alternate setting, use
	 * {@link javax.usb.UsbInterface#getSetting(byte)
	 * UsbInterface.getSetting(byte number)}.
	 * <p>
	 * If the specified UsbInterface does not exist, this returns null.
	 * @param number The number of the interface to get.
	 * @return The specified UsbInterface, or null.
	 */
	public UsbInterface getUsbInterface( byte number );

	/**
	 * If the specified UsbInterface is contained in this UsbConfiguration.
	 * @param number The interface number.
	 * @return If this configuration contains the specified UsbInterface.
	 */
	public boolean containsUsbInterface( byte number );

    /**
	 * Get the parent UsbDevice that this UsbConfiguration belongs to.
	 * @return the UsbDevice that this UsbConfiguration belongs to.
	 */
    public UsbDevice getUsbDevice();

	/**
	 * Get the configuration descriptor.
	 * <p>
	 * The descriptor may be cached.
	 * @return The configuration descriptor.
	 */
	public UsbConfigurationDescriptor getUsbConfigurationDescriptor();

	/**
	 * Get the configuration String.
	 * <p>
	 * This is a convienence method.  The String may be cached.
	 * If the device does not support strings or does not define the
	 * configuration string, this returns null.
	 * @return The configuration String, or null.
	 * @exception UsbException If there was an error getting the UsbStringDescriptor.
	 * @exception UnsupportedEncodingException If the string encoding is not supported.
	 * @exception UsbDisconnectedException If the device has been disconnected.
	 */
	public String getConfigurationString() throws UsbException,UnsupportedEncodingException,UsbDisconnectedException;
}
