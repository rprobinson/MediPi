package javax.usb.event;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.util.EventListener;

/**
 * Interface for receiving UsbDeviceEvents.
 * @author E. Michael Maximilien
 */
public interface UsbDeviceListener extends EventListener
{
    /**
     * The UsbDevice was detached.
     * @param event The UsbDeviceEvent.
     */
    public void usbDeviceDetached( UsbDeviceEvent event );

    /**
	 * An error occurred.
     * @param event The UsbDeviceErrorEvent.
     */
    public void errorEventOccurred( UsbDeviceErrorEvent event );

    /**
	 * Data was successfully transferred.
     * @param event The UsbDeviceDataEvent.
     */
    public void dataEventOccurred( UsbDeviceDataEvent event );
}
