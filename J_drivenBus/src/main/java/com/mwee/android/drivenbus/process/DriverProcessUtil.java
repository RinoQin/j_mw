package com.mwee.android.drivenbus.process;

import java.util.List;
/*import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;*/

/**
 * Created by virgil on 2017/1/20.
 */

class DriverProcessUtil {

    /**
     * 当前进程是否是应用的主进程.
     *
     * @param //context Context
     * @return boolean | true:是主进程;false:不是主进程(例如:推送进程)
     */
    //TODO
    /*protected static boolean isMainProcess(Context context) {

        if (context == null) {
            return false;
        }
        final String packageName = context.getPackageName();

        *//*return TextUtils.equals(packageName, getCurrentProcessName(context));*//*
        String currentProcessName =  getCurrentProcessName(context);
        return packageName!=null && currentProcessName!=null && packageName.equals(currentProcessName);

    }*/

    protected static boolean isMainProcess(Object object) {
        //不可靠
        if(Thread.currentThread().getName().equals("main")){
            return true;
        }else{
            return false;
        }

    }

    /**
     * 获取当前进程的包名
     *
     * @param context Context
     * @return String | example:com.google.youtube
     */
    //TODO
    /*protected static String getCurrentProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> proList = am.getRunningAppProcesses();
        final String packageName = context.getPackageName();
        int currentPID = android.os.Process.myPid();
        String currentProcess = "";
        for (ActivityManager.RunningAppProcessInfo temp : proList) {
            if (temp.pid == currentPID) {
                currentProcess = temp.processName;
                break;
            }
        }
        return currentProcess;
    }*/
    protected static String getCurrentProcessName(Object context) {
        String currentProcess = "";
        return currentProcess;
    }
}
