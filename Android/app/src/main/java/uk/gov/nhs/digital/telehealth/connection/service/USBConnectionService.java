package uk.gov.nhs.digital.telehealth.connection.service;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceConnectionException;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceNotFoundException;

import java.util.Collection;
import java.util.List;

public class USBConnectionService implements ConnectionService {

    public UsbManager getUsbManager(Context context) {
        return (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public Collection<?> getAllDevices(Context context) throws DeviceConnectionException {
        UsbManager manager = getUsbManager(context);
        Collection<UsbDevice> availableDevices = manager.getDeviceList().values();
        if (availableDevices.isEmpty()) {
            throw new DeviceConnectionException("No devices connected");
        }
        return availableDevices;
    }

    public List<?> getAllDrivers(Context context) throws DeviceConnectionException {
        UsbManager manager = getUsbManager(context);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            throw new DeviceConnectionException("No devices connected");
        }
        return availableDrivers;
    }

    public Object getDriver(Context context, String deviceName) throws DeviceNotFoundException, DeviceConnectionException {
        UsbSerialDriver driver = null;
        List<UsbSerialDriver> availableDrivers = (List<UsbSerialDriver>) getAllDrivers(context);
        for (UsbSerialDriver availableDriver : availableDrivers) {
            if(availableDriver.getDevice().getDeviceName().equalsIgnoreCase(deviceName)) {
                driver = availableDriver;
            }
        }
        if(driver == null) {
            throw new DeviceNotFoundException("No device found with name '" + deviceName + "'");
        }
        return driver;
    }

    @Override
    public Object getDevice(Context context, String deviceName) throws DeviceNotFoundException, DeviceConnectionException {
        UsbSerialDriver driver = (UsbSerialDriver)getDriver(context, deviceName);
        return driver.getDevice();
    }

    public UsbDevice getDevice(Context context, int vendorId, int productId) throws DeviceNotFoundException, DeviceConnectionException {
        Collection<UsbDevice> availableDevices = (Collection<UsbDevice>) getAllDevices(context);
        UsbDevice device = null;
        for (UsbDevice availableDevice : availableDevices) {
            if(availableDevice.getVendorId() == vendorId && availableDevice.getProductId() == productId) {
                device = availableDevice;
            }
        }
        if(device == null) {
            throw new DeviceNotFoundException("Device with VENDOR ID:" + vendorId + " and PRODUCT ID:" + productId +" not found");
        }
        return device;
    }

    public UsbSerialDriver getUsbDriverByDevice(Context context, UsbDevice device) throws DeviceNotFoundException, DeviceConnectionException {
        UsbSerialDriver driver = null;
        if(device != null) {
            List<UsbSerialDriver> availableDrivers = (List<UsbSerialDriver>) getAllDrivers(context);
            for (UsbSerialDriver availableDriver : availableDrivers) {
                UsbDevice availableDevice = availableDriver.getDevice();
                if(availableDevice.getVendorId() == device.getVendorId() && availableDevice.getProductId() == device.getProductId()) {
                    driver = availableDriver;
                    break;
                }
            }
        }
        if(driver == null) {
            throw new DeviceNotFoundException("No driver found for the device:" + device.toString());
        }
        return driver;
    }

    @Override
    public Object getConnection(Context context, Object device) throws DeviceConnectionException {
        UsbDeviceConnection connection = null;
        if(device instanceof UsbDevice) {
            UsbDevice usbDevice = (UsbDevice) device;
            UsbManager manager = getUsbManager(context);
            connection = manager.openDevice(usbDevice);
        } else {
            throw new DeviceConnectionException("Unable to connect to device");
        }

        if(connection == null) {
            throw new DeviceConnectionException("Unable to connect to device");
        }
        return connection;
    }

    public String getDriverInfo(UsbSerialDriver driver) {
        UsbDevice device = driver.getDevice();
        StringBuilder deviceInfo = new StringBuilder("Device id:" + device.getDeviceId());
        deviceInfo.append("\nDeviceName:" + device.getDeviceName());
        deviceInfo.append("\nManufacturerName:" + device.getManufacturerName());
        deviceInfo.append("\nProductName:" + device.getProductName());
        deviceInfo.append("\nSerialNumber:" + device.getSerialNumber());
        deviceInfo.append("\nVersion:" + device.getVersion());
        deviceInfo.append("\nDeviceProtocol:" + device.getDeviceProtocol());
        deviceInfo.append("\nVendorId:" + device.getVendorId());
        deviceInfo.append("\nProductId:" + device.getProductId());
        return deviceInfo.toString();
    }

    public String getDeviceInfo(UsbDevice device) {
        StringBuilder deviceInfo = new StringBuilder("Device id:" + device.getDeviceId());
        deviceInfo.append("\nDeviceName:" + device.getDeviceName());
        deviceInfo.append("\nManufacturerName:" + device.getManufacturerName());
        deviceInfo.append("\nProductName:" + device.getProductName());
        deviceInfo.append("\nSerialNumber:" + device.getSerialNumber());
        deviceInfo.append("\nVersion:" + device.getVersion());
        deviceInfo.append("\nDeviceProtocol:" + device.getDeviceProtocol());
        deviceInfo.append("\nVendorId:" + device.getVendorId());
        deviceInfo.append("\nProductId:" + device.getProductId());
        return deviceInfo.toString();
    }
}