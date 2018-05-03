package com.mwee.android.drivenbus;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainThread extends Thread {
    private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private volatile boolean manualStop = false;

    public MainThread() {
        super();
    }

    @Override
    public void run() {
        super.run();
        doWork();
    }

    private void doWork() {
        try {
            work();
        } catch (InterruptedException e) {
            doWork();
        }
    }

    private void work() throws InterruptedException {
        while (!manualStop) {
            Runnable runnable = queue.poll(1, TimeUnit.HOURS);
            runnable.run();
        }
    }

    public void addJob(Runnable runnable) {
        try {
            queue.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
