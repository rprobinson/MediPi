package uk.gov.nhs.digital.telehealth.service;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import uk.gov.nhs.digital.telehealth.connection.service.USBConnectionService;
import uk.gov.nhs.digital.telehealth.domain.BM55User;
import uk.gov.nhs.digital.telehealth.domain.BloodPressure;
import uk.gov.nhs.digital.telehealth.domain.GeographicalLocation;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceConnectionException;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceNotFoundException;

/**
 * Created by Krishna9889 on 25/08/2016.
 */
public class BM55USBService extends USBService {

    private static final int VENDOR_ID = 0x0c45;
    private static final int PRODUCT_ID = 0x7406;

    @Override
    public void initialiseDevice(UsbDeviceConnection connection, UsbInterface interfce, int endpointNumber) {
        writeDataToInterface(connection, new byte[] {(byte) 0xAA}, DEFAULT_BYTE_ARRAY_LENGTH_8, PADDING_BYTE_0xF4);
        readData(connection, interfce, endpointNumber);
    }

    @Override
    public int getNumberOfReadings(UsbDeviceConnection connection, UsbInterface interfce, int endpointNumber) {
        writeDataToInterface(connection, new byte[]{(byte) 0xA2}, DEFAULT_BYTE_ARRAY_LENGTH_8, PADDING_BYTE_0xF4);
        byte[] readBytes = readData(connection, interfce, endpointNumber);
        return readBytes[0];
    }

    @Override
    public byte[] readData(UsbDeviceConnection connection, UsbInterface interfce, int endpointNumber) {
        byte[] data = new byte[8];
        UsbEndpoint inEndpoint = interfce.getEndpoint(endpointNumber);
        connection.claimInterface(interfce, true);
        connection.bulkTransfer(inEndpoint, data, data.length, 1000);
        return data;
    }

    @Override
    public void terminateDeviceCommunication(UsbDeviceConnection connection, UsbInterface interfce, int endpointNumber) {
        writeDataToInterface(connection, new byte[]{(byte) 0xF7}, DEFAULT_BYTE_ARRAY_LENGTH_8, PADDING_BYTE_0xF4);
        readData(connection, interfce, endpointNumber);

        writeDataToInterface(connection, new byte[] {(byte) 0xF6}, DEFAULT_BYTE_ARRAY_LENGTH_8, PADDING_BYTE_0xF4);
        readData(connection, interfce, endpointNumber);
    }

    @Override
    public List<?> getMeasurements(String user, AppCompatActivity activity, USBConnectionService connectionService) throws DeviceNotFoundException, DeviceConnectionException {
        BM55User readingsUser = BM55User.valueOf(user);
        List<BloodPressure> measurements = new ArrayList<BloodPressure>();
        UsbDevice bloodPressureMeter = (UsbDevice) connectionService.getDevice(activity, VENDOR_ID, PRODUCT_ID);
        UsbDeviceConnection connection = getUSBConnection(activity, connectionService, bloodPressureMeter, 0);
        UsbInterface interfce = bloodPressureMeter.getInterface(0);

        this.initialiseDevice(connection, interfce, 0);
        int numberOfReadings = this.getNumberOfReadings(connection, interfce, 0);

        BloodPressure bloodPressure;
        byte[] data;
        for(int readingsCounter = 1; readingsCounter < numberOfReadings; readingsCounter++) {
            this.writeDataToInterface(connection, new byte[] {(byte) 0xA3, (byte) readingsCounter}, BM55USBService.DEFAULT_BYTE_ARRAY_LENGTH_8, BM55USBService.PADDING_BYTE_0xF4);
            data = this.readData(connection, interfce, 0);
            bloodPressure = new BloodPressure(data);
            if(bloodPressure.getUser().equals(readingsUser)) {
                measurements.add(bloodPressure);
            }
        }

        this.terminateDeviceCommunication(connection, interfce, 0);
        return measurements;
    }
}
