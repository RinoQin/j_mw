package com.mwee.android.test;

import com.mwee.android.drivenbus.DriverBus;
import com.mwee.android.drivenbus.util.Log;

public class TestDrivenBus {

    public static void main(String[] args){
        String[] pathList = {"com.mwee.android.test.Demo","com.mwee.android.test.LalaDemo"};
        DriverBus.init(pathList);
        DriverBus.call("Demo/setDemoName","欢迎来到demo");
        String name = DriverBus.call("Demo/getDemoName");

        String lalaCall = DriverBus.call("lala/getMyCall");
        Log.i("lalacall is : "+lalaCall);
        Log.i("DemoName is : "+name);


        Demo demo = DriverBus.call("Demo/getDemo");
        DriverBus.call("Demo/setDemoValue","无所畏惧");

        DriverBus.call("lala/lala",demo);

        String nameLala = DriverBus.call("Demo/getDemoName");
        String valueLala = DriverBus.call("Demo/getDemoValue");

        Log.i("DemoName lala is : "+nameLala);
        Log.i("DemoValue lala is : "+valueLala);


        Demo demoInstance = DriverBus.call("Demo/getInstance");

        DriverBus.call("lala/lala",demoInstance);

        String nameLalaInstance = DriverBus.call("Demo/getDemoName");
        String valueLalaInstance = DriverBus.call("Demo/getDemoValue");

        Log.i("DemoName lalaInstance is : "+nameLalaInstance);
        Log.i("DemoValue lalaInstance is : "+valueLalaInstance);
    }
}
