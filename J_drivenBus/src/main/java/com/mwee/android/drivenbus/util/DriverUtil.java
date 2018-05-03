package com.mwee.android.drivenbus.util;

/*import android.text.TextUtils;*/

import com.mwee.android.drivenbus.IDriver;
import com.mwee.android.drivenbus.component.DrivenMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * DriverUtil
 * Created by virgil on 16/8/4.
 */
public class DriverUtil {

    /**
     * 将Driver的方法添加索引
     *
     * @param driver IDriver
     * @return HashMap<String, Method>
     */
    public static HashMap<String, Method> indexDriver(IDriver driver) {
        HashMap<String, Method> methodInfo = new HashMap<>();
        Method[] methods = driver.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Annotation annotation = method.getAnnotation(DrivenMethod.class);
            if (annotation != null) {
                String uri = ((DrivenMethod) annotation).uri();
                /*if (!TextUtils.isEmpty(uri)) {*/
                if(uri!=null && uri.length()>0){
                    method.setAccessible(true);
                    methodInfo.put(uri, method);
                }
            }
        }
        return methodInfo;
    }
}
