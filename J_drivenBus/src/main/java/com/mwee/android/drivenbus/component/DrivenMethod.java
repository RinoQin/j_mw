package com.mwee.android.drivenbus.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DrivenMethod
 * Created by virgil on 16-5-15.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DrivenMethod {
    String uri() default "";
    boolean UIThread() default false;
}
