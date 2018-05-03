package com.mwee.android.drivenbus;

/**
 * 一个主线程的调度器
 *
 * @author virgil
 */
public class MainThreadWorker {
    private static MainThread thread = null;

    static {
        thread = new MainThread();
    }

    public static boolean isMainThread() {
//        return Looper.getMainLooper() == Looper.myLooper() ;
        //不可靠
        if(Thread.currentThread().getName().equals("main")){
            return true;
        }else{
            return false;
        }
    }

    public static void postOnMainThead(Runnable runnable) {
        thread.addJob(runnable);
    }

}
