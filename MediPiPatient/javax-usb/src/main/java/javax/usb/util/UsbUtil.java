package javax.usb.util;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation. All
 * Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions of the
 * Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbIrp;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.UsbPort;
import javax.usb.UsbServices;
import javax.usb.UsbStringDescriptor;
import javax.usb.event.UsbDeviceListener;
import javax.usb.event.UsbPipeListener;

/**
 * General utility methods.
 * @author Dan Streetman
 */
public class UsbUtil {
	public static final String PREFIX = "  ";
	/**
	 * Get the specified byte's value as an unsigned short.
	 * <p>
	 * This converts the specified byte into a short. The least significant byte
	 * (8 bits) of the short will be identical to the byte (8 bits) provided,
	 * and the most significant byte (8 bits) of the short will be zero.
	 * <p>
	 * For many of the values in this USB API, unsigned bytes are used. However,
	 * since Java does not include unsigned bytes in the language, those
	 * unsigned bytes must be converted to a larger storage type before being
	 * used in unsigned calculations.
	 * @param b the byte to convert.
	 * @return An unsigned short representing the specified byte.
	 */
	public static short unsignedShort(byte b) {
		return (short) (0x00ff & b);
	}

	/**
	 * Get the specified byte's value as an unsigned integer.
	 * <p>
	 * This converts the specified byte into an integer. The least significant
	 * byte (8 bits) of the integer will be identical to the byte (8 bits)
	 * provided, and the most significant 3 bytes (24 bits) of the integer will
	 * be zero.
	 * <p>
	 * For many of the values in this USB API, unsigned bytes are used. However,
	 * since Java does not include unsigned bytes in the language, those
	 * unsigned bytes must be converted to a larger storage type before being
	 * used in unsigned calculations.
	 * @param b the byte to convert.
	 * @return An unsigned int representing the specified byte.
	 */
	public static int unsignedInt(byte b) {
		return 0x000000ff & b;
	}

	/**
	 * Get the specified short's value as an unsigned integer.
	 * <p>
	 * This converts the specified byte into an integer. The least significant
	 * short (16 bits) of the integer will be identical to the short (16 bits)
	 * provided, and the most significant 2 bytes (16 bits) of the integer will
	 * be zero.
	 * <p>
	 * For many of the values in this USB API, unsigned shorts are used.
	 * However, since Java does not include unsigned short in the language,
	 * those unsigned shorts must be converted to a larger storage type before
	 * being used in unsigned calculations.
	 * @param s the short to convert.
	 * @return An unsigned int representing the specified short.
	 */
	public static int unsignedInt(short s) {
		return 0x0000ffff & s;
	}

	/**
	 * Get the specified byte's value as an unsigned long.
	 * <p>
	 * This converts the specified byte into a long. The least significant byte
	 * (8 bits) of the long will be identical to the byte (8 bits) provided, and
	 * the most significant 7 bytes (56 bits) of the long will be zero.
	 * <p>
	 * For many of the values in this USB API, unsigned bytes are used. However,
	 * since Java does not include unsigned bytes in the language, those
	 * unsigned bytes must be converted to a larger storage type before being
	 * used in unsigned calculations.
	 * @param b the byte to convert.
	 * @return An unsigned long representing the specified byte.
	 */
	public static long unsignedLong(byte b) {
		return 0x00000000000000ff & b;
	}

	/**
	 * Get the specified short's value as an unsigned long.
	 * <p>
	 * This converts the specified byte into a long. The least significant short
	 * (16 bits) of the long will be identical to the short (16 bits) provided,
	 * and the most significant 6 bytes (48 bits) of the long will be zero.
	 * <p>
	 * For many of the values in this USB API, unsigned shorts are used.
	 * However, since Java does not include unsigned short in the language,
	 * those unsigned shorts must be converted to a larger storage type before
	 * being used in unsigned calculations.
	 * @param s the short to convert.
	 * @return An unsigned long representing the specified short.
	 */
	public static long unsignedLong(short s) {
		return 0x000000000000ffff & s;
	}

