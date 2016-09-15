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
 * USB constants.
 * @author Dan Streetman
 */
public interface UsbConst
{
	//**************************************************************************
	// Hub constants

	public static final byte HUB_CLASSCODE = (byte)0x09;

	//**************************************************************************
	// Device constants

	/** Unknown device speed.  Either the speed could not be detected or the speed was invalid (not USB 1.1 speed). */
	public static final Object DEVICE_SPEED_UNKNOWN = new Object();
	/** Low speed device. */
	public static final Object DEVICE_SPEED_LOW     = new Object();
	/** Full speed device. */
	public static final Object DEVICE_SPEED_FULL    = new Object();

	//**************************************************************************
	// Configuration constants

	public static final byte CONFIGURATION_POWERED_MASK  = (byte)0x60;
	public static final byte CONFIGURATION_SELF_POWERED  = (byte)0x40;
	public static final byte CONFIGURATION_REMOTE_WAKEUP = (byte)0x20;

	//**************************************************************************
	// Endpoint constants

	public static final byte ENDPOINT_NUMBER_MASK                       = (byte)0x0f;

	public static final byte ENDPOINT_DIRECTION_MASK                    = (byte)0x80;
	public static final byte ENDPOINT_DIRECTION_OUT                     = (byte)0x00;
	public static final byte ENDPOINT_DIRECTION_IN                      = (byte)0x80;

	public static final byte ENDPOINT_TYPE_MASK                         = (byte)0x03;
	public static final byte ENDPOINT_TYPE_CONTROL                      = (byte)0x00;
	public static final byte ENDPOINT_TYPE_ISOCHRONOUS                  = (byte)0x01;
	public static final byte ENDPOINT_TYPE_BULK                         = (byte)0x02;
	public static final byte ENDPOINT_TYPE_INTERRUPT                    = (byte)0x03;

	public static final byte ENDPOINT_SYNCHRONIZATION_TYPE_MASK         = (byte)0x0c;
	public static final byte ENDPOINT_SYNCHRONIZATION_TYPE_NONE         = (byte)0x00;
	public static final byte ENDPOINT_SYNCHRONIZATION_TYPE_ASYNCHRONOUS = (byte)0x04;
	public static final byte ENDPOINT_SYNCHRONIZATION_TYPE_ADAPTIVE     = (byte)0x08;
	public static final byte ENDPOINT_SYNCHRONIZATION_TYPE_SYNCHRONOUS  = (byte)0x0c;

	public static final byte ENDPOINT_USAGE_TYPE_MASK                   = (byte)0x30;
	public static final byte ENDPOINT_USAGE_TYPE_DATA                   = (byte)0x00;
	public static final byte ENDPOINT_USAGE_TYPE_FEEDBACK               = (byte)0x10;
	public static final byte ENDPOINT_USAGE_TYPE_IMPLICIT_FEEDBACK_DATA = (byte)0x20;
	public static final byte ENDPOINT_USAGE_TYPE_RESERVED               = (byte)0x30;

	//**************************************************************************
	// Request constants

	public static final byte REQUESTTYPE_DIRECTION_MASK      = (byte)0x80;
	public static final byte REQUESTTYPE_DIRECTION_IN        = (byte)0x80;
	public static final byte REQUESTTYPE_DIRECTION_OUT       = (byte)0x00;

	public static final byte REQUESTTYPE_TYPE_MASK           = (byte)0x60;
	public static final byte REQUESTTYPE_TYPE_STANDARD       = (byte)0x00;
	public static final byte REQUESTTYPE_TYPE_CLASS          = (byte)0x20;
	public static final byte REQUESTTYPE_TYPE_VENDOR         = (byte)0x40;
	public static final byte REQUESTTYPE_TYPE_RESERVED       = (byte)0x60;

	public static final byte REQUESTTYPE_RECIPIENT_MASK      = (byte)0x1f;
	public static final byte REQUESTTYPE_RECIPIENT_DEVICE    = (byte)0x00;
	public static final byte REQUESTTYPE_RECIPIENT_INTERFACE = (byte)0x01;
	public static final byte REQUESTTYPE_RECIPIENT_ENDPOINT  = (byte)0x02;
	public static final byte REQUESTTYPE_RECIPIENT_OTHER     = (byte)0x03;

	public static final byte REQUEST_GET_STATUS              = (byte)0x00;
	public static final byte REQUEST_CLEAR_FEATURE           = (byte)0x01;
	public static final byte REQUEST_SET_FEATURE             = (byte)0x03;
	public static final byte REQUEST_SET_ADDRESS             = (byte)0x05;
	public static final byte REQUEST_GET_DESCRIPTOR          = (byte)0x06;
	public static final byte REQUEST_SET_DESCRIPTOR          = (byte)0x07;
	public static final byte REQUEST_GET_CONFIGURATION       = (byte)0x08;
	public static final byte REQUEST_SET_CONFIGURATION       = (byte)0x09;
	public static final byte REQUEST_GET_INTERFACE           = (byte)0x0a;
	public static final byte REQUEST_SET_INTERFACE           = (byte)0x0b;
	public static final byte REQUEST_SYNCH_FRAME             = (byte)0x0c;

	//**************************************************************************
	// Feature selectors

	public static final byte FEATURE_SELECTOR_DEVICE_REMOTE_WAKEUP = (byte)0x01;
	public static final byte FEATURE_SELECTOR_ENDPOINT_HALT        = (byte)0x00;

	//**************************************************************************
	// Descriptor constants

	public static final byte DESCRIPTOR_TYPE_DEVICE              = (byte)0x01;
	public static final byte DESCRIPTOR_TYPE_CONFIGURATION       = (byte)0x02;
	public static final byte DESCRIPTOR_TYPE_STRING              = (byte)0x03;
	public static final byte DESCRIPTOR_TYPE_INTERFACE           = (byte)0x04;
	public static final byte DESCRIPTOR_TYPE_ENDPOINT            = (byte)0x05;

	public static final byte DESCRIPTOR_MIN_LENGTH               = (byte)0x02;
	public static final byte DESCRIPTOR_MIN_LENGTH_DEVICE        = (byte)0x12;
	public static final byte DESCRIPTOR_MIN_LENGTH_CONFIGURATION = (byte)0x09;
	public static final byte DESCRIPTOR_MIN_LENGTH_INTERFACE     = (byte)0x09;
	public static final byte DESCRIPTOR_MIN_LENGTH_ENDPOINT      = (byte)0x07;
	public static final byte DESCRIPTOR_MIN_LENGTH_STRING        = (byte)0x02;

}
