package com.mwee.android.test;

import com.mwee.android.alp.IMsgReceiver;
import com.mwee.android.alp.JThreadPushServer;

public class TestServer2 {

    public static void main(String[] args){
        JThreadPushServer pushServer2 = JThreadPushServer.getInstance();
        pushServer2.startServer(3001, new IMsgReceiver() {
            @Override
            public void receive(String param) {
                pushServer2.pushMsg("server2 收到了 "+param);
            }

            @Override
            public void connected() {System.out.println("来自server2 IMsgReceiver.connected");
                System.out.println("server2 已与client 建立连接");
            }

            @Override
            public void disconnected() {
                System.out.println("server2 已与client 断开连接");
            }
        });


        pushServer2.pushMsg("来自server2 Hello client");
    }
}