	/**
	 * Get the specified int's value as an unsigned long.
	 * <p>
	 * This converts the specified int into a long. The least significant int
	 * (32 bits) of the long will be identical to the int (32 bits) provided,
	 * and the most significant int (32 bits) of the long will be zero.
	 * @param i the int to convert.
	 * @return An unsigned long representing the specified int.
	 */
	public static long unsignedLong(int i) {
		return 0x00000000ffffffff & i;
	}

	/**
	 * Convert 2 bytes into a short.
	 * <p>
	 * This converts the 2 bytes into a short. The msb will be the high byte (8
	 * bits) of the short, and the lsb will be the low byte (8 bits) of the
	 * short.
	 * @param msb The Most Significant Byte.
	 * @param lsb The Least Significant Byte.
	 * @return A short representing the bytes.
	 */
	public static short toShort(byte msb, byte lsb) {
		return (short) ((0xff00 & (short) (msb << 8)) | (0x00ff & (short) lsb));
	}

	/**
	 * Convert 4 bytes into an int.
	 * <p>
	 * This converts the 4 bytes into an int.
	 * @param byte3 The byte to be left-shifted 24 bits.
	 * @param byte2 The byte to be left-shifted 16 bits.
	 * @param byte1 The byte to be left-shifted 8 bits.
	 * @param byte0 The byte that will not be left-shifted.
	 * @return An int representing the bytes.
	 */
	public static int toInt(byte byte3, byte byte2, byte byte1, byte byte0) {
		return toInt(toShort(byte3, byte2), toShort(byte1, byte0));
	}

	/**
	 * Convert 8 bytes into a long.
	 * <p>
	 * This converts the 8 bytes into a long.
	 * @param byte7 The byte to be left-shifted 56 bits.
	 * @param byte6 The byte to be left-shifted 48 bits.
	 * @param byte5 The byte to be left-shifted 40 bits.
	 * @param byte4 The byte to be left-shifted 32 bits.
	 * @param byte3 The byte to be left-shifted 24 bits.
	 * @param byte2 The byte to be left-shifted 16 bits.
	 * @param byte1 The byte to be left-shifted 8 bits.
	 * @param byte0 The byte that will not be left-shifted.
	 * @return A long representing the bytes.
	 */
	public static long toLong(byte byte7, byte byte6, byte byte5, byte byte4, byte byte3, byte byte2, byte byte1, byte byte0) {
		return toLong(toInt(byte7, byte6, byte5, byte4), toInt(byte3, byte2, byte1, byte0));
	}

	/**
	 * Convert 2 shorts into an int.
	 * <p>
	 * This converts the 2 shorts into an int.
	 * @param mss The Most Significant Short.
	 * @param lss The Least Significant Short.
	 * @return An int representing the shorts.
	 */
	public static int toInt(short mss, short lss) {
		return((0xffff0000 & (int) (mss << 16)) | (0x0000ffff & (int) lss));
	}

	/**
	 * Convert 4 shorts into a long.
	 * <p>
	 * This converts the 4 shorts into a long.
	 * @param short3 The short to be left-shifted 48 bits.
	 * @param short2 The short to be left-shifted 32 bits.
	 * @param short1 The short to be left-shifted 16 bits.
	 * @param short0 The short that will not be left-shifted.
	 * @return A long representing the shorts.
	 */
	public static long toLong(short short3, short short2, short short1, short short0) {
		return toLong(toInt(short3, short2), toInt(short1, short0));
	}

	/**
	 * Convert 2 ints into a long.
	 * <p>
	 * This converts the 2 ints into a long.
	 * @param msi The Most Significant Int.
	 * @param lsi The Least Significant Int.
	 * @return A long representing the ints.
	 */
	public static long toLong(int msi, int lsi) {
		/*
		 * We can't represent a mask for the MSI, but that's ok, we don't really
		 * need one; left-shifting sets the low bits to 0.
		 */
		return (long) ((long) ((long) msi << 32) | (long) ((long) 0x00000000ffffffff & (long) lsi));
	}

