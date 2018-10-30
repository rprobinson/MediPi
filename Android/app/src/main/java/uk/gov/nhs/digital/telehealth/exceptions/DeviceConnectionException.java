package uk.gov.nhs.digital.telehealth.exceptions;

public class DeviceConnectionException extends Exception {
    public DeviceConnectionException() {
        super();
    }

    public DeviceConnectionException(String detailMessage) {
        super(detailMessage);
    }

    public DeviceConnectionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DeviceConnectionException(Throwable throwable) {
        super(throwable);
    }
}
