package javax.usb;

/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.io.*;
import java.util.*;
import java.security.*;

/**
 * Entry point for javax.usb.
 * @author Dan Streetman
 * @author E. Michael Maximilien
 */
public final class UsbHostManager
{
	private UsbHostManager() { }

	/** 
	 * Get the UsbServices implementation.
	 * @return The UsbServices implementation instance.
	 * @exception UsbException If there is an error creating the UsbSerivces implementation.
	 * @exception SecurityException If the caller does not have security access.
	 */
	public static UsbServices getUsbServices() throws UsbException,SecurityException
	{
		synchronized (servicesLock) {
			if (null == usbServices)
				usbServices = createUsbServices();
		}

		return usbServices;
	}

	/**
	 * Get the Properties loaded from the properties file.
	 * <p>
	 * If the properties have not yet been loaded, this loads them.
	 * @return An copy of the Properties.
	 * @exception UsbException If an error occurs while loading the properties.
	 * @exception SecurityException If the caller does not have security access.
	 */
	public static Properties getProperties() throws UsbException,SecurityException
	{
		synchronized (propertiesLock) {
			if (!propertiesLoaded)
				setupProperties();
		}

		return (Properties)properties.clone();
	}

	/**
	 * Create the UsbServices implementation instance.
	 * <p>
	 * This creates the UsbServices implementation instance based on the
	 * class named in the properties file.
	 * @return The UsbServices implementation instance.
	 * @exception UsbException If the UsbServices class could not be instantiated.
	 * @exception SecurityException If the caller does not have security access.
	 */
	private static UsbServices createUsbServices() throws UsbException,SecurityException
	{
		String className = getProperties().getProperty(JAVAX_USB_USBSERVICES_PROPERTY);

		if (null == className)
			throw new UsbException(USBSERVICES_PROPERTY_NOT_DEFINED());

		try {
			return (UsbServices)Class.forName(className).newInstance();
		} catch ( ClassNotFoundException cnfE ) {
			throw new UsbException(USBSERVICES_CLASSNOTFOUNDEXCEPTION(className)+" : "+cnfE.getMessage());
		} catch ( ExceptionInInitializerError eiiE ) {
			throw new UsbException(USBSERVICES_EXCEPTIONININITIALIZERERROR(className)+" : "+eiiE.getMessage());
		} catch ( InstantiationException iE ) {
			throw new UsbException(USBSERVICES_INSTANTIATIONEXCEPTION(className)+" : "+iE.getMessage());
		} catch ( IllegalAccessException iaE ) {
			throw new UsbException(USBSERVICES_ILLEGALACCESSEXCEPTION(className)+" : "+iaE.getMessage());
		} catch ( ClassCastException ccE ) {
			throw new UsbException(USBSERVICES_CLASSCASTEXCEPTION(className)+" : "+ccE.getMessage());
		}
	}

	/**
	 * Set up the Properties using the properties file.
	 * <p>
	 * This populates the Properties using the key-values listed in the properties file.
	 * @exception UsbException If an error occurs.
	 * @exception SecurityException If the caller does not have security access.
	 */
	private static void setupProperties() throws UsbException,SecurityException
	{
		InputStream i = null;

		// First look in 'java.home'/lib
		String h = System.getProperty("java.home");
		String s = System.getProperty("file.separator");
		if (null != h && null != s)
		{
			try { i = new FileInputStream(h + s + "lib" + s + JAVAX_USB_PROPERTIES_FILE); }
			catch ( FileNotFoundException fnfE ) { /* no 'java.home'/lib properties file, ok */ }
		}

		// Now check the normal CLASSPATH
		if (null == i)
			i = UsbHostManager.class.getClassLoader().getResourceAsStream(JAVAX_USB_PROPERTIES_FILE);

		if (null == i)
			throw new UsbException(PROPERTIES_FILE_NOT_FOUND);

		try {
			properties.load(i);
		} catch ( IOException ioE ) {
			throw new UsbException(PROPERTIES_FILE_IOEXCEPTION_READING + " : " + ioE.getMessage());
		}

		propertiesLoaded = true;

		try {
			i.close();
		} catch ( IOException ioE ) {
//FIXME - handle this better than System.err
			System.err.println(PROPERTIES_FILE_IOEXCEPTION_CLOSING + " : " + ioE.getMessage());
		}
	}

	public static final String JAVAX_USB_PROPERTIES_FILE = "javax.usb.properties";
	public static final String JAVAX_USB_USBSERVICES_PROPERTY = "javax.usb.services";

	private static final String PROPERTIES_FILE_NOT_FOUND = "Properties file " + JAVAX_USB_PROPERTIES_FILE + " not found.";
	private static final String PROPERTIES_FILE_IOEXCEPTION_READING = "IOException while reading properties file " + JAVAX_USB_PROPERTIES_FILE;
	private static final String PROPERTIES_FILE_IOEXCEPTION_CLOSING = "IOException while closing properties file " + JAVAX_USB_PROPERTIES_FILE;

	private static final String USBSERVICES_PROPERTY_NOT_DEFINED()
	{ return "The property " + JAVAX_USB_USBSERVICES_PROPERTY + " is not defined as the implementation class of UsbServices"; }
	private static final String USBSERVICES_CLASSNOTFOUNDEXCEPTION(String c)
	{ return "The UsbServices implementation class "+c+" was not found"; }
	private static final String USBSERVICES_EXCEPTIONININITIALIZERERROR(String c)
	{ return "an Exception occurred during initialization of the UsbServices Class "+c; }
	private static final String USBSERVICES_INSTANTIATIONEXCEPTION(String c)
	{ return "An Exception occurred during instantiation of the UsbServices implementation "+c; }
	private static final String USBSERVICES_ILLEGALACCESSEXCEPTION(String c)
	{ return "An IllegalAccessException occurred while creating the UsbServices implementation "+c; }
	private static final String USBSERVICES_CLASSCASTEXCEPTION(String c)
	{ return "The class "+c+" does not implement UsbServices"; }

	private static boolean propertiesLoaded = false;
	private static Properties properties = new Properties();
	private static Object propertiesLock = new Object();

	private static UsbServices usbServices = null;
	private static Object servicesLock = new Object();
}
