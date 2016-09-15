package javax.usb;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

/**
 * Exception indicating a platform-specific UsbException.
 * <p>
 * This indicates an error occurred that is specific to the operating system or platform.
 * This provides access to the specific {@link #getErrorCode() error code} and/or
 * {@link #getPlatformException() platform Exception}.
 * @author Dan Streetman
 */
public class UsbPlatformException extends UsbException
{
	/**
	 * Constructor.
	 */
	public UsbPlatformException() { super(); }

	/**
	 * Constructor.
	 * @param s The detail message.
	 */
	public UsbPlatformException(String s) { super(s); }

	/**
	 * Constructor.
	 * @param e The error code.
	 */
	public UsbPlatformException(int e)
	{
		super();
		errorCode = e;
	}

	/**
	 * Constructor.
	 * @param pE The platform Exception.
	 */
	public UsbPlatformException(Exception pE)
	{
		super();
		platformException = pE;
	}

	/**
	 * Constructor.
	 * @param s The detail message.
	 * @param e The error code.
	 */
	public UsbPlatformException(String s, int e)
	{
		super(s);
		errorCode = e;
	}

	/**
	 * Constructor.
	 * @param s The detail message.
	 * @param pE The platform Exception.
	 */
	public UsbPlatformException(String s, Exception pE)
	{
		super(s);
		platformException = pE;
	}

	/**
	 * Constructor.
	 * @param e The error code.
	 * @param pE The platform Exception.
	 */
	public UsbPlatformException(int e, Exception pE)
	{
		super();
		errorCode = e;
		platformException = pE;
	}

	/**
	 * Constructor.
	 * @param s The detail message.
	 * @param e The error code.
	 * @param pE The platform Exception.
	 */
	public UsbPlatformException(String s, int e, Exception pE)
	{
		super(s);
		errorCode = e;
		platformException = pE;
	}

	/**
	 * Get the platform Exception.
	 * @return The platform Exception, or null.
	 */
	public Exception getPlatformException() { return platformException; }

	/**
	 * Get the platform error code.
	 * @return The platform error code.
	 */
	public int getErrorCode() { return errorCode; }

	protected Exception platformException = null;
	protected int errorCode = 0;
}
