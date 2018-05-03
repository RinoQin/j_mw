package com.mwee.android.drivenbus;

/*import android.content.Context;
import android.content.Intent;*/

import com.mwee.android.drivenbus.exception.DriverBusException;
import com.mwee.android.drivenbus.process.DriverServiceUtil;

/**
 * DriverBus
 * Created by virgil on 16/8/4.
 */
public class DriverBus {


    /**
     * register  one driver
     *
     * @param driver IDriver
     */
    public static void registerDriver(IDriver driver) {
        DrivenBusManager.getInstance().registerDriver(driver);
    }

    /**
     * unrgister one driver
     *
     * @param driver IDriver
     */
    public static void unRegisterDriver(IDriver driver) {
        DrivenBusManager.getInstance().unRegisterDriver(driver);
    }

    //TODO
    /*public static void prepareMultiProcess(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String className = DriverServiceUtil.getService(context);
                    Intent intent = new Intent();
                    intent.setClassName(context, className);
                    intent.setPackage(context.getPackageName());
                    context.startService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }*/
    public static void prepareMultiProcess(final Object context) {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    /**
     * Init drivers ;
     * You shall call this method when you want to load drivers by path when you use multidex instant run
     *
     * @param pathList List<String> pathList
     * @throws DriverBusException
     */
    public static void init(String[] pathList) throws DriverBusException {
        DrivenBusManager.getInstance().init(pathList);
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
    public static <T> T call(String uri, Object... params) throws DriverBusException {
        return DrivenBusManager.getInstance().call(uri, params);
    }

    public static <T> T call(String uri) throws DriverBusException {
        return DrivenBusManager.getInstance().call(uri);
    }

    /**
     * call all driver with method
     *
     * @param method String
     * @param params List<Object>
     * @throws DriverBusException
     */
    public static void broadcast(String method, Object... params) throws DriverBusException {
        DrivenBusManager.getInstance().broadcast(method, params);
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
        DrivenBusManager.getInstance().broadcastInProcess(processName,method, params);
    }

    /**
     * throw exception when caught exception
     *
     * @param errorWithException boolean
     */
    public static void setErrorWithException(boolean errorWithException) {
        DriverCalled.errorWithException = errorWithException;
    }
}
