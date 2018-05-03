package com.mwee.android.test;

import com.mwee.android.alp.IMsgReceiver;
import com.mwee.android.alp.JThreadPushServer;

public class TestServer {

    public static void main(String[] args){
        JThreadPushServer pushServer = JThreadPushServer.getInstance();
        pushServer.startServer(3000, new IMsgReceiver() {
            @Override
            public void receive(String param) {
                pushServer.pushMsg("server 收到了 "+param);
            }

            @Override
            public void connected() {
                System.out.println("server 已与client 建立连接");
            }

            @Override
            public void disconnected() {
                System.out.println("server 已与client 断开连接");
            }
        });
    }
}