	/**
	 * Format a byte into a proper length hex String.
	 * <p>
	 * This is identical to Long.toHexString() except this pads (with 0's) to
	 * the proper size.
	 * @param b the byte to convert
	 */
	public static String toHexString(byte b) {
		return toHexString(unsignedLong(b), '0', 2, 2);
	}

	/**
	 * Format a short into a proper length hex String.
	 * <p>
	 * This is identical to Long.toHexString() except this pads (with 0's) to
	 * the proper size.
	 * @param s the short to convert
	 */
	public static String toHexString(short s) {
		return toHexString(unsignedLong(s), '0', 4, 4);
	}

	/**
	 * Format a int into a proper length hex String.
	 * <p>
	 * This is identical to Long.toHexString() except this pads (with 0's) to
	 * the proper size.
	 * @param i the integer to convert
	 */
	public static String toHexString(int i) {
		return toHexString(unsignedLong(i), '0', 8, 8);
	}

	/**
	 * Format a long into the specified length hex String.
	 * <p>
	 * This is identical to Long.toHexString() except this pads (with 0's) to
	 * the proper size.
	 * @param l the long to convert
	 */
	public static String toHexString(long l) {
		return toHexString(l, '0', 16, 16);
	}

	/**
	 * Format a long into the specified length hex String.
	 * <p>
	 * This is identical to Long.toHexString() except this pads (with 0's), or
	 * truncates, to the specified size. If max < min the functionaliy is
	 * exactly as Long.toHexString().
	 * @param l the long to convert
	 * @param c the character to use for padding
	 * @param min the min length of the resulting String
	 * @param max the max length of the resulting String
	 */
	public static String toHexString(long l, char c, int min, int max) {
		StringBuffer sb = new StringBuffer(Long.toHexString(l));

		if(max < min)
			return sb.toString();

		while(sb.length() < max)
			sb.insert(0, c);

		return sb.substring(sb.length() - min);
	}

	/**
	 * Format a byte[] into a hex String.
	 * <p>
	 * This creates a String by concatenating the result of
	 * <code>delimiter + {@link #toHexString(byte) toHexString(byte)}</code> for
	 * each byte in the array. If the specified length is greater than the
	 * actual array length, the array length is used. If the specified length
	 * (or array length) is 0 or less, the resulting String will be an empty
	 * String.
	 * @param delimiter The delimiter to prefix every byte with.
	 * @param array The byte[] to convert.
	 * @param length The number of bytes to use.
	 * @return A String representing the byte[].
	 * @exception NullPointerException If the byte[] is null.
	 */
	public static String toHexString(String delimiter, byte[] array, int length) {
		StringBuffer sB = new StringBuffer();

		if(length > array.length)
			length = array.length;

		if(length < 0)
			length = 0;

		for(int i = 0; i < length; i++)
			sB.append(delimiter + toHexString(array[i]));

		return sB.toString();
	}

	/**
	 * Format a short[] into a hex String.
	 * <p>
	 * This creates a String by concatenating the result of
	 * <code>delimiter + {@link #toHexString(short) toHexString(short)}</code>
	 * for each short in the array. If the specified length is greater than the
	 * actual array length, the array length is used. If the specified length
	 * (or array length) is 0 or less, the resulting String will be an empty
	 * String.
	 * @param delimiter The delimiter to prefix every short with.
	 * @param array The short[] to convert.
	 * @param length The number of shorts to use.
	 * @return A String representing the short[].
	 * @exception NullPointerException If the short[] is null.
	 */
	public static String toHexString(String delimiter, short[] array, int length) {
		StringBuffer sB = new StringBuffer();

		if(length > array.length)
			length = array.length;

		if(length < 0)
			length = 0;

		for(int i = 0; i < length; i++)
			sB.append(delimiter + toHexString(array[i]));

		return sB.toString();
	}

