package com.mwee.android.test;

import com.mwee.android.alp.IMsgReceiver;
import com.mwee.android.alp.JThreadPushClient;

public class TestClient {

    public static void main(String[] args){

        JThreadPushClient jClient= JThreadPushClient.getInstance();
        jClient.startClient("127.0.0.1", 3000, new IMsgReceiver() {
            @Override
            public void receive(String param) {
                System.out.println("client 收到Server的回调："+param);
            }
            @Override
            public void connected() {
                System.out.println("client 已与server 建立连接");
            }

            @Override
            public void disconnected() {
                System.out.println("client 已与server 断开连接");
            }
        });

        jClient.pushMsg("来自client 第一条消息");



        /*JThreadPushClient jClient2= JThreadPushClient.getInstance();*/
        jClient.pushMsg("来自client 第二条消息");

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jClient.pushMsg("来自client 第三条消息");
        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        jClient.pushMsg("来自client 第四条消息");
        jClient.pushMsg("来自client 第五条消息");
        jClient.pushMsg("来自client 第六条消息");
        jClient.pushMsg("来自client 第七条消息");

    }
}
