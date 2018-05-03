package com.mwee.android.alp;

import com.mwee.android.alp.IMsgReceiver;
import com.mwee.android.alp.JThreadPushServer;
import org.junit.Test;

public class ServerTest {

    @Test
    public void tesetServer(){
        JThreadPushServer pushServer = JThreadPushServer.getInstance();
        pushServer.startServer(3000, new IMsgReceiver() {
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


        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pushServer.pushMsg("server return message ：Hello client");
    }
}
