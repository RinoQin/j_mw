package com.mwee.android.alp;

// network已经抽出
/*import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;*/
/*import android.text.TextUtils;*/

import com.mwee.android.alp.adapter.NetworkAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by virgil on 2016/12/12.
 */

class Util {
    /**
     * 网络是否可用
     *
     * @return boolean｜true:可用；false：不可用
     * 每个平台的网络检测可能有区别
     */
    /*public static boolean isNetworkAvailable(Context context) {
        boolean flag = false;
        ConnectivityManager manager = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (manager != null) {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) {
                flag = networkInfo.isAvailable();
            }
        }
        return flag;
    }*/
    public static boolean isNetworkAvailable(Object obj) {
        return NetworkAdapter.isNetworkAvailable(obj);
    }

    public static char[] writeInt(int info, int length) {
        String result = info + "";
        char[] bytes = new char[length];
        result.getChars(0, result.length(), bytes, length - result.length());
        return bytes;
    }
    public static byte[] integerToBytes(int x, int len) {
        return ByteBuffer.allocate(len).order(ByteOrder.BIG_ENDIAN).putInt(x).array();
    }

    public static int bytesToInteger(byte[] bytes) {
        ByteBuffer wrapped = ByteBuffer.wrap(bytes);
        return wrapped.getInt();
    }
    public static String socketReader(InputStream in,byte[] header) throws IOException {
        int readCount=in.read(header,0, 4);
        int totalLength=bytesToInteger(header);
        if (totalLength >= 10000000) {
            return "2,4,";
        }
        byte[] content=new byte[totalLength];
        int totalReadCount = 0;
        while (totalLength > 0) {
            readCount = in.read(content, totalReadCount, totalLength);
            totalReadCount += readCount;
            totalLength = totalLength - readCount;
        }
       String strRequest = new String(content);
        /*if(TextUtils.isEmpty(strRequest)){*/
        if(strRequest==null || strRequest.length()==0){
            strRequest=null;
        }
        return strRequest;
    }
}
