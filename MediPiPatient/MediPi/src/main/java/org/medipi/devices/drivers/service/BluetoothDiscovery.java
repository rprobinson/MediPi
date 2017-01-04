/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.devices.drivers.service;

import com.intel.bluetooth.RemoteDeviceHelper;
import java.io.IOException;
import java.util.ArrayList;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

/**
 * Class to discover bluetooth devices
 *
 * IMPORTANT: This is not currently used but was developed as a start for creating a
 * natively coded bluetooth manager
 *
 * @author rick@robinsonhq.com
 */
public class BluetoothDiscovery implements DiscoveryListener {

    private static Object lock = new Object();
    public ArrayList<RemoteDevice> devices;

    public BluetoothDiscovery() {
        devices = new ArrayList<RemoteDevice>();
    }

    public void findDevices() {
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();
            agent.startInquiry(DiscoveryAgent.GIAC, this);

            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            System.out.println("Device Inquiry Completed. ");

            UUID[] uuidSet = new UUID[1];
            uuidSet[0] = new UUID(0x1101); //OBEX Object Push service

            int[] attrIDs = new int[]{
                0x0003 // Service name
            };

            for (RemoteDevice device : devices) {
                agent.searchServices(attrIDs, uuidSet, device, this);
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                System.out.println("Service search finished.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass arg1) {

        String name;
        try {
            name = btDevice.getFriendlyName(false);
        } catch (Exception e) {
            name = btDevice.getBluetoothAddress();
        }
        devices.add(btDevice);
        System.out.println("device found: " + name);

    }

    @Override
    public void inquiryCompleted(int arg0) {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void serviceSearchCompleted(int arg0, int arg1) {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {
            String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            System.out.println("url = " + url);
            if (url == null) {
                continue;
            }
            DataElement serviceName = servRecord[i].getAttributeValue(0x0003);
            if (serviceName != null) {
                System.out.println("service " + serviceName.getValue() + " found " + url);

                if (serviceName.getValue().equals("OBEX Object Push")) {
//                    sendMessageToDevice(url);                
                }
            } else {
            }

        }
    }

// this code doesnt seem to work but it should???
    private Boolean pairingDevice(RemoteDevice remoteDevice) {
        //check if authenticated already
        if (remoteDevice.isAuthenticated()) {
            return true;
        } else {

            System.out.println("--> Pairing device");
            boolean devicePaired;
            try {
                boolean paired = RemoteDeviceHelper.authenticate(remoteDevice);
                //LOG.info("Pair with " + remoteDevice.getFriendlyName(true) + (paired ? " succesfull" : " failed"));
                devicePaired = paired;
                if (devicePaired) {
                    System.out.println("--> Pairing successful with device " + remoteDevice.getBluetoothAddress());
                } else {
                    System.out.println("--> Pairing unsuccessful with device " + remoteDevice.getBluetoothAddress());
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("--> Pairing unsuccessful with device " + remoteDevice.getBluetoothAddress());
                devicePaired = false;
            }
            System.out.println("--> Pairing device Finish");
            return devicePaired;
        }
    }

}