	/**
	 * Format a int[] into a hex String.
	 * <p>
	 * This creates a String by concatenating the result of
	 * <code>delimiter + {@link #toHexString(int) toHexString(int)}</code> for
	 * each int in the array. If the specified length is greater than the actual
	 * array length, the array length is used. If the specified length (or array
	 * length) is 0 or less, the resulting String will be an empty String.
	 * @param delimiter The delimiter to prefix every int with.
	 * @param array The int[] to convert.
	 * @param length The number of ints to use.
	 * @return A String representing the int[].
	 * @exception NullPointerException If the int[] is null.
	 */
	public static String toHexString(String delimiter, int[] array, int length) {
		StringBuffer sB = new StringBuffer();

		if(length > array.length)
			length = array.length;

		if(length < 0)
			length = 0;

		for(int i = 0; i < length; i++)
			sB.append(delimiter + toHexString(array[i]));

		return sB.toString();
	}

	/**
	 * Format a long[] into a hex String.
	 * <p>
	 * This creates a String by concatenating the result of
	 * <code>delimiter + {@link #toHexString(long) toHexString(long)}</code> for
	 * each long in the array. If the specified length is greater than the
	 * actual array length, the array length is used. If the specified length
	 * (or array length) is 0 or less, the resulting String will be an empty
	 * String.
	 * @param delimiter The delimiter to prefix every long with.
	 * @param array The long[] to convert.
	 * @param length The number of longs to use.
	 * @return A String representing the long[].
	 * @exception NullPointerException If the long[] is null.
	 */
	public static String toHexString(String delimiter, long[] array, int length) {
		StringBuffer sB = new StringBuffer();

		if(length > array.length)
			length = array.length;

		if(length < 0)
			length = 0;

		for(int i = 0; i < length; i++)
			sB.append(delimiter + toHexString(array[i]));

		return sB.toString();
	}

	/**
	 * Format a byte[] into a hex String.
	 * <p>
	 * This calls {@link #toHexString(String,byte[],int) toHexString(delimiter,
	 * array, array.length)}.
	 * @param delimiter The delimiter to prefix every byte with.
	 * @param array The byte[] to convert.
	 * @return A String representing the byte[].
	 * @exception NullPointerException If the byte[] is null.
	 */
	public static String toHexString(String delimiter, byte[] array) {
		return toHexString(delimiter, array, array.length);
	}

	/**
	 * Format a short[] into a hex String.
	 * <p>
	 * This calls {@link #toHexString(String,short[],int) toHexString(delimiter,
	 * array, array.length)}.
	 * @param delimiter The delimiter to prefix every short with.
	 * @param array The short[] to convert.
	 * @return A String representing the short[].
	 * @exception NullPointerException If the short[] is null.
	 */
	public static String toHexString(String delimiter, short[] array) {
		return toHexString(delimiter, array, array.length);
	}

	/**
	 * Format a int[] into a hex String.
	 * <p>
	 * This calls {@link #toHexString(String,int[],int) toHexString(delimiter,
	 * array, array.length)}.
	 * @param delimiter The delimiter to prefix every int with.
	 * @param array The int[] to convert.
	 * @return A String representing the int[].
	 * @exception NullPointerException If the int[] is null.
	 */
	public static String toHexString(String delimiter, int[] array) {
		return toHexString(delimiter, array, array.length);
	}

	/**
	 * Format a long[] into a hex String.
	 * <p>
	 * This calls {@link #toHexString(String,long[],int) toHexString(delimiter,
	 * array, array.length)}.
	 * @param delimiter The delimiter to prefix every long with.
	 * @param array The long[] to convert.
	 * @return A String representing the long[].
	 * @exception NullPointerException If the long[] is null.
	 */
	public static String toHexString(String delimiter, long[] array) {
		return toHexString(delimiter, array, array.length);
	}

