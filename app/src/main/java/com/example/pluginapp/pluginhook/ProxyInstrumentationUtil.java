package com.example.pluginapp.pluginhook;

import android.app.Activity;
import android.app.Instrumentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProxyInstrumentationUtil {

    public static void replaceInstrumentation(Activity activity) {
        Class<Activity> clazz = Activity.class;
        //通过反射拿到名为mInstrumentation的字段
        try {
            Field mInstrumentationFiled = clazz.getDeclaredField("mInstrumentation");
            mInstrumentationFiled.setAccessible(true);
            //获取原Instrumentation实例
            Instrumentation mInstrumentation = (Instrumentation) mInstrumentationFiled.get(activity);
            //创建代理Instrumentation实例
            ProxyInstrumentation proxyInstrumentation = new ProxyInstrumentation(mInstrumentation);
            //替换代理实例proxyInstrumentation
            mInstrumentationFiled.set(activity, proxyInstrumentation);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }


    public static void attachContext() {
        try {
            //获取ActivityThread的实例
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            //获取ActivityThread实例
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            //获取原始的Instrumentation实例
            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);
            //创建代理ProxyInstrumentation实例
            ProxyInstrumentation proxyInstrumentation = new ProxyInstrumentation(instrumentation);
            mInstrumentationField.set(currentActivityThread, proxyInstrumentation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
