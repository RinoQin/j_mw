package com.mwee.android.alp;

import com.mwee.android.alp.IMsgReceiver;
import com.mwee.android.alp.JThreadPushClient;
import org.junit.Test;

public class ClientTest2 {

    @Test
    public void testClient(){

        JThreadPushClient jClient= JThreadPushClient.getInstance();
        jClient.startClient("127.0.0.1", 3001, new IMsgReceiver() {
            @Override
            public void receive(String param) {
                System.out.println("这里是IMsgReceiver.receive");
            }

            @Override
            public void connected() {
                System.out.println("这里是IMsgReceiver.connected");
            }

            @Override
            public void disconnected() {
                System.out.println("这里是IMsgReceiver.disconnected");
            }
        });
        jClient.pushMsg("hello2 meiwei");



        JThreadPushClient jClient2= JThreadPushClient.getInstance();
        jClient2.pushMsg("hello2 星期一");

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jClient2.pushMsg("hello2 tuiteng");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jClient.pushMsg("hello2 不用等");

    }
}
