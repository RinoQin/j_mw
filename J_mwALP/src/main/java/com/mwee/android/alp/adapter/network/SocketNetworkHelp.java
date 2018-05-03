package com.mwee.android.alp.adapter.network;

import com.mwee.android.alp.AlpLog;

import java.net.InetAddress;

public class SocketNetworkHelp extends NetworkHelpBase{

    private static String IP = "www.baidu.com";

    /**
     * 测试本地能否ping ip，用以检测网络是否可以建立通信,经测试在安卓环境运行ping百度可行
     *
     * @return
     */
    public static boolean isNetworkAvailable(Object obj) {
        boolean isReach = false;
        try {
            InetAddress address = InetAddress.getByName(IP);// ping this IP

            //3秒超时,isReachable在Internet上由于防火墙等因素导致并不可靠，应用在在局域网中较好
            if (address.isReachable(3000)) {
                isReach = true;
                AlpLog.i("SUCCESS - ping " + IP + " with no interface specified");
            } else {
                isReach = false;
                AlpLog.i("FAILURE - ping " + IP + " with no interface specified");
            }
        } catch (Exception e) {
            AlpLog.i("error occurs:" + e.getMessage());
        }
        return isReach;

    }

}
