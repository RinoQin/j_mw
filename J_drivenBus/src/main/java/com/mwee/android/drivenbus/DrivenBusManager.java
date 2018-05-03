package com.mwee.android.drivenbus;

/*
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
*/


import com.mwee.android.drivenbus.util.Log;

import com.mwee.android.drivenbus.component.DrivenMethod;
import com.mwee.android.drivenbus.exception.DrivenException;
import com.mwee.android.drivenbus.exception.DriverBusException;
import com.mwee.android.drivenbus.exception.IncorrectDriverException;
import com.mwee.android.drivenbus.exception.NoDriverException;
import com.mwee.android.drivenbus.process.DriverServiceUtil;
import com.mwee.android.drivenbus.util.DriverUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * DrivenBusManager
 * Created by virgil on 16-5-15.
 */
@SuppressWarnings("unused")
public class DrivenBusManager implements IDriveProcessor {
    private final static DrivenBusManager instance = new DrivenBusManager();
    private HashMap<String, IDriver> driverMap = new HashMap<>();
    private HashMap<String, HashMap<String, Method>> driverMethod = new HashMap<>();

    private String mPackageName;
    private IDriveProcessor multiProcessor;

    private DrivenBusManager() {
    }

    public static DrivenBusManager getInstance() {
        return instance;
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
    public <T> T callInProcess(String processName, String uri, Object... params) throws DriverBusException {
        if (multiProcessor == null) {
            Log.e("Driver", "You shall call DriverBus.prepareMultiProcess(Context) first");
            return null;
        }
        return multiProcessor.call(uri, params);
    }

    public <T> T callInProcess(String processName, String uri) throws DriverBusException {
        if (multiProcessor == null) {
            Log.e("Driver", "You shall call DriverBus.prepareMultiProcess(Context) first");
            return null;
        }
        return multiProcessor.call(uri);
    }

    /**
     * call all driver with method
     *
     * @param method String
     * @param params List<Object>
     * @throws DriverBusException
     */
    public void broadcastInProcess(String processName, String method, Object... params) throws DriverBusException {
        if (multiProcessor == null) {
            Log.e("Driver", "You shall call DriverBus.prepareMultiProcess(Context) first");
        }
        multiProcessor.broadcast(method, params);
    }

    /**
     * call all driver with method
     *
     * @param method String
     * @param params List<Object>
     * @throws DriverBusException
     */
    @Override
    public void broadcast(String method, Object... params) {
        if (driverMap != null && !driverMap.isEmpty()) {
            Set<String> set1 = driverMap.keySet();
            Set<String> set2 = new HashSet<>();
            set2.addAll(set1);
            for (String moduleName : set2) {
                IDriver driver = driverMap.get(moduleName);
                if (driver != null) {
                    beCalled(moduleName + "/" + method, false, params);
                }
            }
        }
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
    @Override
    public <T> T call(String uri, Object... params) throws DriverBusException {
        checkBeforeCall(uri);
        return beCalled(uri, params);
    }

    public void registerDriver(IDriver driver) {
        String result = verifyDriver(driver);
        /*if (TextUtils.isEmpty(result)) {*/
        if(result==null || result.length()==0){
            driverMap.put(driver.getModuleName(), driver);
            driverMethod.put(driver.getModuleName(), DriverUtil.indexDriver(driver));
        } else {
            Log.e("DrivenBusManager", "registerDriver fail:" + result);
        }
    }

    public void unRegisterDriver(IDriver driver) {
        String result = verifyDriver(driver);
        /*if (TextUtils.isEmpty(result)) {*/
        if(result==null || result.length()==0){
            IDriver cacheDriver=driverMap.get(driver.getModuleName());
            if (cacheDriver == driver) {
                driverMap.remove(driver.getModuleName());
                driverMethod.remove(driver.getModuleName());
            }
        } else {
            Log.e("DrivenBusManager", "unregisterDriver fail:" + result);
        }
    }

    /**
     * Init drivers ;
     * You shall call this method when you want to load drivers by path when you use multidex instant run
     *
     * @param pathList List<String> pathList
     * @throws DriverBusException
     */
    public void init(String[] pathList) throws DriverBusException {
        if (pathList == null || pathList.length <= 0) {
            throw new DrivenException("Empty pathList");
        } else {
            List<String> nameList = new ArrayList<>();
            Collections.addAll(nameList, pathList);
            loadDrivers(nameList);
        }
    }

    private <T> T beCalled(String uri, Object... paramList) {
        return beCalled(uri, true, paramList);
    }

    /**
     * call driver。
     *
     * @param uri            String
     * @param throwException boolean | throw exception whether if driver does not has this method
     * @param paramList      Object...
     * @return Object
     */
    private <T> T beCalled(final String uri, final boolean throwException, final Object... paramList) {
        String moduleName = getModuleNameByUri(uri);
        final IDriver driver = driverMap.get(moduleName);

        if (driver == null) {
            if (throwException) {
                if (DriverCalled.errorWithException) {
                    throw new NoDriverException(moduleName + " does not exist");
                } else {
                    new NoDriverException(moduleName + " does not exist").printStackTrace();
                }
            }
        } else {
            HashMap<String, Method> methodMap = driverMethod.get(moduleName);
            if (methodMap != null) {
                final Method method = methodMap.get(uri);
                if (method != null) {

                    if (needUiThread(method)) {
                        //TODO
                        /*new Handler(Looper.getMainLooper()).post(new Runnable() {*/
                        final ArrayBlockingQueue<T> resultQueue = new ArrayBlockingQueue<>(1);
                        MainThreadWorker.postOnMainThead(new Runnable() {
                            @Override
                            public void run() {
                                {
                                    T result = DriverCalled.beCalled(driver, method, uri, paramList);
                                    if (method.getReturnType() != Void.class) {
                                        try {
                                            resultQueue.put(result);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                        if (method.getReturnType() != Void.class) {
                            try {
                                return resultQueue.take();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    } else {
                        return DriverCalled.beCalled(driver, method, uri, paramList);
                    }
                } else {
                    if (throwException) {
                        if (DriverCalled.errorWithException) {
                            throw new NoDriverException(uri + " does not exist");
                        } else {
                            new NoDriverException(uri + " does not exist").printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean needUiThread(Method method) {
        if (method == null) {
            return false;
        }
        /*if (Looper.getMainLooper() != Looper.myLooper()) {*/
        if (!MainThreadWorker.isMainThread()) {
            DrivenMethod drivenMethod = method.getAnnotation(DrivenMethod.class);
            if (drivenMethod.UIThread()) {
                return true;
            }
        }
        return false;
    }

    /**
     * load drivers by full package path
     *
     * @param pathList List<String>
     */
    private void loadDrivers(List<String> pathList) {
        for (String temp : pathList) {
            /*if (!TextUtils.isEmpty(temp)) {*/
            if(temp!=null && temp.length()>0){
                try {
                    Object clzObject = Class.forName(temp).newInstance();
                    if (clzObject instanceof IDriver) {
                        registerDriver((IDriver) clzObject);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * check env and params before call
     *
     * @param uri String
     */
    private void checkBeforeCall(String uri) throws DriverBusException {
        String moduleName = getModuleNameByUri(uri);
        /*if (TextUtils.isEmpty(moduleName)) {*/
        if(moduleName==null || moduleName.length()==0){
            throw new IncorrectDriverException("Empty moduleName");
        }
    }

    /**
     * Get module from uri.
     * Uri should be formatted like:module/method
     *
     * @param uri String
     * @return String
     */
    private String getModuleNameByUri(String uri) {
        /*if (TextUtils.isEmpty(uri)) {*/
        if(uri==null || uri.length()==0){
            return "";
        }
        String moduleName = "";
        String[] temp = uri.split("/");
        if (temp.length > 1) {
            moduleName = temp[0];
        }
        return moduleName;
    }

    /**
     * Verify this driver
     *
     * @param driver Driver
     * @return String | error info,empty if the driver is in law
     */
    private String verifyDriver(IDriver driver) {
        if (driver == null) {
            return "";
        }
        String errorInfo = "";
        /*if (TextUtils.isEmpty(driver.getModuleName())) {*/
        if(driver.getModuleName()==null || driver.getModuleName().length()==0){
            errorInfo = "driver.getModuleName() returns empty";
        } else {

        }
        return errorInfo;
    }

    private String currentProcessName = "";
    private String currentReceiverName = "";
    //TODO
    /*private Context context = null;*/
    private Object context = null;


    //TODO
    /*public void registerProcess(Context context, String currentProcessName, String currentReceiverName) {
        this.context = context;
        this.currentProcessName = currentProcessName;
        this.currentReceiverName = currentReceiverName;
    }*/
    public void registerProcess(Object context, String currentProcessName, String currentReceiverName) {
        this.context = context;
        this.currentProcessName = currentProcessName;
        this.currentReceiverName = currentReceiverName;
    }

    //TODO
    private void checkProcess(String process) {
        String className = DriverServiceUtil.getServiceByProcessName(context, process);
        connectProcess(context,className);
    }

    //TODO
    /*private void callProcess(String uri,Object... param){
        Message mes=Message.obtain();
        mes.obj=null;
        mes.setData(null);
        Bundle bundle=new Bundle();
        bundle.putString("uri",uri);
        for(int i=0;i<param.length;i++){
            Object temp=param[i];
            if(temp instanceof String){
                bundle.putString(String.valueOf(i),(String)temp);
            }
        }
    }*/
    private void callProcess(String uri,Object... param){

    }

    //TODO
    /*private void connectProcess(final Context context, final String className) {
        try {

            Intent intent = new Intent();
            intent.setClassName(context, className);
            intent.setPackage(context.getPackageName());
            context.bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Messenger messenger=new Messenger(service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            },Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    private void connectProcess(final Object context, final String className){

    }
}
