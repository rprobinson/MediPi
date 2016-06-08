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
 * Interface for a USB interface.
 * <p>
 * This object actually represents a specific 'alternate' setting for a USB interface.
 * Interfaces must have at least one alternate setting, and one and only one setting
 * is active per interface.  All settings share the same interface
 * number.  If this interface setting is not active, it cannot be claimed or released;
 * the active interface setting should be used for claiming and releasing ownership of
 * the interface; also no action may be taken on any parts of this interface
 * setting, if the setting is not active.  Any attempt to perform action on objects
 * belonging to an inactive interface setting will throw a UsbNotActiveException.
 * @author Dan Streetman
 */
public interface UsbInterface
{
	/**
	 * Claim this interface.
	 * <p>
	 * This will attempt whatever claiming the native implementation provides,
	 * if any.  If the interface is already claimed, or the native claim fails,
	 * this will fail.
	 * <p>
	 * This must be done before opening and/or using any UsbPipes.
	 * @exception UsbClaimException If the interface is already claimed.
	 * @exception UsbException If the interface could not be claimed.
	 * @exception UsbNotActiveException If this interface setting is not
	 * {@link #isActive() active}.
	 * @exception UsbDisconnectedException If this interface (device) has been disconnected.
	 */
	public void claim() throws UsbClaimException,UsbException,UsbNotActiveException,UsbDisconnectedException;

	/**
	 * Claim this interface using a UsbInterfacePolicy.
	 * <p>
	 * This will attempt whatever claiming the native implementation provides,
	 * if any.  If the native claim fails, this will fail.  If the interface
	 * is already claimed, this may fail depending on the value of the
	 * {@link javax.usb.UsbInterfacePolicy#forceClaim(UsbInterface) UsbInterfacePolicy.forceClaim()}.
	 * @param policy The UsbInterfacePolicy to use.
	 * @exception UsbClaimException If the interface is already claimed.
	 * @exception UsbException If the interface could not be claimed.
	 * @exception UsbNotActiveException If this interface setting is not
	 * {@link #isActive() active}.
	 * @exception UsbDisconnectedException If this interface (device) has been disconnected.
	 */
	public void claim(UsbInterfacePolicy policy) throws UsbClaimException,UsbException,UsbNotActiveException,UsbDisconnectedException;

	/**
	 * Release this interface.
	 * <p>
	 * This will only succeed if the interface has been properly claimed.
	 * If the native release fails, this will fail.
	 * <p>
	 * This should be done after the interface is no longer being used.
	 * All pipes must be closed before this can be released.
	 * <p>
	 * @exception UsbClaimException If the interface is not claimed.
	 * @exception UsbException If the interface could not be released.
	 * @exception UsbNotActiveException If this interface setting is not
	 * {@link #isActive() active}.
	 * @exception UsbDisconnectedException If this interface (device) has been disconnected.
	 */
	public void release() throws UsbClaimException,UsbException,UsbNotActiveException,UsbDisconnectedException;

	/**
	 * If this interface is claimed.
	 * <p>
	 * This will return true if claimed in Java.
	 * This may, depending on implementation, return true if
	 * claimed natively (outside of Java)
	 * <p>
	 * If this UsbInterface is not {@link #isActive() active}, this will
	 * return if the active alternate setting is active.
	 * @return If this interface is claimed (in Java).
	 */
	public boolean isClaimed();

	/**
	 * If this interface alternate setting is active.
	 * <p>
	 * The interface itself is active if and only if its parent
	 * configuration is {@link javax.usb.UsbConfiguration#isActive() active}.
	 * If the interface itself is not active, none of its alternate settings
	 * are active.
	 * @return if this interface alternate setting is active.
	 */
	public boolean isActive();

	/**
	 * Get the number of alternate settings for this interface.
	 * @return the number of alternate settings for this interface.
	 */
	public int getNumSettings();

	/**
	 * Get the number of the active alternate setting.
	 * @return The active setting number for this interface.
	 * @exception UsbNotActiveException If the interface (and parent config) is not
	 * {@link #isActive() active}.
	 */
	public byte getActiveSettingNumber() throws UsbNotActiveException;

	/**
	 * Get the active alternate setting.
	 * <p>
	 * @return The active setting for this interface.
	 * @exception UsbNotActiveException If this interface (and parent config) is not
	 * {@link #isActive() active}.
	 */
	public UsbInterface getActiveSetting() throws UsbNotActiveException;

	/**
	 * Get the specified alternate setting.
	 * <p>
	 * If the specified setting does not exist, this returns null.
	 * @return The specified alternate setting, or null.
	 */
	public UsbInterface getSetting( byte number );

	/**
	 * If the specified alternate setting exists.
	 * @param number The alternate setting number.
	 * @return If the alternate setting exists.
	 */
	public boolean containsSetting( byte number );

	/**
	 * Get all alternate settings for this interface.
	 * <p>
	 * This returns all alternate settings, including this one.
	 * @return All alternate settings for this interface.
	 */
	public List getSettings();

    /**
	 * Get all endpoints for this interface setting.
	 * @return All endpoints for this setting.
	 */
    public List getUsbEndpoints();

	/**
	 * Get a specific UsbEndpoint.
	 * <p>
	 * If this does not contain the specified endpoint, this returns null.
	 * @param address The address of the UsbEndpoint to get.
	 * @return The specified UsbEndpoint, or null.
	 */
	public UsbEndpoint getUsbEndpoint( byte address );

	/**
	 * If the specified UsbEndpoint is contained in this UsbInterface.
	 * @param address The endpoint address.
	 * @return If this UsbInterface contains the specified UsbEndpoint.
	 */
	public boolean containsUsbEndpoint( byte address );

    /**
	 * Get the parent UsbConfiguration that this UsbInterface belongs to.
	 * @return The UsbConfiguration that this interface belongs to.
	 */
    public UsbConfiguration getUsbConfiguration();

	/**
	 * Get the interface descriptor.
	 * <p>
	 * The descriptor may be cached.
	 * @return The interface descriptor.
	 */
	public UsbInterfaceDescriptor getUsbInterfaceDescriptor();

	/**
	 * Get the interface String.
	 * <p>
	 * This is a convienence method.  The String may be cached.
	 * If the device does not support strings or does not define the
	 * interface string, this returns null.
	 * @return The interface String, or null.
	 * @exception UsbException If there was an error getting the UsbStringDescriptor.
	 * @exception UnsupportedEncodingException If the string encoding is not supported.
	 * @exception UsbDisconnectedException If this interface (device) has been disconnected.
	 */
	public String getInterfaceString() throws UsbException,UnsupportedEncodingException,UsbDisconnectedException;
}
