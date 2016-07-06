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

import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;

import org.medipi.devices.exceptions.DeviceConnectionException;

/**
 * The Class BF480USBService extends an abstract class USBService.
 * This class has specific implementations related to Beurer BF480 Diagnostic scale.
 * This class contains the method implementations such as, device initialisation, device termination
 * and method to get the number of readings the device has stored.
 */
public class BF480USBService extends USBService {

	/** The Constant MAX_NUMBER_OF_READINGS represents that the beurer BF480 has maximum of 64 readings stored per user. */
	public static final int MAX_NUMBER_OF_READINGS = 64;

	/* (non-Javadoc)
	 * @see org.medipi.devices.drivers.service.USBService#initialiseDevice(javax.usb.UsbDevice, javax.usb.UsbControlIrp, javax.usb.UsbPipe)
	 */
	@Override
	public void initialiseDevice(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException {
		writeDataToInterface(device, usbControl, new byte[] {(byte) 0x10}, DEFAULT_BYTE_ARRAY_LENGTH_8, PADDING_BYTE_0x00);
	}

	/* (non-Javadoc)
	 * @see org.medipi.devices.drivers.service.USBService#getNumberOfReadings(javax.usb.UsbDevice, javax.usb.UsbControlIrp, javax.usb.UsbPipe)
	 */
	@Override
	public int getNumberOfReadings(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException {
		return MAX_NUMBER_OF_READINGS;
	}

	/* (non-Javadoc)
	 * @see org.medipi.devices.drivers.service.USBService#readData(javax.usb.UsbPipe, int)
	 */
	@Override
	public byte[] readData(final UsbPipe connectionPipe, final int numberOfBytes) throws UsbException {
		final byte[] data = new byte[numberOfBytes];
		final UsbIrp irp = connectionPipe.asyncSubmit(data);
        irp.waitUntilComplete(3000);

        //This condition is just to check if the data is being read properly.
        if (irp.isUsbException()) {
        	throw new DeviceConnectionException("Unplug and then replug in the Beurer BF480 Diagnostic Scale and press download");
        }
		return data;
	}

	/* (non-Javadoc)
	 * @see org.medipi.devices.drivers.service.USBService#terminateDeviceCommunication(javax.usb.UsbDevice, javax.usb.UsbControlIrp)
	 */
	@Override
	public void terminateDeviceCommunication(final UsbDevice device, final UsbControlIrp usbControl, final UsbPipe connectionPipe) throws UsbException, InterruptedException {
		// Not required to terminate the device communication
	}
}