	/**
	 * Get a String description of the specified device-speed Object.
	 * <p>
	 * The String will be one of:
	 * <ul>
	 * <li>Low</li>
	 * <li>Full</li>
	 * <li>Unknown</li>
	 * <li>null</li>
	 * <li>Invalid</li>
	 * </ul>
	 * The string "null" is used for a null Object. The string "Invalid" is used
	 * for an Object that does not correspond to any of those defined in
	 * {@link javax.usb.UsbConst UsbConst}.
	 * @param object The device-speed Object.
	 * @return A String representing the speed Object.
	 * @see UsbConst#DEVICE_SPEED_LOW Low Speed.
	 * @see UsbConst#DEVICE_SPEED_FULL Full Speed.
	 * @see UsbConst#DEVICE_SPEED_UNKNOWN Unknown Speed.
	 */
	public static String getSpeedString(Object object) {
		if(UsbConst.DEVICE_SPEED_LOW == object)
			return "Low";
		if(UsbConst.DEVICE_SPEED_FULL == object)
			return "Full";
		if(UsbConst.DEVICE_SPEED_UNKNOWN == object)
			return "Unknown";
		if(null == object)
			return "null";

		return "Invalid";
	}

	/**
	 * Create a synchronized UsbDevice.
	 * @param usbDevice The unsynchronized UsbDevice.
	 * @return A synchronized UsbDevice.
	 */
	public static UsbDevice synchronizedUsbDevice(UsbDevice usbDevice) {
		return new UsbUtil.SynchronizedUsbDevice(usbDevice);
	}

	/**
	 * Create a synchronized UsbPipe.
	 * @param usbPipe The unsynchronized UsbPipe.
	 * @return A synchronized UsbPipe.
	 */
	public static UsbPipe synchronizedUsbPipe(UsbPipe usbPipe) {
		return new UsbUtil.SynchronizedUsbPipe(usbPipe);
	}

	/**
	 * Get the virtual root UsbHub.
	 * @return The virtual root UsbHub.
	 */
	public static UsbHub getVirtualRootUsbHub()
	{
		UsbServices services = null;
		UsbHub virtualRootUsbHub = null;

		/* First we need to get the UsbServices.
		 * This might throw either an UsbException or SecurityException.
		 * A SecurityException means we're not allowed to access the USB bus,
		 * while a UsbException indicates there is a problem either in
		 * the javax.usb implementation or the OS USB support.
		 */
		try {
			services = UsbHostManager.getUsbServices();
		} catch ( UsbException uE ) {
			throw new RuntimeException("Error : " + uE.getMessage());
		} catch ( SecurityException sE ) {
			throw new RuntimeException("Error : " + sE.getMessage());
		}

		/* Now we need to get the virtual root UsbHub,
		 * everything is connected to it.  The Virtual Root UsbHub
		 * doesn't actually correspond to any physical device, it's
		 * strictly virtual.  Each of the devices connected to one of its
		 * ports corresponds to a physical host controller located in
		 * the system.  Those host controllers are (usually) located inside
		 * the computer, e.g. as a PCI board, or a chip on the mainboard,
		 * or a PCMCIA card.  The virtual root UsbHub aggregates all these
		 * host controllers.
		 *
		 * This also may throw an UsbException or SecurityException.
		 */
		try {
			virtualRootUsbHub = services.getRootUsbHub();
		} catch ( UsbException uE ) {
			throw new RuntimeException("Error : " + uE.getMessage());
		} catch ( SecurityException sE ) {
			throw new RuntimeException("Error : " + sE.getMessage());
		}

		return virtualRootUsbHub;
	}

	/**
	 * Process all devices in the system using getAttachedUsbDevices().
	 * Note that this accepts a UsbDevice, not a UsbHub, since UsbHubs are UsbDevices.
	 */
	public static void processUsingGetAttachedUsbDevices(UsbDevice usbDevice, String prefix)
	{
		UsbHub usbHub = null;

		/* If this is not a UsbHub, just display device and return. */
		if (!usbDevice.isUsbHub()) {
			System.out.println(prefix + "Device");
			return;
		} else {
			/* We know it's a hub, so cast it. */
			usbHub = (UsbHub)usbDevice;
		}

		if (usbHub.isRootUsbHub()) {
			/* This is the virtual root UsbHub. */
			System.out.println(prefix + "Virtual root UsbHub");
		} else {
			/* This is not the virtual root UsbHub. */
			System.out.println(prefix + "UsbHub");
		}

		/* Now let's process each of this hub's devices. */
		List attachedUsbDevices = usbHub.getAttachedUsbDevices();

		for (int i=0; i<attachedUsbDevices.size(); i++) {
			/* We know all objects in the list are UsbDevice objects; casting is safe. */
			UsbDevice device = (UsbDevice)attachedUsbDevices.get(i);

			/* Recursively handle this device. */
			processUsingGetAttachedUsbDevices(device, prefix+PREFIX);
		}
	}

