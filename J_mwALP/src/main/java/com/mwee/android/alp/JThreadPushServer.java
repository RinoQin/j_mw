package com.mwee.android.alp;

import java.util.concurrent.ArrayBlockingQueue;

public class JThreadPushServer {
    /**
     * 单例
     */
    private static JThreadPushServer instance = new JThreadPushServer();
    /**
     * 服务器的实例
     */
    private Server server = null;
    /**
     * 用户的监听
     */
    private IMsgReceiver userReceiver;

    private ArrayBlockingQueue<Runnable> threadHanlder= null;

    /**
     * 内部和Server的监听
     */
    private IMsgReceiver receiver = new IMsgReceiver() {
        @Override
        public void receive(String param) {
            if (userReceiver != null) {
                try {
                    threadHanlder.put(new Runnable() {
                        @Override
                        public void run() {
                            userReceiver.receive(param);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void connected() {

        }

        @Override
        public void disconnected() {

        }
    };

    private Thread workingThread = null;

    private JThreadPushServer() {
        init();
    }

    public static JThreadPushServer getInstance() {
        if (instance == null) {
            synchronized (JThreadPushServer.class) {
                if (instance == null) {
                    instance = new JThreadPushServer();
                }
            }
        }
        return instance;
    }



    private void init() {
        threadHanlder = new ArrayBlockingQueue(2);
        Thread thread = new Thread("ClientHeartBeatingThread_" + System.currentTimeMillis()){
            @Override
            public void run() {
                super.run();
                handleMessage();
            }

            public void handleMessage() {
                while(true){
                try {
                        Runnable runnable = threadHanlder.take();
                        runnable.run();
                     } catch (InterruptedException e) {
                        e.printStackTrace();
                     }
                }
            }
        };
        thread.start();
    }

    /**
     * 推送消息到所有站点
     *
     * @param msg String | 消息体
     */
    public void pushMsg(final String msg) {
        try {
            threadHanlder.put(new Runnable(){
                @Override
                public void run() {
                    if (server == null) {
                        return;
                    }
                    server.pushMsgToAll(msg);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 推送消息到指定的接收者
     *
     * @param targetName String | 对应{@link PushClient#registerToServer(String)} 里的名称
     * @param msg        String
     */
    public void pushMsgTo(final String targetName, final String msg) {
        try {
            threadHanlder.put(new Runnable(){
                @Override
                public void run() {
                    if (server == null) {
                        return;
                    }
                    server.pushMsgToTarget(targetName, msg);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动服务器
     *
     * @param port         int
     * @param userReceiver IMsgReceiver
     */
    public void startServer(final int port, IMsgReceiver userReceiver) {
        if (port < 3000) {
            AlpLog.e("JThreadPushServer startServer() 非法端口 " + port);
            return;
        }
        if (server != null) {
            server.finish();
        }
        this.userReceiver = userReceiver;

        workingThread = new Thread(() -> {
            server = new Server();
            server.init(port, receiver);
        });
        //TODO
        workingThread.setName("PushServerWorking_" + /*SystemClock.elapsedRealtime()*/System.currentTimeMillis());
        workingThread.start();
    }

    public void finishServer() {
        synchronized (JThreadPushServer.class) {
            if (AlpLog.showLog) {
                AlpLog.i("destroy");
            }
            if (server != null) {
                server.finish();
                server = null;
            }
            try {
                if (workingThread != null && !workingThread.isInterrupted() && workingThread.isAlive()) {
                    workingThread.interrupt();
                }
                workingThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }


            threadHanlder = null;
            instance = null;
        }
    }


}
