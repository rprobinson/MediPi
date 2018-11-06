package uk.gov.nhs.digital.telehealth.connection.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import uk.gov.nhs.digital.telehealth.exceptions.BluetoothAdapterException;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceConnectionException;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceNotFoundException;
import uk.gov.nhs.digital.telehealth.connection.service.ConnectionService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectionService implements ConnectionService {

    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    //private UUID DEFAULT_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private BluetoothAdapter getBluetoothAdapter() throws BluetoothAdapterException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            throw new BluetoothAdapterException("Bluetooth is not enabled.");
        }
        return bluetoothAdapter;
    }

    @Override
    public Collection<?> getAllDevices(Context context) throws DeviceConnectionException {
        BluetoothAdapter bluetoothAdapter = null;
        try {
            bluetoothAdapter = getBluetoothAdapter();
        } catch (BluetoothAdapterException e) {
            throw new DeviceConnectionException(e);
        }
        Set<BluetoothDevice> availableDevices = bluetoothAdapter.getBondedDevices();
        if (availableDevices.isEmpty()) {
            throw new DeviceConnectionException("No devices connected");
        }
        return availableDevices;
    }

    public Object getDriver(Context context, String deviceName) throws DeviceNotFoundException, DeviceConnectionException {
        UsbSerialDriver driver = null;
        List<UsbSerialDriver> availableDrivers = (List<UsbSerialDriver>) getAllDevices(context);
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
        BluetoothDevice device = null;
        Set<BluetoothDevice> availableDevices = (Set<BluetoothDevice>) getAllDevices(context);
        for (BluetoothDevice availableDevice : availableDevices) {
            if(availableDevice.getName().equalsIgnoreCase(deviceName)) {
                device = availableDevice;
            }
        }
        if(device == null) {
            throw new DeviceNotFoundException("No device found with name '" + deviceName + "'");
        }
        return device;
    }

    @Override
    public Object getConnection(Context context, Object device) throws DeviceConnectionException {
        BluetoothSocket bluetoothSocket = null;
        BluetoothDevice bluetoothDevice = (BluetoothDevice) device;
        if(device instanceof BluetoothDevice && device != null) {
            try {
                /*bluetoothDevice.createBond();
                bluetoothDevice.setPairingConfirmation(true);*/
                //bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(DEFAULT_UUID);
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(DEFAULT_UUID);
                /*Class<?> clazz = bluetoothDevice.getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};

                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};

                bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, params);*/
                bluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    Class<?> clazz = bluetoothDevice.getClass();
                    Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

                    Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                    Object[] params = new Object[]{Integer.valueOf(1)};

                    bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, params);
                    bluetoothSocket.connect();
                }  catch (Exception e2) {
                    throw new DeviceConnectionException("Unable to connect to device", e2);
                }
            }
        } else {
            throw new DeviceConnectionException("Unable to connect to device");
        }

        if(bluetoothSocket == null) {
            throw new DeviceConnectionException("Unable to connect to device");
        }
        return bluetoothSocket;
    }
}