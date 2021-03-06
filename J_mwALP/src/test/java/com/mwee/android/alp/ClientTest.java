package com.mwee.android.alp;

import com.mwee.android.alp.IMsgReceiver;
import com.mwee.android.alp.JThreadPushClient;
import org.junit.Test;

public class ClientTest {

    @Test
    public void testClient(){

        JThreadPushClient jClient= JThreadPushClient.getInstance();
        jClient.startClient("127.0.0.1", 3000, new IMsgReceiver() {
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
        jClient.pushMsg("hello meiwei");



        JThreadPushClient jClient2= JThreadPushClient.getInstance();
        jClient2.pushMsg("hello 星期一");

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jClient2.pushMsg("hello tuiteng");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jClient.pushMsg("hello 不用等");

    }
}
