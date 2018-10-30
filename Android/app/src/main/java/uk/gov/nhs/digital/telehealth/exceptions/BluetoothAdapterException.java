package uk.gov.nhs.digital.telehealth.exceptions;

public class BluetoothAdapterException extends Exception {
    public BluetoothAdapterException() {
        super();
    }

    public BluetoothAdapterException(String detailMessage) {
        super(detailMessage);
    }

    public BluetoothAdapterException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BluetoothAdapterException(Throwable throwable) {
        super(throwable);
    }
}
