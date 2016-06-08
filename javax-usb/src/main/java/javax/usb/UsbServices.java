package javax.usb;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import javax.usb.event.*;

/**
 * Interface for a javax.usb implementation.
 * <p>
 * This is instantiated by the UsbHostManager.
 * The implementation must include a no-parameter constructor.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public interface UsbServices
{
	/**
	 * Get the virtual UsbHub to which all physical Host Controller UsbHubs are attached.
	 * <p>
	 * The USB root hub is a special hub at the top of the topology tree.
	 * The USB 1.1 specification mentions root hubs in sec 5.2.3,
	 * where it states that 'the host includes an embedded hub called
	 * the root hub'.  The implication of this seems to be that the
	 * (hardware) Host Controller device is the root hub, since
	 * the Host Controller device 'emulates' a USB hub, and in
	 * systems with only one physical Host Controller device, its
	 * emulated hub is in effect the root hub.  However when
	 * multiple Host Controller devices are considered, there are
	 * two (2) options that were considered:
	 * <ol>
	 * <li>Have an array or list of the available topology trees,
	 * with each physical Host Controller's emulated root hub as
	 * the root UsbHub of that particular topology tree.  This
	 * configuration could be compared to the MS-DOS/Windows decision
	 * to assign drive letters to different physical drives (partitions).
	 * </li>
	 * <li>Have a 'virtual' root hub, which is completely virtual (not
	 * associated with any physical device) and is created and managed
	 * solely by the javax.usb implementation.  This configuration could
	 * be compared to the UNIX descision to put all physical drives
	 * on 'mount points' under a single 'root' (/) directory filesystem.
	 * </li>
	 * </ol>
	 * <p>
	 * The first configuration results in having to maintain
	 * a list of different and completely unconnected device topologies.
	 * This means a search for a particular device must be performed on
	 * all the device topologies.  Since a UsbHub already has a list of
	 * UsbDevices, and a UsbHub <i>is</i> a UsbDevice, introducing a new,
	 * different list is not a desirable action, since it introduces extra
	 * unnecessary steps in performing actions, like searching.
	 * <p>
	 * As an example, a recursive search for a certain device
	 * in the first configuration involves getting the first root UsbHub,
	 * getting all its attached UsbDevices, and checking each device;
	 * any of those devices which are UsbHubs can be also searched recursively.
	 * Then, the entire operation must be performed on the next root UsbHub,
	 * and this is repeated for all the root UsbHubs in the array/list.
	 * In the second configuration, the virtual root UsbHub is recursively
	 * searched in a single operation.
	 * <p>
	 * The second configuration is what is used in this API.  The implementation
	 * is responsible for creating a single root UsbHub which is completely
	 * virtual (and available through the UsbServices object).  Every
	 * UsbHub attached to this virtual root UsbHub corresponds to a
	 * physical Host Controller's emulated hub.  I.e., the first level of
	 * UsbDevices under the virtual root UsbHub are all UsbHubs corresponding
	 * to a particular Host Controller on the system.  Note that since
	 * the root UsbHub is a virtual hub, the number of ports is not
	 * posible to specify; so all that is guaranteed is the number of ports
	 * is at least equal to the number of UsbHubs attached to the root UsbHub.
	 * The number of ports on the virtual root UsbHub may change if UsbHubs
	 * are attached or detached (e.g., if a Host Controller is physically
	 * hot-removed from the system or hot-plugged, or if its driver is
	 * dynamically loaded, or for any other reason a top-level Host Controller's
	 * hub is attached/detached).  This API specification suggests that the
	 * number of ports for the root UsbHub equal the number of directly
	 * attached UsbHubs.
	 * @return The virtual UsbHub object.
	 * @exception UsbException If there is an error accessing javax.usb.
	 * @exception SecurityException If current client not configured to access javax.usb.
	 */
	public UsbHub getRootUsbHub() throws UsbException,SecurityException;

	/**
	 * Add UsbServicesListener.
	 * @param listener The UsbServicesListener.
	 */
	public void addUsbServicesListener( UsbServicesListener listener );

	/**
	 * Remove UsbServicesListener.
	 * @param listener The UsbServicesListener.
	 */
	public void removeUsbServicesListener( UsbServicesListener listener );

	/**
	 * Get the (minimum) version number of the javax.usb API
	 * that this UsbServices implements.
	 * <p>
	 * This should correspond to the output of (some version of) the
	 * {@link javax.usb.Version#getApiVersion() javax.usb.Version}.
	 * @return the version number of the minimum API version.
	 */
	public String getApiVersion();

	/**
	 * Get the version number of the UsbServices implementation.
	 * <p>
	 * The format should be <major>.<minor>.<revision>
	 * @return the version number of the UsbServices implementation.
	 */
	public String getImpVersion();

	/**
	 * Get a description of this UsbServices implementation.
	 * <p>
	 * The format is implementation-specific, but should include at least
	 * the following:
	 * <ul>
	 * <li>The company or individual author(s).</li>
	 * <li>The license, or license header.</li>
	 * <li>Contact information.</li>
	 * <li>The minimum or expected version of Java.</li>
	 * <li>The Operating System(s) supported (usually one per implementation).</li>
	 * <li>Any other useful information.</li>
	 * </ul>
	 * @return a description of the implementation.
	 */
	public String getImpDescription();
}
