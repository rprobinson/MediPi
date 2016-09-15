package javax.usb;

/*
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.usb.event.*;

/**
 * Interface for a USB device.
 * <p>
 * The submission methods contained in this UsbDevice operate on the device's Default Control Pipe.
 * The device does not have to be {@link #isConfigured() configured} to use the Default Control Pipe.
 * <p>
 * The implementation is not required to be Thread-safe.  If a Thread-safe UsbDevice
 * is required, use a {@link javax.usb.util.UsbUtil#synchronizedUsbDevice(UsbDevice) synchronizedUsbDevice}.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public interface UsbDevice
{
	/**
	 * Get the UsbPort on the parent UsbHub that this device is connected to.
	 * @return The port on the parent UsbHub that this is attached to.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
	public UsbPort getParentUsbPort() throws UsbDisconnectedException;

    /**
	 * If this is a UsbHub.
	 * @return true if this is a UsbHub.
	 */
    public boolean isUsbHub();

    /**
	 * Get the manufacturer String.
	 * <p>
	 * This is a convienence method, which uses
	 * {@link #getString(byte) getString}.
	 * @return The manufacturer String, or null.
	 * @exception UsbException If there was an error getting the UsbStringDescriptor.
	 * @exception UnsupportedEncodingException If the string encoding is not supported.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
    public String getManufacturerString() throws UsbException,UnsupportedEncodingException,UsbDisconnectedException;

    /**
	 * Get the serial number String.
	 * <p>
	 * This is a convienence method, which uses
	 * {@link #getString(byte) getString}.
	 * @return The serial number String, or null.
	 * @exception UsbException If there was an error getting the UsbStringDescriptor.
	 * @exception UnsupportedEncodingException If the string encoding is not supported.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
    public String getSerialNumberString() throws UsbException,UnsupportedEncodingException,UsbDisconnectedException;

    /**
	 * Get the product String.
	 * <p>
	 * This is a convienence method, which uses
	 * {@link #getString(byte) getString}.
	 * @return The product String, or null.
	 * @exception UsbException If there was an error getting the UsbStringDescriptor.
	 * @exception UnsupportedEncodingException If the string encoding is not supported.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
    public String getProductString() throws UsbException,UnsupportedEncodingException,UsbDisconnectedException;

    /**
	 * Get the speed of the device.
	 * <p>
	 * The speed will be one of:
	 * <ul>
	 * <li>{@link javax.usb.UsbConst#DEVICE_SPEED_UNKNOWN UsbConst.DEVICE_SPEED_UNKNOWN}</li>
	 * <li>{@link javax.usb.UsbConst#DEVICE_SPEED_LOW UsbConst.DEVICE_SPEED_LOW}</li>
	 * <li>{@link javax.usb.UsbConst#DEVICE_SPEED_FULL UsbConst.DEVICE_SPEED_FULL}</li>
	 * </ul>
	 * @return The speed of this device.
	 */
    public Object getSpeed();

    /**
	 * Get all UsbConfigurations for this device.
	 * <p>
	 * The List is unmodifiable.
	 * @return All UsbConfigurations for this device.
	 */
    public List getUsbConfigurations();

	/**
	 * Get the specified UsbConfiguration.
	 * <p>
	 * If the specified UsbConfiguration does not exist, null is returned.
	 * Config number 0 is reserved for the Not Configured state (see the USB 1.1 specification
	 * section 9.4.2).  Obviously, no UsbConfiguration exists for the Not Configured state.
	 * @return The specified UsbConfiguration, or null.
	 */
	public UsbConfiguration getUsbConfiguration( byte number );

	/**
	 * If this UsbDevice contains the specified UsbConfiguration.
	 * <p>
	 * This will return false for zero (the Not Configured state).
	 * @return If the specified UsbConfiguration is contained in this UsbDevice.
	 */
	public boolean containsUsbConfiguration( byte number );

	/**
	 * Get the number of the active UsbConfiguration.
	 * <p>
	 * If the device is in a Not Configured state, this will return zero.
	 * @return The active config number.
	 */
	public byte getActiveUsbConfigurationNumber();

    /**
	 * Get the active UsbConfiguration.
	 * <p>
	 * If this device is Not Configured, this returns null.
	 * @return The active UsbConfiguration, or null.
	 */
    public UsbConfiguration getActiveUsbConfiguration();

	/**
	 * If this UsbDevice is configured.
	 * <p>
	 * This returns true if the device is in the configured state
	 * as shown in the USB 1.1 specification table 9.1.
	 * @return If this is in the Configured state.
	 */
	public boolean isConfigured();

	/**
	 * Get the device descriptor.
	 * <p>
	 * The descriptor may be cached.
	 * @return The device descriptor.
	 */
	public UsbDeviceDescriptor getUsbDeviceDescriptor();

	/**
	 * Get the specified string descriptor.
	 * <p>
	 * This is a convienence method.  The UsbStringDescriptor may be cached.
	 * If the device does not support strings or does not define the
	 * specified string descriptor, this returns null.
	 * @param index The index of the string descriptor to get.
	 * @return The specified string descriptor.
	 * @exception UsbException If an error occurred while getting the string descriptor.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
	public UsbStringDescriptor getUsbStringDescriptor( byte index ) throws UsbException,UsbDisconnectedException;

	/**
	 * Get the String from the specified string descriptor.
	 * <p>
	 * This is a convienence method, which uses
	 * {@link #getUsbStringDescriptor(byte) getUsbStringDescriptor()}.
	 * {@link javax.usb.UsbStringDescriptor#getString() getString()}.
	 * @param index The index of the string to get.
	 * @return The specified String.
	 * @exception UsbException If an error occurred while getting the String.
	 * @exception UnsupportedEncodingException If the string encoding is not supported.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
	public String getString( byte index ) throws UsbException,UnsupportedEncodingException,UsbDisconnectedException;

	/**
	 * Submit a UsbControlIrp synchronously to the Default Control Pipe.
	 * @param irp The UsbControlIrp.
	 * @exception UsbException If an error occurrs.
	 * @exception IllegalArgumentException If the UsbControlIrp is not valid.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
	public void syncSubmit( UsbControlIrp irp ) throws UsbException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Submit a UsbControlIrp asynchronously to the Default Control Pipe.
	 * @param irp The UsbControlIrp.
	 * @exception UsbException If an error occurrs.
	 * @exception IllegalArgumentException If the UsbControlIrp is not valid.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
	public void asyncSubmit( UsbControlIrp irp ) throws UsbException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Submit a List of UsbControlIrps synchronously to the Default Control Pipe.
	 * <p>
	 * All UsbControlIrps are guaranteed to be atomically (with respect to other clients
	 * of this API) submitted to the Default Control Pipe.  Atomicity on a native level
	 * is implementation-dependent.
	 * @param list The List of UsbControlIrps.
	 * @exception UsbException If an error occurrs.
	 * @exception IllegalArgumentException If the List contains non-UsbControlIrp objects or those UsbIrp(s) are invalid.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
	public void syncSubmit( List list ) throws UsbException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Submit a List of UsbControlIrps asynchronously to the Default Control Pipe.
	 * <p>
	 * All UsbControlIrps are guaranteed to be atomically (with respect to other clients
	 * of this API) submitted to the Default Control Pipe.  Atomicity on a native level
	 * is implementation-dependent.
	 * @param list The List of UsbControlIrps.
	 * @exception UsbException If an error occurrs.
	 * @exception IllegalArgumentException If the List contains non-UsbControlIrp objects or those UsbIrp(s) are invalid.
	 * @exception UsbDisconnectedException If this device has been disconnected.
	 */
	public void asyncSubmit( List list ) throws UsbException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Create a UsbControlIrp.
	 * <p>
	 * This creates a UsbControlIrp that may be optimized for use on
	 * this UsbDevice.  Using this UsbIrp instead of a
	 * {@link javax.usb.util.DefaultUsbControlIrp DefaultUsbControlIrp}
	 * may increase performance or decrease memory requirements.
	 * <p>
	 * The UsbDevice cannot require this UsbControlIrp to be used, all submit
	 * methods <i>must</i> accept any UsbControlIrp implementation.
	 * @param bmRequestType The bmRequestType.
	 * @param bRequest The bRequest.
	 * @param wValue The wValue.
	 * @param wIndex The wIndex.
	 * @return A UsbControlIrp ready for use.
	 */
	public UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex);

	/**
	 * Add a UsbDeviceListener to this UsbDevice.
	 * @param listener The UsbDeviceListener to add.
	 */
	public void addUsbDeviceListener( UsbDeviceListener listener );

	/**
	 * Remove a UsbDeviceListener from this UsbDevice.
	 * @param listener The listener to remove.
	 */
	public void removeUsbDeviceListener( UsbDeviceListener listener );

}
