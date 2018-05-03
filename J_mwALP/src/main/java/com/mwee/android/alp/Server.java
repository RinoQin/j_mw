package com.mwee.android.alp;

//TODO 无法用java中的系统时间替换，建议抽取到android类库
/* 经对业务的理解，ClientHandler.lastBeating初始值为0，是上一次收到心跳的时间，所以计时基于开机还是1970都是可以的
 */
/*import android.os.SystemClock;*/

/*import android.support.v4.util.ArrayMap;*/
/*import android.text.TextUtils;*/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推送服务器
 * Created by virgil on 2016/12/11.
 */
class Server {

    /**
     * 客户端和名称的mapping
     */
    /*private ArrayMap<String, ClientHandler> keyClient = new ArrayMap<>();*/
    private HashMap<String, ClientHandler> keyClient = new HashMap<String, ClientHandler>();
    /**
     * 客户端列表
     */
    private List<ClientHandler> clientList = new ArrayList<ClientHandler>();
    /**
     * 手动终止
     */
    private volatile boolean callFinish = false;
    /**
     * 服务器的监听
     */
    private IMsgReceiver receiver;
    private Thread handlerThread = null;

    protected Server() {
        if (AlpLog.showLog) {
            AlpLog.i("server created "+ Thread.currentThread().getName());
        }
    }

