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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbPipe;

import org.medipi.devices.drivers.domain.BF480Measurement;
import org.medipi.utilities.BytesManipulator;

/**
 * Tests the BF480 device interfaces and collects the measurements it has stored.
 */
public class TestBF480USBService {

	private static final short VENDOR_ID = (short) 0x04d9;
	private static final short PRODUCT_ID = (short) 0x8010;

	private static final int USER_NUMBER = Integer.valueOf(1);
	private static final int READING_START_BYTE_NUMBER = (USER_NUMBER - 1) * 6;

	private USBService usbService = new BF480USBService();

	/**
	 * Test BF480 device to get the measurements.
	 *
	 * @throws Exception the exception
	 */
	//@Test
	public void testGetBF480Measurements() throws Exception {
		List<BF480Measurement> measurements = new ArrayList<BF480Measurement>();
		UsbDevice device = usbService.getUSBDevice(VENDOR_ID, PRODUCT_ID);

		UsbPipe connectionPipe = usbService.getUSBConnection(device, 0, -127);
		byte requestType = 33;
		byte request = 0x09;
		short value = 521;
		short index = 0;
		UsbControlIrp usbControl = usbService.getUSBControl(device, requestType, request, value, index);
		try {
			connectionPipe.open();

			usbService.initialiseDevice(device, usbControl, connectionPipe);

			//Read 128 x 64 data from serial interface
			byte[][] rawReadings = new byte[BF480USBService.MAX_NUMBER_OF_READINGS][BF480USBService.BYTE_ARRAY_LENGTH_128];
			for(int readingsCounter = 0; readingsCounter < BF480USBService.MAX_NUMBER_OF_READINGS; readingsCounter++) {
				rawReadings[readingsCounter] = usbService.readData(connectionPipe, 128);
			}

			//Convert 128 x 64 data to 64 x 64
			int[][] readings = new int[BF480USBService.MAX_NUMBER_OF_READINGS][BF480USBService.BYTE_ARRAY_LENGTH_128 / 2];
			for(int readingsCounter = 0; readingsCounter < BF480USBService.MAX_NUMBER_OF_READINGS; readingsCounter++) {
				readings[readingsCounter] = BytesManipulator.convertBytesToIntegers(rawReadings[readingsCounter]);
			}

			//Transpose the data matrix to retrieve each patients information
			int[][] userReadings = BytesManipulator.transpose(readings);

			//convert all 64 readings to an object by iterating over rows of the matrix
			for(int readingsCounter = 0; readingsCounter < BF480USBService.MAX_NUMBER_OF_READINGS; readingsCounter++) {
				if(userReadings[readingsCounter][READING_START_BYTE_NUMBER + 4] == 0) {
					break;
				}
				measurements.add(new BF480Measurement(userReadings[readingsCounter], READING_START_BYTE_NUMBER));
			}

			Collections.sort(measurements);
		} finally {
			if(connectionPipe != null && connectionPipe.isOpen()) {
				try {
					connectionPipe.close();
					connectionPipe.getUsbEndpoint().getUsbInterface().release();
				} catch(UsbException e) {
					//Do nothing
				}
			}
		}
		System.out.println("Number of readings:" + measurements.size());
		for(BF480Measurement measurement : measurements) {
			System.out.println(measurement + "\n");
		}
	}
}
