package javax.usb.util;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.util.*;

import javax.usb.*;

/**
 * UsbIrp default implementation.
 * <p>
 * The behavior and defaults follow those defined in the {@link javax.usb.UsbIrp interface}.
 * Any of the fields may be updated if the default is not appropriate; in most cases
 * the {@link #getData() data} will be the only field that needs to be {@link #setData(byte[]) set}.
 * @author Dan Streetman
 */
public class DefaultUsbIrp implements UsbIrp
{
	/** Constructor. */
	public DefaultUsbIrp() { }

	/**
	 * Constructor.
	 * @param data The data.
	 * @exception IllegalArgumentException If the data is null.
	 */
	public DefaultUsbIrp(byte[] data) { setData(data); }

	/**
	 * Constructor.
	 * @param data The data.
	 * @param offset The offset.
	 * @param length The length.
	 * @param shortPacket The Short Packet policy.
	 * @exception IllegalArgumentException If the data is null, or the offset and/or length is negative.
	 */
	public DefaultUsbIrp(byte[] data, int offset, int length, boolean shortPacket)
	{
		setData(data,offset,length);
		setAcceptShortPacket(shortPacket);
	}

	/**
	 * Get the data.
	 * @return The data.
	 */
	public byte[] getData() { return data; }

	/**
	 * Get the offset.
	 * @return The offset.
	 */
	public int getOffset() { return offset; }

	/**
	 * Get the length.
	 * @return The length.
	 */
	public int getLength() { return length; }

	/**
	 * Get the actual length.
	 * @return The actual length.
	 */
	public int getActualLength() { return actualLength; }

	/**
	 * Set the data, offset, and length.
	 * @param d The data.
	 * @param o The offset.
	 * @param l The length.
	 * @exception IllegalArgumentException If the data is null, or the offset and/or length is negative.
	 */
	public void setData( byte[] d, int o, int l ) throws IllegalArgumentException
	{
		if (null == d)
			throw new IllegalArgumentException("Data cannot be null.");

		data = d;
		setOffset(o);
		setLength(l);
	}

	/**
	 * Set the data.
	 * @param d The data.
	 * @exception IllegalArgumentException If the data is null.
	 */
	public void setData( byte[] d ) throws IllegalArgumentException
	{
		if (null == d)
			throw new IllegalArgumentException("Data cannot be null.");

		setData(d, 0, d.length);
	}

	/**
	 * Set the offset.
	 * @param o The offset.
	 * @exception IllegalArgumentException If the offset is negative.
	 */
	public void setOffset(int o) throws IllegalArgumentException
	{
		if (0 > o)
			throw new IllegalArgumentException("Offset cannot be negative.");

		offset = o;
	}

	/**
	 * Set the length.
	 * @param l The length.
	 * @exception IllegalArgumentException If the length is negative.
	 */
	public void setLength(int l) throws IllegalArgumentException
	{
		if (0 > l)
			throw new IllegalArgumentException("Length cannot be negative");

		length = l;
	}

	/**
	 * Set the actual length.
	 * @param l The actual length.
	 * @exception IllegalArgumentException If the length is negative.
	 */
	public void setActualLength(int l) throws IllegalArgumentException
	{
		if (0 > l)
			throw new IllegalArgumentException("Actual length cannot be negative");

		actualLength = l;
	}

	/**
	 * If a UsbException occurred.
	 * @return If a UsbException occurred.
	 */
	public boolean isUsbException() { return ( null != getUsbException() ); }

	/**
	 * Get the UsbException.
	 * @return The UsbException, or null.
	 */
	public UsbException getUsbException() { return usbException; }

	/**
	 * Set the UsbException.
	 * @param exception The UsbException.
	 */
	public void setUsbException( UsbException exception ) { usbException = exception; }

	/**
	 * Get the Short Packet policy.
	 * @return The Short Packet policy.
	 */
	public boolean getAcceptShortPacket() { return acceptShortPacket; }

	/**
	 * Set the Short Packet policy.
	 * @param accept The Short Packet policy.
	 */
	public void setAcceptShortPacket( boolean accept ) { acceptShortPacket = accept; }

	/**
	 * If this is complete.
	 * @return If this is complete.
	 */
	public boolean isComplete() { return complete; }

	/**
	 * Set this as complete (or not).
	 * @param b If this is complete (or not).
	 */
	public void setComplete( boolean b ) { complete = b; }

	/**
	 * Complete this submission.
	 * <p>
	 * This will:
	 * <ul>
	 * <li>{@link #setComplete(boolean) Set} this {@link #isComplete() complete}.</li>
	 * <li>Notify all {@link #waitUntilComplete() waiting Threads}.</li>
	 * </ul>
	 */
	public void complete()
	{
		setComplete(true);
		synchronized(waitLock) { waitLock.notifyAll(); }
	}

	/**
	 * Wait until {@link #isComplete() complete}.
	 * <p>
	 * This will block until this is {@link #isComplete() complete}.
	 */
	public void waitUntilComplete()
	{
		synchronized ( waitLock ) {
			while (!isComplete()) {
				try { waitLock.wait(); }
				catch ( InterruptedException iE ) { }
			}
		}
	}

	/**
	 * Wait until {@link #isComplete() complete}, or the timeout has expired.
	 * <p>
	 * This will block until this is {@link #isComplete() complete},
	 * or the timeout has expired.  If the timeout is 0 or less,
	 * this behaves as the {@link #waitUntilComplete() no-timeout method}.
	 * @param timeout The maximum number of milliseconds to wait.
	 */
	public void waitUntilComplete( long timeout )
	{
		if (0 >= timeout) {
			waitUntilComplete();
			return;
		}

		long startTime = System.currentTimeMillis();

		synchronized ( waitLock ) {
			if (!isComplete() && ((System.currentTimeMillis() - startTime) < timeout)) {
				try { waitLock.wait( timeout ); }
				catch ( InterruptedException iE ) { }
			}
		}
	}

	protected byte[] data = new byte[0];
	protected boolean complete = false;
	protected boolean acceptShortPacket = true;
	protected int offset = 0;
	protected int length = 0;
	protected int actualLength = 0;
	protected UsbException usbException = null;
	private Object waitLock = new Object();
}
