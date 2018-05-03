package com.mwee.android.drivenbus.process;

/**
 * Created by virgil on 2017/1/22.
 */

public interface IServciceConnect {
    void call(String url, Object... param);

    void broadcast(String url, Object... param);
}
