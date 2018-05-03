package com.mwee.android.drivenbus.exception;

/**
 * DriverBusException
 * Created by virgil on 16-5-15.
 */
public class DriverBusException extends RuntimeException {
    public DriverBusException() {
        super();
    }

    public DriverBusException(String msg) {
        super(msg);
    }

    public DriverBusException(Throwable throwable) {
        super(throwable);
    }
}
