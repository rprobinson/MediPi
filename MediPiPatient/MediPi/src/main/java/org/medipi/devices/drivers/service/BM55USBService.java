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

import java.util.Arrays;

import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;

import org.medipi.devices.exceptions.DeviceConnectionException;

/**
 * The Class BM55USBService extends an abstract class USBService.
 * This class has specific implementations related to Beurer BM55 blood pressure monitor device.
 * This class contains the method implementations such as, device initialisation, device termination
 * and method to get the number of readings the device has stored.
 */
public class BM55USBService extends USBService {

	/* (non-Javadoc)
	 * @see org.medipi.devices.drivers.service.USBService#initialiseDevice(javax.usb.UsbDevice, javax.usb.UsbControlIrp, javax.usb.UsbPipe)
	 */
	@Override
	public void initialiseDevice(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException {
		writeDataToInterface(device, usbControl, new byte[] {(byte) 0xAA}, DEFAULT_BYTE_ARRAY_LENGTH_8, PADDING_BYTE_0xF4);
		readData(connectionPipe, 8);
	}

	/* (non-Javadoc)
	 * @see org.medipi.devices.drivers.service.USBService#getNumberOfReadings(javax.usb.UsbDevice, javax.usb.UsbControlIrp, javax.usb.UsbPipe)
	 */
	@Override
	public int getNumberOfReadings(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException {
		writeDataToInterface(device, usbControl, new byte[] {(byte) 0xA2}, DEFAULT_BYTE_ARRAY_LENGTH_8, PADDING_BYTE_0xF4);
		final byte[] data = readData(connectionPipe, 8);
		return data[0];
	}

	/* (non-Javadoc)
	 * @see org.medipi.devices.drivers.service.USBService#readData(javax.usb.UsbPipe, int)
	 */
	@Override
	public byte[] readData(final UsbPipe connectionPipe, final int numberOfBytes) throws UsbException {
		final byte[] data = new byte[numberOfBytes];
		final UsbIrp irp = connectionPipe.asyncSubmit(data);
        irp.waitUntilComplete(500);

        //This condition is just to check if the data is being read properly. Input and output data cannot be the same if the device is responding.
        if (irp.isUsbException() || Arrays.equals(data, new byte[numberOfBytes])) {
        	throw new DeviceConnectionException("Unplug and then replug in the Beurer BM55 Blood Pressure Monitor and press download");
        }
		return data;
	}

	/* (non-Javadoc)
	 * @see org.medipi.devices.drivers.service.USBService#terminateDeviceCommunication(javax.usb.UsbDevice, javax.usb.UsbControlIrp)
	 */
	@Override
	public void terminateDeviceCommunication(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException, InterruptedException {
		writeDataToInterface(device, usbControl, new byte[] {(byte) 0xF7}, DEFAULT_BYTE_ARRAY_LENGTH_8, PADDING_BYTE_0xF4);
		try {
			readData(connectionPipe, 8);
		} catch(DeviceConnectionException e) {
			//do nothing
		}
	}
}
