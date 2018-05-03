package com.mwee.android.test;

import com.mwee.android.drivenbus.Driver;
import com.mwee.android.drivenbus.component.DrivenMethod;

public class LalaDemo extends Driver{

    private static String myCall;

    private static final String Tag = "lala";

    static{
        myCall = "我是LalaDemo，我为自己带盐!";
    }

    @DrivenMethod(uri = Tag+"/getMyCall")
    public static String getMyCall(){
        return myCall;
    }

    @DrivenMethod(uri = Tag+"/lala")
    public Demo lala(Demo demo){
        String demoName =  demo.getDemoName();
        String demoValue = demo.getDemoValue();
        demo.setDemoName(myCall+demoName);
        demo.setDemoValue(myCall+demoValue);
        return demo;
    }


    @Override
    public String getModuleName() {
        return Tag;
    }
}
