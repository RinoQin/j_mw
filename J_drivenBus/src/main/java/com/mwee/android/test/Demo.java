package com.mwee.android.test;


import com.mwee.android.drivenbus.Driver;
import com.mwee.android.drivenbus.component.DrivenMethod;

public class Demo extends Driver {

    private static Demo demo = null;

    @DrivenMethod(uri = TAG+"/getDemo")
    public Demo getDemo(){
        return this;
    }

    @DrivenMethod(uri = TAG+"/getInstance")
    public static Demo getInstance(){
        /*if (demo != null) {
            return demo;
        } else {
            synchronized (Demo.class) {
                if(demo==null) {*/
                    demo = new Demo();
                /*}*/
                return demo;
            /*}
        }*/
    }

    private final static String TAG = "Demo";

    @Override
    public String getModuleName() {
        return TAG;
    }

    private String demoName;

    private String demoValue;

    public Demo(){
        super();
    }

    @DrivenMethod(uri = TAG+"/getDemoName")
    public String getDemoName() {
        return demoName;
    }
    @DrivenMethod(uri = TAG+"/setDemoName")
    public void setDemoName(String demoName) {
        this.demoName = demoName;
    }
    @DrivenMethod(uri = TAG+"/getDemoValue")
    public String getDemoValue() {
        return demoValue;
    }
    @DrivenMethod(uri = TAG+"/setDemoValue")
    public void setDemoValue(String demoValue) {
        this.demoValue = demoValue;
    }
}
