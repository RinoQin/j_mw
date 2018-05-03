package com.mwee.android.alp;

/**
 * Created by virgil on 2016/12/11.
 */
public interface IMsgReceiver {
    void receive(String param);

    void connected();

    void disconnected();
}
