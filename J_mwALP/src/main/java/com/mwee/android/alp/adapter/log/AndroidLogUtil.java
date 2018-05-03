package com.mwee.android.alp.adapter.log;

/*import android.util.Log;

import java.util.Arrays;*/

public class AndroidLogUtil extends LogBase{

    /**
     * 是否是生产包
     */
    private static boolean isRelase = false;
    /**
     * 是否需要打印日志
     */
    public static boolean showLog = true;


    /**
     * 设置当前环境
     *
     * @param release boolean | true:生产包;false:非生产包
     */
    public static void setRelease(boolean release) {
        isRelase = release;
        showLog = !isRelase;
    }

   /* public static void e(String msg) {
        if (showLog) {
            Log.e("AlpLog", msg);
        }
    }

    public static void e(String msg,Throwable t) {
        if (showLog) {
            Log.e("AlpLog", msg,t);
        }
    }

    public static void i(String msg) {
        if (showLog) {
            Log.i("AlpLog", msg);
        }
    }
    public static void i(String... msg) {
        if (showLog) {
            Log.i("AlpLog", Arrays.toString(msg));
        }
    }



    public static void d(String msg) {
        if (showLog) {
            Log.d("AlpLog", msg);
                    }
    }

    public static void d(String... msg) {
        if (showLog) {
            Log.d("AlpLog", Arrays.toString(msg));
        }
    }*/

}
