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
 * Version class that prints the current version numbers.
 * <p>
 * This maintains the version number of the current javax.usb API specification
 * and the supported USB specification version number.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public class Version
{
	/**
	 * Prints out text to stdout (with appropriate version numbers).
	 * <p>
	 * The specific text printed is:
	 * <pre>
	 * javax.usb API version &lt;getApiVersion()>
	 * USB specification version &lt;getUsbVersion()>
	 * </pre>
	 * @param args a String[] of arguments.
	 */
	public static void main( String[] args )
	{
		System.out.println( "javax.usb API version " + getApiVersion() );
		System.out.println( "USB specification version " + getUsbVersion() );
	}

	/**
	 * Get the version number of this API.
	 * <p>
	 * The format of this is &lt;major>.&lt;minor>[.&lt;revision>]
	 * <p>
	 * The revision number is optional; a missing revision
	 * number (i.e., version X.X) indicates the revision number is zero
	 * (i.e., version X.X.0).
	 * @return the version number of this API.
	 */
	public static String getApiVersion() { return VERSION_JAVAX_USB; }

	/**
	 * Get the version number of the USB specification this API implements.
	 * <p>
	 * The formt of this is &lt;major>.&lt;minor>[.&lt;revision>]
	 * <p>
	 * This should correspond with a released USB specification hosted by
	 * <a href="http://www.usb.org">the USB organization website</a>.  The revision
	 * number will only be present if the USB specification contains it.
	 * @return the version number of the implemented USB specification version.
	 */
	public static String getUsbVersion() { return VERSION_USB_SPECIFICATION; }

	//--------------------------------------------------------------------------
	// Class variables
	//

	private static final String VERSION_USB_SPECIFICATION = "1.1";
	private static final String VERSION_JAVAX_USB = "1.0.3-CVS";
}
