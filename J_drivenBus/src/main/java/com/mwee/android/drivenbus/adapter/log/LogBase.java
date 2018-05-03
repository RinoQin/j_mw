package com.mwee.android.drivenbus.adapter.log;

/**
 * 规定一些使用logUtil必须实现的方法
 */
public class LogBase {

    public static  void e(String msg){};

    public static void e(String... msg) {};

    public static void e(String msg,Throwable t) {};

    public static void i(String msg) {};

    public static void i(String... msg) {};

    public static void d(String msg) {};

    public static void d(String... msg) {};
}
