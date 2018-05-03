package com.mwee.android.alp;

import com.mwee.android.alp.IMsgReceiver;
import com.mwee.android.alp.JThreadPushServer;
import org.junit.Test;

public class ServerTest2 {

    @Test
    public void testServer(){
        JThreadPushServer pushServer = JThreadPushServer.getInstance();
        pushServer.startServer(3001, new IMsgReceiver() {
            @Override
            public void receive(String param) {

            }

            @Override
            public void connected() {

            }

            @Override
            public void disconnected() {

            }
        });
        pushServer.pushMsg("server return message ：Hello client");
    }
}