    /**
     * 链路管理器，每6分钟检测所有链路，如果链路的心跳间隔超过了6分钟，则主动断掉链路
     */
    private void initClientChecker() {
        if (handlerThread == null) {
            handlerThread = new Thread("ALPServerLoop") {
                @Override
                public void run() {
                    super.run();
                    while (!checkFinish()) {
                        try {
                            Thread.sleep(6 * 60 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AlpLog.i("Server 开始轮询心跳链路");
                        try {
                            List<ClientHandler> cloneClientList = new ArrayList<ClientHandler>();
                            cloneClientList.addAll(clientList);
                            for (int i = 0; i < cloneClientList.size(); i++) {
                                ClientHandler temp = cloneClientList.get(i);
                                //TODO 无法用java中的系统时间替换，建议抽取到android类库
                                //当前时间减去最后一次心跳时间，整体改用System.currentTimeMillis()不影响业务逻辑，方案可行
                                if (temp.isFinished || (/*SystemClock.elapsedRealtime()*/System.currentTimeMillis() - temp.lastBeating) > 6 * 60 * 1000) {
                                    if (AlpLog.showLog) {
                                        if (temp.isFinished) {
                                            AlpLog.i("Server 链路[" + temp.getLogName() + "] isFinished");
                                        } else {
                                            AlpLog.i("Server 链路[" + temp.getLogName() + "] 超时");
                                        }
                                    }
                                    temp.callStop();
                                    synchronized (Server.this) {
                                        clientList.remove(temp);
                                    }
                                    cloneClientList.remove(temp);
                                    i--;
                                    /*if (!TextUtils.isEmpty(temp.clientKey)) {*/
                                    if (temp.clientKey!=null && temp.clientKey.length()>0){
                                                keyClient.remove(temp.clientKey);
                                    } else {
                                        if (keyClient.containsValue(temp)) {
                                            for (Map.Entry<String, ClientHandler> tempEntry : keyClient.entrySet()) {
                                                keyClient.remove(tempEntry.getKey());
                                                break;
                                            }
                                        }
                                    }

                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            handlerThread.start();
        }
    }

    /**
     * 初始化服务器
     *
     * @param port     int
     * @param receiver IMsgReceiver
     */
    protected void init(int port, IMsgReceiver receiver) {
        if (checkFinish()) {
            return;
        }
        initClientChecker();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            this.receiver = receiver;
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));

            while (!checkFinish()) {
                Socket client = serverSocket.accept();
                receiveClient(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (AlpLog.showLog) {
                    AlpLog.e("server close " + Thread.currentThread().getName());
                }
                if (!checkFinish()) {
                    Thread.sleep(10 * 1000);
                    if (AlpLog.showLog) {
                        AlpLog.e("server start retry " + Thread.currentThread().getName());
                    }
                    init(port, receiver);
                }else{
                    if (AlpLog.showLog) {
                        AlpLog.e("server finally finished "+ Thread.currentThread().getName());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 注销当前的链路
     *
     * @param client ClientHandler
     */
    private void unRegriserClient(ClientHandler client) {
        if (client == null) {
            return;
        }
        synchronized (Server.this) {
            if (clientList != null && !clientList.isEmpty()) {
                if (clientList.contains(client)) {
                    clientList.remove(client);
                }
            }
        }
        if (keyClient != null && !keyClient.isEmpty()) {
            String key = client.clientKey;
            if (keyClient.containsKey(key)) {
                keyClient.remove(key);
            }
        }
    }

    /**
     * 收到链路创建的请求
     *
     * @param client Socket
     */
    private void receiveClient(Socket client) {
        if (AlpLog.showLog) {
            AlpLog.i("Server receiveClient " + String.format("开始监听客户端: %s", client.getRemoteSocketAddress()));
        }
        ClientHandler clientHandler = new ClientHandler(client);
        synchronized (Server.this) {
            clientList.add(clientHandler);
        }
        clientHandler.start();
        //TODO
        clientHandler.lastBeating = /*SystemClock.elapsedRealtime()*/System.currentTimeMillis();
    }

    /**
     * 处理消息
     *
     * @param socket ClientHandler
     * @param key    String
     * @param value  String
     */
    private void processMsg(ClientHandler socket, String key, String value) {
        switch (key) {
            case Configure.MSG_TYPE_BIZ:
                if (receiver != null) {
                    receiver.receive(value);
                }
                break;
            case Configure.MSG_TYPE_INNER:
                int indexValueType = value.indexOf(Configure.SYMBOL_SPLIT);
                String valueType = value.substring(0, indexValueType);
                String msgValue = value.substring(indexValueType + Configure.SYMBOL_SPLIT.length());
                switch (valueType) {
                    case Configure.KEY_HEART:
                        //TODO
                        socket.lastBeating = /*SystemClock.elapsedRealtime()*/System.currentTimeMillis();
                        break;
                    case Configure.KEY_REGIST:
                        keyClient.put(msgValue, socket);
                        socket.clientKey = msgValue;
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 给所有链路推送消息
     *
     * @param msg String
     */
    protected void pushMsgToAll(String msg) {
        try {
            String finalMsg = Configure.MSG_TYPE_BIZ + Configure.SYMBOL_SPLIT + msg;
            List<ClientHandler> tempList = new ArrayList<ClientHandler>();
            tempList.addAll(clientList);
            for (ClientHandler temp : tempList) {
                temp.pushMsg(finalMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 推送消息给指定的链路
     *
     * @param targetName String
     * @param msg        String
     */
    public void pushMsgToTarget(String targetName, String msg) {
        ClientHandler temp = keyClient.get(targetName);
        if (temp != null) {
            String finalMsg = Configure.MSG_TYPE_BIZ + Configure.SYMBOL_SPLIT + msg;
            temp.pushMsg(finalMsg);
        }
    }

    private synchronized boolean checkFinish() {
        return callFinish;
    }

    /**
     * 结束掉当前的服务器
     */
    public void finish() {
        if (AlpLog.showLog) {
            AlpLog.i("Server call finish " + Thread.currentThread().getName());
        }
        clientList.clear();
        keyClient.clear();
        synchronized (this) {
            callFinish = true;
        }
    }

    /**
     * 链路
     */
    private class ClientHandler extends Thread {
        private Socket client;
        private InputStream in;
        private OutputStream out;
        private boolean callStop = false;
        private String clientKey = "";
        /**
         * 上一次收到心跳包的时间
         */
        private long lastBeating = 0L;
        private boolean isFinished = false;

        ClientHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            super.run();

            try {
                init();
                byte[] header = new byte[4];
                while (!callStop) {
                    if (in == null) {
                        break;
                    }
                    String clientMsg = Util.socketReader(in, header);
                    if (/*TextUtils.isEmpty(clientMsg)*/clientMsg==null || clientMsg.length()==0) {
                        AlpLog.i("Server " + getLogName() + "链路已断开");
                        callStop();
                        break;
                    }
                    if (AlpLog.showLog) {
                        AlpLog.i("Server receive msg [" + clientMsg + "] from [" + this.getLogName() + "]");
                    }
                    int indexMsgType = clientMsg.indexOf(Configure.SYMBOL_SPLIT);
                    String msgType = clientMsg.substring(0, indexMsgType);
                    String value = clientMsg.substring(indexMsgType + Configure.SYMBOL_SPLIT.length());
                    processMsg(this, msgType, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
                callStop();
            } catch (Error e) {
                e.printStackTrace();
                callStop();
            }
            isFinished = true;
        }

        /**
         * 初始化流
         *
         * @throws IOException
         */
        private void init() throws IOException {
            in = client.getInputStream();
            out = client.getOutputStream();
        }

        /**
         * 通过当前链路推送消息
         *
         * @param msg String
         */
        private void pushMsg(String msg) {
            try {
                synchronized (ClientHandler.this) {
                    if (out != null) {
                        byte[] infoByte = msg.getBytes();
                        byte[] header = Util.integerToBytes(infoByte.length, 4);
                        out.write(header);
                        out.write(infoByte);
                        out.flush();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
                callStop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 结束链路，并注销掉Server的引用
         */
        private void callStop() {
            callStop = true;
            unRegriserClient(this);
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            in = null;
            out = null;
            client = null;
            isFinished = true;
        }

        /**
         * 获取链路的日志名称
         *
         * @return String
         */
        private String getLogName() {
            return clientKey + "," + (client != null ? client.getRemoteSocketAddress() : "");
        }
    }
}