	/**
	 * Process all devices in the system using getUsbPorts().
	 * Notice that this looks the same as using getAttachedUsbDevices()
	 * except this also displays UsbPorts that do not have a connected device.
	 */
	public static void processUsingGetUsbPorts(UsbDevice usbDevice, String prefix)
	{
		UsbHub usbHub = null;

		/* If this is not a UsbHub, just display device and return. */
		if (!usbDevice.isUsbHub()) {
			System.out.println(prefix + "Device");
			return;
		} else {
			/* We know it's a hub, so cast it. */
			usbHub = (UsbHub)usbDevice;
		}

		if (usbHub.isRootUsbHub()) {
			/* This is the virtual root UsbHub. */
			System.out.println(prefix + "Virtual root UsbHub");
		} else {
			/* This is not the virtual root UsbHub. */
			System.out.println(prefix + "UsbHub");
		}

		/* Now let's process each of this hub's ports. */
		List usbPorts = usbHub.getUsbPorts();

		for (int i=0; i<usbPorts.size(); i++) {
			/* We know all objects in the list are UsbPort objects; casting is safe. */
			UsbPort port = (UsbPort)usbPorts.get(i);

			/* If this doesn't have a device attached, just process the port. */
			if (!port.isUsbDeviceAttached()) {
				System.out.println(prefix+PREFIX + "UsbPort");
				continue;
			} else {
				/* There is a device attached, so we'll process it. */
				processUsingGetUsbPorts(port.getUsbDevice(), prefix+PREFIX);
			}
		}
	}

	/**
	 * A synchronized UsbDevice wrapper implementation.
	 */
	public static class SynchronizedUsbDevice implements UsbDevice {
		public SynchronizedUsbDevice(UsbDevice usbDevice) {
			this.usbDevice = usbDevice;
		}

		public UsbPort getParentUsbPort() {
			return usbDevice.getParentUsbPort();
		}

		public boolean isUsbHub() {
			return usbDevice.isUsbHub();
		}

		public String getManufacturerString() throws UsbException, UnsupportedEncodingException {
			synchronized(submitLock) {
				return usbDevice.getManufacturerString();
			}
		}

		public String getSerialNumberString() throws UsbException, UnsupportedEncodingException {
			synchronized(submitLock) {
				return usbDevice.getSerialNumberString();
			}
		}

		public String getProductString() throws UsbException, UnsupportedEncodingException {
			synchronized(submitLock) {
				return usbDevice.getProductString();
			}
		}

		public Object getSpeed() {
			return usbDevice.getSpeed();
		}

		public List getUsbConfigurations() {
			return usbDevice.getUsbConfigurations();
		}

		public UsbConfiguration getUsbConfiguration(byte number) {
			return usbDevice.getUsbConfiguration(number);
		}

		public boolean containsUsbConfiguration(byte number) {
			return usbDevice.containsUsbConfiguration(number);
		}

		public byte getActiveUsbConfigurationNumber() {
			return usbDevice.getActiveUsbConfigurationNumber();
		}

		public UsbConfiguration getActiveUsbConfiguration() {
			return usbDevice.getActiveUsbConfiguration();
		}

		public boolean isConfigured() {
			return usbDevice.isConfigured();
		}

		public UsbDeviceDescriptor getUsbDeviceDescriptor() {
			return usbDevice.getUsbDeviceDescriptor();
		}

		public UsbStringDescriptor getUsbStringDescriptor(byte index) throws UsbException {
			synchronized(submitLock) {
				return usbDevice.getUsbStringDescriptor(index);
			}
		}

