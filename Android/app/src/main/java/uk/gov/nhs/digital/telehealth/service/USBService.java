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
package uk.gov.nhs.digital.telehealth.service;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import uk.gov.nhs.digital.telehealth.connection.service.USBConnectionService;
import uk.gov.nhs.digital.telehealth.domain.GeographicalLocation;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceConnectionException;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceNotFoundException;

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
	 */
	public abstract void initialiseDevice(UsbDeviceConnection connection, UsbInterface interfce, int endpointNumber);

	/**
	 * Gets the number of readings the device has stored.
	 *
	 */
	public abstract int getNumberOfReadings(UsbDeviceConnection connection, UsbInterface interfce, int endpointNumber);

	/**
	 * Read data from the serial interface.
	 */
	public abstract byte[] readData(UsbDeviceConnection connection, UsbInterface interfce, int endpointNumber);

	/**
	 * Terminate device communication.
	 */
	public abstract void terminateDeviceCommunication(UsbDeviceConnection connection, UsbInterface interfce, int endpointNumber);

	public abstract List<?> getMeasurements(String user, AppCompatActivity activity, USBConnectionService connectionService) throws DeviceNotFoundException, DeviceConnectionException;

	/**
	 * Creates the USB connection object which will be used to read the data from the serial interface.
	 */
	public UsbDeviceConnection getUSBConnection(AppCompatActivity activity, USBConnectionService connectionService, UsbDevice device, int interfaceNumber) throws DeviceConnectionException {
		final UsbDeviceConnection connection = (UsbDeviceConnection) connectionService.getConnection(activity, device);
		final UsbInterface interfce = device.getInterface(interfaceNumber);
		connection.claimInterface(interfce, true);
		return connection;
	}

	/**
	 * Writes data to the serial interface.
	 */
	public int writeDataToInterface(UsbDeviceConnection connection, byte[] data, int bytesLength, byte paddingByte) {
		byte[] outBuffer = getPaddedByteArray(data, bytesLength, paddingByte);
		int numberOfBytesSent = connection.controlTransfer(33, 0x09, 2 << 8 | 9, 0, outBuffer, outBuffer.length, 1000);
		return numberOfBytesSent;
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
}