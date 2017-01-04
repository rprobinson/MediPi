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
 * Interface for receiving UsbPipeEvents.
 * @author E. Michael Maximilien
 * @author Dan Streetman
 */
public interface UsbPipeListener extends EventListener
{
    /**
	 * An error occurred.
     * @param event The UsbPipeErrorEvent.
     */
    public void errorEventOccurred( UsbPipeErrorEvent event );

    /**
	 * Data was successfully transferred.
     * @param event The UsbPipeDataEvent.
     */
    public void dataEventOccurred( UsbPipeDataEvent event );

}
