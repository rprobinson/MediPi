package uk.gov.nhs.digital.telehealth.exceptions;

public class DeviceNotFoundException extends Exception {
    public DeviceNotFoundException() {
        super();
    }

    public DeviceNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public DeviceNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DeviceNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
