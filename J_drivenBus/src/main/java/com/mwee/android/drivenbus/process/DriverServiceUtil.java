package com.mwee.android.drivenbus.process;

import com.mwee.android.drivenbus.util.Log;

import com.mwee.android.drivenbus.DrivenBusManager;
import com.mwee.android.drivenbus.exception.DriverBusException;

/*import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;
import android.util.Log;*/

/**
 * Created by virgil on 2017/1/22.
 */

public class DriverServiceUtil {


    /**
     * 获取当前进程注册的Service的class Name
     *
     * @param //context Context
     * @return String
     */
    //TODO
    /*public static String getService(Context context) {
        return getServiceByProcessName(context, DriverProcessUtil.getCurrentProcessName(context));
    }*/
    public static String getService() {
        Thread.currentThread().getStackTrace();
        return "";
    }

    /**
     * 获取指定进程的ClassName
     *
     * @param context
     * @param processName
     * @return
     */
    //TODO
    /*public static String getServiceByProcessName(Context context, String processName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
            ServiceInfo[] service = info.services;
            for (ServiceInfo temp : service) {
                Log.d("DriverServiceUtil", temp.packageName);
                *//*if (TextUtils.equals(temp.processName, processName)) {*//*
                if(processName!=null && temp.processName!=null && processName.equals(temp.processName)){
                    return temp.name;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }*/
    public static String getServiceByProcessName(Object context, String processName) {
        return "";
    }

    /**
     * called by other module;
     * if your dest method runs on different thread,you shall send AsyncDrivenCallBack;
     * We will check all your param and throw some exception when error occurred
     *
     * @param uri    String
     * @param params Object... | recommend use only one param,use json string if param is complex
     * @return Object
     * @throws DriverBusException
     */
    public static <T> T callInProcess(String processName, String uri, Object... params) throws DriverBusException {
        return DrivenBusManager.getInstance().callInProcess(processName, uri, params);
    }

    public static <T> T callInProcess(String processName, String uri) throws DriverBusException {
        return DrivenBusManager.getInstance().callInProcess(processName, uri);
    }

    /**
     * call all driver with method
     *
     * @param method String
     * @param params List<Object>
     * @throws DriverBusException
     */
    public static void broadcastInProcess(String processName, String method, Object... params) throws DriverBusException {
        DrivenBusManager.getInstance().broadcast(method, params);
    }

}
