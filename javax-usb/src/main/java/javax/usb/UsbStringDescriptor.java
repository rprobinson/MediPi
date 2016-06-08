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

/**
 * Interface for a USB string descriptor.
 * @author Dan Streetman
 */
public interface UsbStringDescriptor extends UsbDescriptor
{
	/**
	 * Get this descriptor's bString.
	 * <p>
	 * Modifications to the returned byte[] will not affect the StringDescriptor's bString
	 * (i.e. a copy of the bString is returned).
	 * @return This descriptor's bString.
	 */
	public byte[] bString();

	/**
	 * Get this descriptor's translated String.
	 * <p>
	 * This is the String translation of the {@link #bString() bString}.
	 * The translation is done using the best available Unicode encoding that this
	 * JVM provides.  USB strings are 16-bit little-endian; if no 16-bit little-endian
	 * encoding is available, and the string can be converted to 8-bit (all high bytes are zero),
	 * then 8-bit encoding is used.  If no encoding is available,
	 * an UnsupportedEncodingException is thrown.
	 * <p>
	 * For information about Unicode see
	 * <a href="http://www.unicode.org/">the Unicode website</a>.
	 * @return This descriptor's String.
	 * @exception UnsupportedEncodingException If no encoding is available.
	 */
    public String getString() throws UnsupportedEncodingException;
}
