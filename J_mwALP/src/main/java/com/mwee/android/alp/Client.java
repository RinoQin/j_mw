package com.mwee.android.alp;


/*import android.text.TextUtils;*/

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 客户端
 * Created by virgil on 2016/12/11.
 */
class Client {
    /**
     * 客户端持有的Socket
     */
    private Socket socket = null;
    /**
     * 手动终止
     */
    private boolean callFinish = false;
    private InputStream in;
    private OutputStream out;
    /**
     * 客户端的监听
     */
    private IMoniter receiver;
    /**
     * 当前客户端的名称
     */
    private String name;
    private static final Object lock = new Object();
    public boolean isDisconnected = false;

    protected Client() {

    }

    /**
     * 启动客户端的连接
     *
     * @param address  String
     * @param port     int
     * @param receiver IMoniter
     */
    public void startConnect(String address, int port, IMoniter receiver) {
        callFinish = false;
        this.receiver = receiver;
        socket = new Socket();
        try {
            AlpLog.i("Client startClient()  " + address + ":" + port);

            socket.connect(new InetSocketAddress(address, port), 3000);
            socket.setKeepAlive(true);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            if (receiver != null) {
                receiver.connected();
            }
            byte[] header = new byte[4];
            while (!callFinish) {
                if (in == null) {
                    AlpLog.i("Client " + getName() + " 输入流已为null" + Thread.currentThread().getName());
                    break;
                }

                String clientMsg = Util.socketReader(in, header);
                /*if (TextUtils.isEmpty(clientMsg)) {*/
                if (clientMsg == null || clientMsg.length() == 0) {
                    AlpLog.i("Client " + getName() + " 链路已断开_" + Thread.currentThread().getName());
                    break;
                }

                AlpLog.i("Client " + getName() + " receive:" + clientMsg);
                int indexMsgType = clientMsg.indexOf(Configure.SYMBOL_SPLIT);
                String msgType = clientMsg.substring(0, indexMsgType);
                String value = clientMsg.substring(indexMsgType + Configure.SYMBOL_SPLIT.length());
                processMsg(msgType, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        } finally {
            close();

            if (receiver != null) {
                receiver.disconnected(callFinish);
            }
        }

    }

    private void close() {
        if (!callFinish) {
            isDisconnected = true;
        }
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        in = null;
        out = null;
        socket = null;
    }

    /**
     * 接收到Server推送的消息
     *
     * @param key   String
     * @param value String
     */
    private void processMsg(String key, String value) {
        switch (key) {
            case Configure.MSG_TYPE_BIZ:
                if (receiver != null) {
                    receiver.receiveMsg(value);
                }
                break;
            case Configure.MSG_TYPE_INNER:
                int indexValueType = value.indexOf(Configure.SYMBOL_SPLIT);
                String valueType = value.substring(0, indexValueType);
                String msgValue = value.substring(indexValueType + Configure.SYMBOL_SPLIT.length());
                AlpLog.i("Client processMsg : " + msgValue);
                switch (valueType) {
                    case Configure.KEY_HEART:
                        break;
                    case Configure.KEY_REGIST:
                        break;
                }
                break;
        }
    }

    /**
     * 推送消息到服务器
     *
     * @param msg String
     */
    public boolean pushMsgToServer(String msg) {
        try {
            if (isFinish()) {
                AlpLog.i("Client is finished " + Thread.currentThread().getName());
                return false;
            }
            if (out != null) {
                byte[] infoByte = msg.getBytes();
                byte[] header = Util.integerToBytes(infoByte.length, 4);
                synchronized (lock) {
                    out.write(header);
                    out.write(infoByte);
                    out.flush();
                }
                AlpLog.i("Client pushMsgToServer [" + msg + "]_" + Thread.currentThread().getName());

                return true;
            } else {
                AlpLog.i("Client out stream is null" + Thread.currentThread().getName());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检测链路是否连接正常
     *
     * @return boolean | true： 链路正常；false：链路已断开
     */
    private boolean checkAlive() {
        synchronized (lock) {
            try {

                socket.sendUrgentData(0xFF);

            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 发送心跳报文
     */
    private boolean sendHeartBeating() {
        return pushMsgToServer(Configure.MSG_TYPE_INNER + Configure.SYMBOL_SPLIT  + Configure.KEY_HEART + Configure.SYMBOL_SPLIT);
    }

    /**
     * 一次心跳的结果，检测：socket的状态、连接状态、是否关闭，并调用{@link #checkAlive()}来检测链路
     * 最后，发送心跳的报文{@link #sendHeartBeating()}
     *
     * @return boolean
     */
    protected boolean heartBeating() {
        if (!(socket != null && !socket.isClosed() && socket.isConnected())) {
            AlpLog.i("Client 链路异常");
            return false;
        }
        if (!checkAlive()) {
            return false;
        }
        return sendHeartBeating();
    }

    /**
     * 终止Socket
     */
    public void callFinish() {
        callFinish = true;
        close();
    }

    /**
     * 链路是否被手动终止
     *
     * @return boolean | true: 已被手动终止；false：没有被手动终止
     */
    public boolean isFinish() {
        return callFinish;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
