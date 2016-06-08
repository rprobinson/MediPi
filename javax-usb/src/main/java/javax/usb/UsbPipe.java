package javax.usb;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.util.List;

import javax.usb.event.*;

/**
 * Interface for a USB pipe.
 * <p>
 * See the USB 1.1 specification sec 5.3.2 for details on USB pipes.
 * Data flows in the
 * {@link javax.usb.UsbEndpoint#getDirection() direction}
 * defined by the associated {@link #getUsbEndpoint() endpoint}, except for Control
 * {@link javax.usb.UsbEndpoint#getType() type} pipes.
 * <p>
 * The implementation is not required to be Thread-safe.  If a Thread-safe UsbPipe is
 * required, use a {@link javax.usb.util.UsbUtil#synchronizedUsbPipe(UsbPipe) synchronizedUsbPipe}.
 * <p>
 * This pipe's configuration and interface setting must be active to use this pipe.
 * Any attempt to use a UsbPipe belonging to an inactive configuration or interface setting
 * will throw a UsbNotActiveException.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public interface UsbPipe
{
	/**
	 * Open this UsbPipe.
	 * <p>
	 * The pipe cannot be used for communication until it is open.
	 * <p>
	 * The implementation should, to whatever extent the platform allows,
	 * try to ensure the pipe is usable (not in error) before returning
	 * successfully.
	 * <p>
	 * If the pipe has already been opened, this will not succeed.
	 * @exception UsbException If the UsbPipe could not be opened.
	 * @exception UsbNotActiveException If the config or interface setting is not active.
	 * @exception UsbNotClaimedException If the interface is not claimed.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public void open() throws UsbException,UsbNotActiveException,UsbNotClaimedException,UsbDisconnectedException;

	/**
	 * Close this UsbPipe.
	 * <p>
	 * The pipe can only be closed while no submissions are pending.
	 * All submissions can be aborted by {@link #abortAllSubmissions() abortAllSubmissions}.
	 * <p>
	 * If the pipe is already closed, this fails.
	 * @exception UsbException If the UsbPipe could not be closed.
	 * @exception UsbNotActiveException If the UsbPipe is not active.
	 * @exception UsbNotOpenException If the UsbPipe is not open.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public void close() throws UsbException,UsbNotActiveException,UsbNotOpenException,UsbDisconnectedException;

	/**
	 * If this pipe is active.
	 * <p>
	 * This pipe is active only if it belongs to an
	 * {@link javax.usb.UsbConfiguration#isActive() active configuration} and
	 * {@link javax.usb.UsbInterface#isActive() interface setting}, otherwise it is inactive.
	 * This UsbPipe cannot be used if inactive.
	 * @return If this UsbPipe is active.
	 */
	public boolean isActive();

	/**
	 * If this pipe is open.
	 * <p>
	 * This is true after a sucessful {@link #open() open}
	 * until a successful {@link #close() close}.
	 * <p>
	 * If this pipe is not {@link #isActive() active}, this returns false.
	 * @return If this UsbPipe is open.
	 */
	public boolean isOpen();

	/**
	 * Get this pipe's UsbEndpoint.
	 * @return The associated endpoint.
	 */
	public UsbEndpoint getUsbEndpoint();

	/**
	 * Synchonously submit a byte[] to the UsbPipe.
	 * <p>
	 * This can be used for input and output.
	 * This may only be called when the pipe is {@link #isOpen() open}.
	 * The implementation must support multiple (queued) submissions.
	 * There is no maximum size restriction; the implementation will segment the buffer
	 * into multiple transactions if required.  There may be a minimum size, but it
	 * will not be more than the
	 * {@link javax.usb.UsbEndpointDescriptor#wMaxPacketSize() maximum packet size}.
	 * <p>
	 * This will block until either all data is transferred or an error occurrs.
	 * Short packets indicate either the end of data or an error.
	 * <p>
	 * The return value will indicate the number of bytes sucessfully transferred
	 * to or from the target endpoint (depending on direction).
	 * The return value will never exceed the total size of the provided buffer.
	 * If the operation was not sucessful the UsbException will
	 * accurately reflect the cause of the error.
	 * <p>
	 * Short packets are accepted.  There is no way to disable short packet acceptance using this method.
	 * See the USB 1.1 specification sec 5.3.2 for details on short packets and short packet detection.
	 * @param data The buffer to use.
	 * @return The number of bytes actually transferred.
	 * @exception UsbException If an error occurs.
	 * @exception UsbNotActiveException If the pipe is not {@link #isActive() active}.
	 * @exception UsbNotOpenException If the pipe is not {@link #isOpen() open}.
	 * @exception IllegalArgumentException If the data is null.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public int syncSubmit( byte[] data ) throws UsbException,UsbNotActiveException,UsbNotOpenException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Asynchonously submit a byte[] to the UsbPipe.
	 * <p>
	 * This can be used for input and output.
	 * This may only be called when the pipe is {@link #isOpen() open}.
	 * The implementation must support multiple (queued) submissions.
	 * There is no maximum size restriction; the implementation will segment the buffer
	 * into multiple transactions if required.  There may be a minimum size, but it
	 * will not be more than the
	 * {@link javax.usb.UsbEndpointDescriptor#wMaxPacketSize() maximum packet size}.
	 * <p>
	 * The implementation should only place this on a queue, or perform whatever
	 * minimal processing is required, and then return.  This method will not
	 * block until the submission is complete.
	 * <p>
	 * The returned UsbIrp will represent the submission.
	 * <p>
	 * Short packets are accepted.  There is no way to disable short packet acceptance using this method.
	 * See the USB 1.1 specification sec 5.3.2 for details on short packets and short packet detection.
	 * @param data The buffer to use.
	 * @return A UsbIrp representing the submission.
	 * @exception UsbException If an error occurs.
	 * @exception UsbNotActiveException If the pipe is not {@link #isActive() active}.
	 * @exception UsbNotOpenException If the pipe is not {@link #isOpen() open}.
	 * @exception IllegalArgumentException If the data is null.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public UsbIrp asyncSubmit( byte[] data ) throws UsbException,UsbNotActiveException,UsbNotOpenException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Synchonously submit a UsbIrp to the UsbPipe.
	 * <p>
	 * This can be used for input and output.
	 * This may only be called when the pipe is {@link #isOpen() open}.
	 * The implementation must support multiple (queued) submissions.
	 * There is no maximum size restriction; the implementation will segment the buffer
	 * into multiple transactions if required.  There may be a minimum size, but it
	 * will not be more than the
	 * {@link javax.usb.UsbEndpointDescriptor#wMaxPacketSize() maximum packet size}.
	 * <p>
	 * This will block until either all data is transferred or an error occurrs.
	 * Short packets indicate either the end of data or an error.
	 * <p>
	 * If this is a Control {@link javax.usb.UsbEndpoint#getType() type} pipe,
	 * the UsbIrp must be a {@link javax.usb.UsbControlIrp UsbControlIrp}.
	 * @param irp A UsbIrp to use for the submission.
	 * @exception UsbException If an error occurs.
	 * @exception UsbNotActiveException If the pipe is not {@link #isActive() active}.
	 * @exception UsbNotOpenException If the pipe is not {@link #isOpen() open}.
	 * @exception IllegalArgumentException If the UsbIrp is not valid.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public void syncSubmit( UsbIrp irp ) throws UsbException,UsbNotActiveException,UsbNotOpenException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Asynchonously submit a UsbIrp to the UsbPipe.
	 * <p>
	 * This can be used for input and output.
	 * This may only be called when the pipe is {@link #isOpen() open}.
	 * The implementation must support multiple (queued) submissions.
	 * There is no maximum size restriction; the implementation will segment the buffer
	 * into multiple transactions if required.  There may be a minimum size, but it
	 * will not be more than the
	 * {@link javax.usb.UsbEndpointDescriptor#wMaxPacketSize() maximum packet size}.
	 * <p>
	 * The implementation should only place this on a queue, or perform whatever
	 * minimal processing is required, and then return.  This method will not
	 * block until the submission is complete.
	 * <p>
	 * If this is a Control {@link javax.usb.UsbEndpoint#getType() type} pipe,
	 * the UsbIrp must be a {@link javax.usb.UsbControlIrp UsbControlIrp}.
	 * @param irp The UsbIrp to use for the submission.
	 * @exception UsbException If an error occurs.
	 * @exception UsbNotActiveException If the pipe is not {@link #isActive() active}.
	 * @exception UsbNotOpenException If the pipe is not {@link #isOpen() open}.
	 * @exception IllegalArgumentException If the UsbIrp is not valid.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public void asyncSubmit( UsbIrp irp ) throws UsbException,UsbNotActiveException,UsbNotOpenException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Synchonously submit a List of UsbIrps to the UsbPipe.
	 * <p>
	 * This is exactly the same as calling
	 * {@link #syncSubmit(UsbIrp) syncSubmit} multiple times, except:
	 * <ul>
	 * <li>The UsbIrps will be submitted synchronously on the Java level;
	 * and if the implementation permits, synchronously on the native level.</li>
	 * <li>The implementation may optimize the submissions, especially
	 * in the case of Isochronous transfers.</li>
	 * <li>If any of the UsbIrps fails (initial submisson or UsbIrp status),
	 * the implementation may either continue with the remaining UsbIrps,
	 * or fail all remaining UsbIrps.</li>
	 * </ul>
	 * <p>
	 * If this is a Control {@link javax.usb.UsbEndpoint#getType() type} pipe,
	 * the UsbIrps must be {@link javax.usb.UsbControlIrp UsbControlIrps}.
	 * @param list The List of UsbIrps.
	 * @exception UsbException If an error occurs.
	 * @exception UsbNotActiveException If the pipe is not {@link #isActive() active}.
	 * @exception UsbNotOpenException If the pipe is not {@link #isOpen() open}.
	 * @exception IllegalArgumentException If the list is empty or contains any non-UsbIrp objects, or those UsbIrp(s) are invalid.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public void syncSubmit( List list ) throws UsbException,UsbNotActiveException,UsbNotOpenException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Asynchonously submit a List of UsbIrps to the UsbPipe.
	 * <p>
	 * This is exactly the same as calling
	 * {@link #asyncSubmit(UsbIrp) asyncSubmit} multiple times, except:
	 * <ul>
	 * <li>The UsbIrps will be submitted synchronously on the Java level;
	 * and if the implementation permits, synchronously on the native level.</li>
	 * <li>The implementation may optimize the submissions, especially
	 * in the case of Isochronous transfers.</li>
	 * <li>If any of the UsbIrps fails (initial submissions only),
	 * the implementation may either continue with the remaining UsbIrps,
	 * or fail all remaining UsbIrps.</li>
	 * </ul>
	 * <p>
	 * If this is a Control {@link javax.usb.UsbEndpoint#getType() type} pipe,
	 * the UsbIrps must be {@link javax.usb.UsbControlIrp UsbControlIrps}.
	 * @param list The List of UsbIrps.
	 * @exception UsbException If an error occurs.
	 * @exception UsbNotActiveException If the pipe is not {@link #isActive() active}.
	 * @exception UsbNotOpenException If the pipe is not {@link #isOpen() open}.
	 * @exception IllegalArgumentException If the list is empty or contains any non-UsbIrp objects, or those UsbIrp(s) are invalid.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public void asyncSubmit( List list ) throws UsbException,UsbNotActiveException,UsbNotOpenException,IllegalArgumentException,UsbDisconnectedException;

	/**
	 * Stop all submissions in progress.
	 * <p>
	 * This will abort all submission in progress on the pipe,
	 * and block until all submissions are complete.
	 * There will be no submissions pending after this returns.
	 * @exception UsbNotActiveException If the pipe is not {@link #isActive() active}.
	 * @exception UsbNotOpenException If the pipe is not {@link #isOpen() open}.
	 * @exception UsbDisconnectedException If this pipe (device) has been disconnected.
	 */
	public void abortAllSubmissions() throws UsbNotActiveException,UsbNotOpenException,UsbDisconnectedException;

	/**
	 * Create a UsbIrp.
	 * <p>
	 * This creates a UsbIrp that may be optimized for use on
	 * this UsbPipe.  Using this UsbIrp instead of a
	 * {@link javax.usb.util.DefaultUsbIrp DefaultUsbIrp}
	 * may increase performance or decrease memory requirements.
	 * <p>
	 * The UsbPipe cannot require this UsbIrp to be used, all submit
	 * methods <i>must</i> accept any UsbIrp implementation
	 * (or UsbControlIrp implementation if this is a Control-type UsbPipe).
	 * @return A UsbIrp ready for use.
	 */
	public UsbIrp createUsbIrp();

	/**
	 * Create a UsbControlIrp.
	 * <p>
	 * This creates a UsbControlIrp that may be optimized for use on
	 * this UsbPipe.  Using this UsbControlIrp instead of a
	 * {@link javax.usb.util.DefaultUsbControlIrp DefaultUsbControlIrp}
	 * may increase performance or decrease memory requirements.
	 * <p>
	 * The UsbPipe cannot require this UsbControlIrp to be used, all submit
	 * methods <i>must</i> accept any UsbControlIrp implementation.
	 * <p>
	 * Note that if this is not a Control-type UsbPipe, none
	 * of the setup packet fields will be used.
	 * @param bmRequestType The bmRequestType.
	 * @param bRequest The bRequest.
	 * @param wValue The wValue.
	 * @param wIndex The wIndex.
	 * @return A UsbControlIrp ready for use.
	 */
	public UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex);

	/**
	 * Adds the listener.
	 * @param listener The UsbPipeListener.
	 */
	public void addUsbPipeListener( UsbPipeListener listener );

	/**
	 * Removes the listener.
	 * @param listener The UsbPipeListener.
	 */
	public void removeUsbPipeListener( UsbPipeListener listener );

}
