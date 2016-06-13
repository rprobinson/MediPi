package org.medipi.devices.exceptions;

/**
 * The exception to be thrown if there is any issue with
 * the USB device connection.
 */
@SuppressWarnings("serial")
public class DeviceConnectionException extends RuntimeException {

	/**
	 * Instantiates a new device not connected exception.
	 *
	 * @param message the message
	 */
	public DeviceConnectionException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new device not connected exception.
	 *
	 * @param message the message to be logged or shown on the UI
	 * @param cause the cause
	 */
	public DeviceConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new device not connected exception.
	 *
	 * @param cause the cause
	 */
	public DeviceConnectionException(Throwable cause) {
		super(cause);
	}
}
