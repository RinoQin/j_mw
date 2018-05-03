package com.mwee.android.log.adapter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SlfLogUtil extends LogBase {



    static{
        System.setProperty("org.slf4j.simpleLogger.showDateTime","true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat","yyyy-MM-dd HH:mm:ss.SSS");

        String environment = null==System.getProperty("environment")?
                System.getenv("environment"):System.getProperty("environment");
        if(null!=environment && "dev".equals(environment)){
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel",LEVEL.DEBUG.name());
        }else if(null!=environment && "prd".equals(environment)){
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel",LEVEL.ERROR.name());
        }else{
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel",LEVEL.INFO.name());
        }

    }

    /**
     * 是否是生产包
     */
    private static boolean isRelase = false;
    /**
     * 是否需要打印日志
     */
    public static boolean showLog = true;

    public static final String LOG_NAME = "AlpLog";

    private static Logger logger= LoggerFactory.getLogger(LOG_NAME);

    public static void setLogger(Class clazz){

        logger = LoggerFactory.getLogger(clazz);

    }

    public static void setLogger(String name){
        logger = LoggerFactory.getLogger(name);
    }

    private static final int stackTrace_index = 3;

    /**
     * 设置当前环境
     *
     * @param release boolean | true:生产包;false:非生产包
     */
    public static void setRelease(boolean release) {
        isRelase = release;
        showLog = !isRelase;
    }

    public static void e(String msg) {
        if (showLog) {
            String methodName = getLogginMethod();
            logger.error(methodName+msg);

        }
    }

    public static void e(String... msg) {
        if (showLog) {
            String methodName = getLogginMethod();
            logger.error(methodName+Arrays.toString(msg));

        }
    }

    public static void e(String msg,Throwable t) {
        if (showLog) {
            String methodName = getLogginMethod();
            logger.error(methodName+msg,t);
        }
    }

    public static void i(String msg) {
        if (showLog) {
            String methodName = getLogginMethod();
            logger.info(methodName+msg);
        }
    }
    public static void i(String... msg) {
        if (showLog) {
            String methodName = getLogginMethod();
            logger.info(methodName+ Arrays.toString(msg));
        }
    }

    public static void d(String msg) {
        if (showLog) {
            String methodName = getLogginMethod();
            logger.debug(methodName+msg);
        }
    }

    public static void d(String... msg) {
        if (showLog) {
            String methodName = getLogginMethod();
            logger.debug(methodName+Arrays.toString(msg));
        }
    }

    /**
     * 通过当前线程对象堆栈获取调用者来setLogger
     * @return 调用者方法名methodName
     * TODO 这里通过堆栈信息获得调用者信息的方式并不可靠
     */
    private static String  getLogginMethod(){
        StackTraceElement[] tarck = Thread.currentThread().getStackTrace();
        String methodName =  null;
//        for(int i=0;i<tarck.length;i++){
//            logger.info(tarck[i].getMethodName()+" : "+tarck[i].getClassName());
//        }
        if(tarck!=null && tarck.length>(stackTrace_index+1)) {
            methodName = tarck[stackTrace_index].getMethodName();
            String clazzName = tarck[stackTrace_index].getClassName();
            setLogger(clazzName);
        }
        return methodName==null?"":methodName+"  ";
    }


    //打日志文件


    public enum LEVEL{
        DEBUG,
        INFO,
        ERROR;

    }

}
