package com.mwee.android.drivenbus.exception;

/**
 * NoDriverException
 * Created by virgil on 16-5-15.
 */
public class NoDriverException extends DriverBusException {
    public NoDriverException() {
        super("No driver found");
    }

    public NoDriverException(String msg) {
        super(msg);
    }

}
