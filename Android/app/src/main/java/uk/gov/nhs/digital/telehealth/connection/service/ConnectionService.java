package uk.gov.nhs.digital.telehealth.connection.service;

import android.content.Context;

import uk.gov.nhs.digital.telehealth.exceptions.DeviceConnectionException;
import uk.gov.nhs.digital.telehealth.exceptions.DeviceNotFoundException;

import java.util.Collection;

public interface ConnectionService {
    Collection<?> getAllDevices(Context context) throws DeviceConnectionException;
    Object getDevice(Context context, String deviceName) throws DeviceNotFoundException, DeviceConnectionException;
    Object getConnection(Context context, Object device) throws DeviceConnectionException;
}