		public String getString(byte index) throws UsbException, UnsupportedEncodingException {
			synchronized(submitLock) {
				return usbDevice.getString(index);
			}
		}

		public void syncSubmit(UsbControlIrp irp) throws UsbException {
			synchronized(submitLock) {
				usbDevice.syncSubmit(irp);
			}
		}

		public void asyncSubmit(UsbControlIrp irp) throws UsbException {
			synchronized(submitLock) {
				usbDevice.asyncSubmit(irp);
			}
		}

		public void syncSubmit(List list) throws UsbException {
			synchronized(submitLock) {
				usbDevice.syncSubmit(list);
			}
		}

		public void asyncSubmit(List list) throws UsbException {
			synchronized(submitLock) {
				usbDevice.asyncSubmit(list);
			}
		}

		public UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex) {
			return usbDevice.createUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
		}

		public void addUsbDeviceListener(UsbDeviceListener listener) {
			synchronized(listenerLock) {
				usbDevice.addUsbDeviceListener(listener);
			}
		}

		public void removeUsbDeviceListener(UsbDeviceListener listener) {
			synchronized(listenerLock) {
				usbDevice.removeUsbDeviceListener(listener);
			}
		}

		public UsbDevice usbDevice = null;
		protected Object submitLock = new Object();
		protected Object listenerLock = new Object();
	}

	/**
	 * A synchronized UsbPipe wrapper implementation.
	 * <p>
	 * Not all methods are synchronized; the open/close methods are synchronized
	 * to each other, and the submission and abort methods are synchronized to
	 * each other.
	 */
	public static class SynchronizedUsbPipe implements UsbPipe {
		public SynchronizedUsbPipe(UsbPipe usbPipe) {
			this.usbPipe = usbPipe;
		}

		public void open() throws UsbException, UsbNotActiveException, UsbNotClaimedException {
			synchronized(openLock) {
				usbPipe.open();
			}
		}

		public void close() throws UsbException, UsbNotOpenException {
			synchronized(openLock) {
				usbPipe.close();
			}
		}

		public boolean isActive() {
			return usbPipe.isActive();
		}

		public boolean isOpen() {
			return usbPipe.isOpen();
		}

		public UsbEndpoint getUsbEndpoint() {
			return usbPipe.getUsbEndpoint();
		}

		public int syncSubmit(byte[] data) throws UsbException, UsbNotOpenException {
			synchronized(submitLock) {
				return usbPipe.syncSubmit(data);
			}
		}

		public UsbIrp asyncSubmit(byte[] data) throws UsbException, UsbNotOpenException {
			synchronized(submitLock) {
				return usbPipe.asyncSubmit(data);
			}
		}

		public void syncSubmit(UsbIrp irp) throws UsbException, UsbNotOpenException {
			synchronized(submitLock) {
				usbPipe.syncSubmit(irp);
			}
		}

		public void asyncSubmit(UsbIrp irp) throws UsbException, UsbNotOpenException {
			synchronized(submitLock) {
				usbPipe.asyncSubmit(irp);
			}
		}

		public void syncSubmit(List list) throws UsbException, UsbNotOpenException {
			synchronized(submitLock) {
				usbPipe.syncSubmit(list);
			}
		}

		public void asyncSubmit(List list) throws UsbException, UsbNotOpenException {
			synchronized(submitLock) {
				usbPipe.asyncSubmit(list);
			}
		}

		public void abortAllSubmissions() throws UsbNotOpenException {
			synchronized(submitLock) {
				usbPipe.abortAllSubmissions();
			}
		}

		public UsbIrp createUsbIrp() {
			return usbPipe.createUsbIrp();
		}

		public UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex) {
			return usbPipe.createUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
		}

		public void addUsbPipeListener(UsbPipeListener listener) {
			usbPipe.addUsbPipeListener(listener);
		}

		public void removeUsbPipeListener(UsbPipeListener listener) {
			usbPipe.removeUsbPipeListener(listener);
		}

		public UsbPipe usbPipe = null;
		protected Object openLock = new Object();
		protected Object submitLock = new Object();
	}

}
