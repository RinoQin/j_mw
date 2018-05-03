package com.mwee.android.drivenbus.exception;

/**
 * IncorrectDriverException
 * Created by virgil on 16-5-15.
 */
public class IncorrectDriverException extends DriverBusException {
    public IncorrectDriverException() {
        super();
    }

    public IncorrectDriverException(String msg) {
        super(msg);
    }

    public IncorrectDriverException(Throwable throwable) {
        super(throwable);
    }
}
