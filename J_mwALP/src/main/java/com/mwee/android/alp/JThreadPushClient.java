package com.mwee.android.alp;

import java.util.concurrent.ArrayBlockingQueue;

public class JThreadPushClient {


    /**
     * 单例的实例
     */
    private static JThreadPushClient instance = new JThreadPushClient();
    /**
     * 客户端的实例
     */
    private Client client = null;
    /**
     * 服务器地址
     */
    private volatile String serverAddress;
    /**
     * 服务器端口
     */
    private int serverPort;

    private ArrayBlockingQueue<Message> msgHandler = null;

    private volatile int heartInterval = INTERVAL_LONG;
    /**
     * 心跳间隔，1分钟
     */
    private final static int INTERVAL_LONG = 1000 * 60 ;
    /**
     * 轮询间隔20秒
     */
    private final static int INTERVAL_SHORT = 1000 * 20;

    /**
     * 最短的心跳轮询
     */
    private final static int INTERVAL_MIN = 1000 * 5;

    /**
     * 用户传入的回调
     */
    private IMsgReceiver userReceiver;

    private String name = "";
    private final Object lock = new Object();
    private final Object startlock = new Object();

    /**
     * 维持心跳的线程
     */
    private Thread heartThread = new Thread("PushClientHeart") {
        @Override
        public void run() {
            super.run();
            startWork();
        }

        private void startWork() {
            try {
                doWork();
            } catch (InterruptedException e) {
                e.printStackTrace();
                startWork();
            }
        }

        private void doWork() throws InterruptedException {
            while (true) {
                boolean heartOK = false;

                try {
                    synchronized (lock) {
                        lock.wait(heartInterval);
                    }
                    AlpLog.i("PushClient 执行心跳");

                    if (client == null) {
                        AlpLog.i("PushClient client尚未初始化，wait()_" + Thread.currentThread().getName());
                        synchronized (lock) {
                            lock.wait(INTERVAL_SHORT);
                        }
                        if (client == null) {
                            AlpLog.i("PushClient client尚未初始化，尝试重连" + Thread.currentThread().getName());

                            Message message = new Message();
                            message.setWhat(6666);
                            msgHandler.put(message);

                            continue;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (client != null && !client.isFinish()) {
                    try {
                        heartOK = client.heartBeating();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!heartOK) {
                    AlpLog.i("PushClient 链路异常，心跳间隔调整为5秒，5秒后尝试重连_" + Thread.currentThread().getName());
                    heartInterval = INTERVAL_MIN;
                    try {
                        Thread.sleep(INTERVAL_MIN);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message message = new Message();
                    message.setWhat(6666);
                    msgHandler.put(message);

                } else {
                    AlpLog.i("PushClient 心跳正常_" + Thread.currentThread().getName());
                }
            }
        }
    };

/*************************************************维持心跳的线程 end***********************************************************************/

    /**
     * 连接状态的监听
     */
    private IMoniter innerReceiver = new IMoniter() {
        @Override
        public void connected() {
            AlpLog.i("PushClient 连接成功，每1分钟心跳一次_" + Thread.currentThread().getName());

            /**
             * 如果连接成功，则启动心跳，1分钟心跳一次
             */
            heartInterval = INTERVAL_LONG;
            try {
                synchronized (startlock){
                    startlock.notify();
                }
                synchronized (lock) {
                    lock.notify();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (userReceiver != null) {
                userReceiver.connected();
            }
        }

        @Override
        public void disconnected(boolean manaualStop) {

            /**
             * 如果没有网络，则心跳调整为5秒，并通过心跳进行重试。
             */
            //网络是否可用，替换成通过ping一个ip来达到测试是否能够通信
            if (Util.isNetworkAvailable(null)) {
                heartInterval = INTERVAL_MIN;
                AlpLog.i("PushClient 连接断开，心跳改为5秒_" + Thread.currentThread().getName());
            } else {
                heartInterval = INTERVAL_LONG;
                AlpLog.i("PushClient 连接断开，心跳改为1分钟_" + Thread.currentThread().getName());
            }
//            try {
//                Thread.sleep(INTERVAL_MIN);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            if (!manaualStop) {
                try {
                    synchronized (lock) {
                        lock.notify();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (!manaualStop && userReceiver != null) {
                    userReceiver.disconnected();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void receiveMsg(final String msg) {
//            if (userReceiver != null) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            userReceiver.receive(msg);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//            }
            if (msgHandler != null) {
                Message message = new Message();
                message.setWhat(9999);
                message.setMsg(msg);
                try {
                    msgHandler.put(message);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                /*msgHandler.sendMessage(msgHandler.obtainMessage(9999, msg));*/
            }
        }
    };

/****************************************************连接状态的监听 end***************************************************************/



    private JThreadPushClient() {

        init();
        AlpLog.i("init JThreadPushClient...");
        heartThread.start();
        AlpLog.i("heartThread start...");

        /**
         * * 处理消息的线程，这个线程不能监听到后来不断往msgHandler塞的消息，没有msg时要让这个线程阻塞着
         */
        Thread thread = new Thread("PushClientMsg"+System.currentTimeMillis()){
            @Override
            public void run() {
                AlpLog.i("PushClientMsg Thread run...");
                super.run();
                try {
                    //需要获取当前连接是否建立
                    synchronized (startlock) {
                        startlock.wait();
                    }
                    //这里对msgHandler进行处理
                    while(true){
                        //if(null!=msgHandler) {
                            //AlpLog.i("PushClientMsg Thread run msgHandler size "+msgHandler.size());
                            //AlpLog.i("PushClientMsg Thread run msgHandler empty "+msgHandler.isEmpty());
                            Message msg = msgHandler.take();
                            handleMessage(msg);
                        //}
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    AlpLog.e("PushClientMsg Thread run error...",e);
                }
                AlpLog.i("PushClientMsg Thread run over...");
            }

            public void handleMessage(Message msg) {
                //在这里完成对Message的操作
                final String msgStr = msg.getMsg();
                //AlpLog.i("PushClientMsg Thread handleMessage "+msg.getWhat(),msgStr);
                switch (msg.getWhat()) {
                    case 9999:
                        if (userReceiver != null) {
                            try {
                                userReceiver.receive(msgStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 8888:
                        if (client == null) {
                            return;
                        }
                        client.pushMsgToServer(Configure.MSG_TYPE_BIZ + Configure.SYMBOL_SPLIT + msgStr);
                        break;
                    case 7777:
                        if (client == null) {
                            return;
                        }
                        client.pushMsgToServer(Configure.MSG_TYPE_INNER + Configure.SYMBOL_SPLIT + Configure.KEY_REGIST + Configure.SYMBOL_SPLIT + msgStr);
                        break;
                    case 6666:
                        if (client == null || client.isDisconnected || client.isFinish()) {
                            reTryConnect();
                        }
                        break;
                    case 5555:
                        disConnect();
                        break;
                }
            }
        };

        thread.start();
    }

    public void init(){
        if(msgHandler==null)
            msgHandler = new ArrayBlockingQueue(1);
    }



    public static JThreadPushClient getInstance() {
        return instance;
    }

    /**
     * 推送业务消息到服务器
     *
     * @param msg String | 消息体
     */
    public void pushMsg(final String msg) {
       /* client.pushMsgToServer(msg);*/
        Message message = new Message();
        message.setWhat(8888);
        message.setMsg(msg);
        try {
            msgHandler.put(message);
        }catch (InterruptedException e){
            AlpLog.e("pushMsg error ",e);
            e.printStackTrace();
        }
       /* msgHandler.sendMessage(msgHandler.obtainMessage(8888, msg));*/
    }


    /**
     * 将当前客户端注册到服务器
     *
     * @param name String | 客户端名称
     */
    public void registerToServer(final String name) {
        this.name = name;
        client.setName(name);
        Message message = new Message();
        message.setWhat(7777);
        message.setMsg(name);
        try {
            msgHandler.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*msgHandler.sendMessage(msgHandler.obtainMessage(7777, name));*/
    }


    /**
     * 启动客户端，包括：
     * 1，构建{@link Client}
     * 2，连接到服务器
     *
     * @param address  String
     * @param port     int
     * @param receiver IMsgReceiver
     */
    public void startClient(String address, int port, IMsgReceiver receiver) {
        this.serverAddress = address;
        this.serverPort = port;
        this.userReceiver = receiver;
        connect();
    }

    /**
     * 重置IP服务器
     *
     * @param address String
     */
    public void resetAddress(String address) {
        if(address!= null && serverAddress!= null && address.equals(serverAddress)){
            return;
        }
        this.serverAddress = address;
        connect();
    }


    /**
     * 尝试重连
     */
    private void reTryConnect() {
        connect();
    }


//    private synchronized void doConnect() {
//        if ((serverAddress==null || serverAddress.length()==0) || serverPort < 3000) {
//            AlpLog.e("PushClient connect() 参数没有设置 " + serverAddress==null?"":serverAddress + ":" + serverPort);
//            return;
//        }
//        client.startConnect(serverAddress, serverPort, innerReceiver);
//
//    }

    /**
     * 进行连接
     */
    private synchronized void connect() {
        disConnect();
        if ((serverAddress==null || serverAddress.length()==0) || serverPort < 3000) {
            AlpLog.e("PushClient connect() 参数没有设置 " + serverAddress==null?"":serverAddress + ":" + serverPort);
            return;
        }
        client = new Client();
        client.setName(name);

        Thread workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AlpLog.i("workingThread run...");
                if ((serverAddress==null || serverAddress.length()==0) || serverPort < 3000) {
                    AlpLog.e("PushClient connect() 参数没有设置 " + serverAddress + ":" + serverPort);
                    return;
                }
                client.startConnect(serverAddress, serverPort, innerReceiver);
            }
        });
        workingThread.setName("PushClientWorking_" + System.currentTimeMillis());
        workingThread.start();
        AlpLog.i("PushClient startDo connect()  " + serverAddress + ":" + serverPort);

    }


    /**
     * 断开本地的Socket连接
     */
    private synchronized void disConnect() {
        if (client != null) {
            client.callFinish();
        }
    }



}
