/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.medipi.devices.drivers.service;

import java.util.List;

import javax.usb.UsbClaimException;
import javax.usb.UsbConfiguration;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbPipe;
import javax.usb.util.UsbUtil;

import org.medipi.devices.exceptions.DeviceConnectionException;

/**
 * This is an abstract class which provides functionality related to USB serial interfacing.
 * This class has some implementations which are common when communicating with serial USB
 * devices.
 */
public abstract class USBService {

	/** The Constant PADDING_BYTE_0xF4. */
	public static final byte PADDING_BYTE_0xF4 = (byte) 0xF4;

	/** The Constant PADDING_BYTE_0x00. */
	public static final byte PADDING_BYTE_0x00 = (byte) 0x00;

	/** The Constant DEFAULT_BYTE_ARRAY_LENGTH_8. */
	public static final int DEFAULT_BYTE_ARRAY_LENGTH_8 = 8;

	/** The Constant BYTE_ARRAY_LENGTH_128. */
	public static final int BYTE_ARRAY_LENGTH_128 = 128;

	/**
	 * Initialise the device to start the serial communication.
	 *
	 * @param device the device object with which the communication to be instantiated
	 * @param usbControl to write the data to serial interface
	 * @param connectionPipe to read the data from serial interface
	 * @throws UsbException the USB exception
	 */
	public abstract void initialiseDevice(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException;

	/**
	 * Gets the number of readings the device has stored.
	 *
	 * @param device the device object with which the communication to be instantiated
	 * @param usbControl to write the data to serial interface
	 * @param connectionPipe to read the data from serial interface
	 * @return the number of readings in the device
	 * @throws UsbException the USB exception
	 */
	public abstract int getNumberOfReadings(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException;

	/**
	 * Read data from the serial interface.
	 *
	 * @param connectionPipe the USB connection object which will be used to read the data from the serial interface
	 * @param numberOfBytes the number of bytes to be read from the serial interface
	 * @return the byte array read from the serial interface
	 * @throws UsbException the USB exception
	 */
	public abstract byte[] readData(final UsbPipe connectionPipe, final int numberOfBytes) throws UsbException;

	/**
	 * Terminate device communication.
	 *
	 * @param device the device object with which the communication to be instantiated
	 * @param usbControl to write the data to serial interface
	 * @throws UsbException the USB exception
	 * @throws InterruptedException the interrupted exception
	 */
	public abstract void terminateDeviceCommunication(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException, InterruptedException;

	/**
	 * Creates the USB control object which will be used to write the data to the serial interface.
	 *
	 * @param device the device object with which the communication to be instantiated
	 * @param requestType the request type
	 * @param request the request
	 * @param value the value
	 * @param index the index
	 * @return the USB control object
	 */
	public UsbControlIrp getUSBControl(final UsbDevice device, final byte requestType, final byte request, final short value, final short index) {
		return device.createUsbControlIrp(requestType, request, value, index);
	}

	/**
	 * Creates the USB connection object which will be used to read the data from the serial interface.
	 *
	 * @param device the device object with which the communication to be instantiated
	 * @param interfaceNumber the interface number where the data should be submitted
	 * @param endpointNumber the endpoint number of the interface where the data should be submitted
	 * @return the USB connection pipe
	 * @throws UsbClaimException the USB claim exception
	 * @throws UsbException the USB exception
	 */
	public UsbPipe getUSBConnection(final UsbDevice device, final int interfaceNumber, final int endpointNumber) throws UsbClaimException, UsbException {
		//Claim the interface
		final UsbConfiguration configuration = device.getActiveUsbConfiguration();
		final UsbInterface iface = configuration.getUsbInterface((byte) interfaceNumber);
		iface.claim(usbInterface -> true);

		//Get the connection pipe from the specified end point number
		final UsbEndpoint endpoint = iface.getUsbEndpoint((byte) endpointNumber);
		final UsbPipe connectionPipe = endpoint.getUsbPipe();
		return connectionPipe;
	}

	/**
	 * Writes data to the serial interface.
	 *
	 * @param device the device object with which the communication to be instantiated
	 * @param usbControl to write the data to serial interface
	 * @param data the data which needs to be submitted to the serial interface
	 * @param bytesLength the bytes length of the data to be submitted to the serial interface
	 * @param paddingByte the byte which should be padded (number of padding bytes = bytesLength - data.length)
	 * 			to data array before submitting to the serial interface
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws UsbDisconnectedException the USB disconnected exception
	 * @throws UsbException the USB exception
	 */
	public void writeDataToInterface(final UsbDevice device, final UsbControlIrp usbControl, final byte[] data, final int bytesLength, final byte paddingByte) throws IllegalArgumentException, UsbDisconnectedException, UsbException {
		final byte[] outBuffer = getPaddedByteArray(data, bytesLength, paddingByte);
		usbControl.setData(outBuffer);
		device.asyncSubmit(usbControl);

	}

	/**
	 * Appends the padding bytes to inputArray with padding length = length - inputArray.length
	 * e.g. inputArray = {0x10, 0x20}, length = 8, paddingByte=0xFF
	 * then the reuturn array is = {0x10, 0x20, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF}
	 *
	 * @param inputArray the input array which needs to be padded
	 * @param length the length of the returning byte array
	 * @param paddingByte the byte which needs to be padded
	 * @return the padded byte array
	 */
	public byte[] getPaddedByteArray(final byte[] inputArray, final int length, final byte paddingByte) {
		final byte[] outputArray = new byte[length];
		System.arraycopy(inputArray, 0, outputArray, 0, inputArray.length);
		for(int paddingBitsCounter = inputArray.length; paddingBitsCounter < length; paddingBitsCounter++) {
			outputArray[paddingBitsCounter] = paddingByte;
		}
		return outputArray;
	}

	/**
	 * Finds the USB device if connected using vendor id and product id.
	 *
	 * @param vendorId the vendor id of the USB device
	 * @param productId the product id of the USB device
	 * @return the USB device
	 */
	public UsbDevice getUSBDevice(final short vendorId, final short productId) {
		final UsbHub rootUSBHub = UsbUtil.getVirtualRootUsbHub();
		UsbDevice device = findDevice(rootUSBHub, vendorId, productId);
		if(device == null) {
			throw new DeviceConnectionException("Device not found - is the device plugged into the USB port?");
		}
		return device;
	}

	/**
	 * Find the device if connected using vendor id and product id in each Hub recursively.
	 *
	 * @param hub the USB hub
	 * @param vendorId the vendor id of the USB device
	 * @param productId the product id of the USB device
	 * @return the USB device
	 */
	@SuppressWarnings("unchecked")
	private UsbDevice findDevice(final UsbHub hub, final short vendorId, final short productId) {
		for(UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			final UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
			if(desc.idVendor() == vendorId && desc.idProduct() == productId) {
				return device;
			}
			if(device.isUsbHub()) {
				device = findDevice((UsbHub) device, vendorId, productId);
				if(device != null) {
					return device;
				}
			}
		}
		return null;
	}
}
