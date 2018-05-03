package com.mwee.android.drivenbus;

/**
 * Created by virgil on 2017/1/20.
 */

public interface IDriveProcessor {
    public void broadcast(String method, Object... params);

    public <T> T call(String uri, Object... params);

}
