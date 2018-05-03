package com.mwee.android.drivenbus;

/*import android.util.Log;*/
import com.mwee.android.drivenbus.util.Log;

import com.mwee.android.drivenbus.exception.DriverBusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * DriverCalled
 * Created by virgil on 16/8/4.
 */
@SuppressWarnings("unchecked")
class DriverCalled {
    protected static boolean errorWithException = true;

    /**
     * called by other module
     * if your dest method runs on different thread,you shall send AsyncDrivenCallBack;
     *
     * @param uri       String
     * @param paramList List<Object> | recommend use only one param,use json string if param is complex
     * @return Object
     */
    protected static <T> T beCalled(IDriver driver, Method method, String uri, Object... paramList) throws DriverBusException {
        try {
            if (method == null) {
                Log.e("DriverBus", "No method " + uri);
                return null;
            }
            if (paramList != null && paramList.length > 0) {
                return (T) method.invoke(driver, paramList);
            } else {
                return (T) method.invoke(driver);
            }
        } catch (IllegalAccessException e) {
            if (errorWithException) {
                throw new DriverBusException(e);
            } else {
                e.printStackTrace();
            }
        } catch (InvocationTargetException e) {
            if (errorWithException) {
                throw new DriverBusException(e.getCause());
            } else {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            if (errorWithException) {
                throw new DriverBusException(e);
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }
}
