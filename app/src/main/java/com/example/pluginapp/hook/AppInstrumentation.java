package com.example.pluginapp.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;

//自定义Instrumentation代理实例
public class AppInstrumentation extends Instrumentation {
    public Context realContext;
    public static Instrumentation base;
    public PluginContext pluginContext;
    private String KEY_COMPONENT = "commontec_component";

    public AppInstrumentation(Context realContext, Instrumentation base, PluginContext pluginContext) {
        this.realContext = realContext;
        this.base = base;
        this.pluginContext = pluginContext;
    }

    //Instrumentation代理实例替换MainThread和Activity类中的mInstrumentation
    public static void inject(Activity activity, PluginContext pluginContext) {
        //Reflect是反射工具
        Reflect reflect = Reflect.on(activity);
        Object mMainThread = reflect.get("mMainThread");
        Instrumentation mInstrumentation = Reflect.on(mMainThread).get("mInstrumentation");
        AppInstrumentation appInstrumentation = new AppInstrumentation(activity, mInstrumentation, pluginContext);
        Reflect.on(mMainThread).set("mInstrumentation", appInstrumentation);
        Reflect.on(activity).set("mInstrumentation", appInstrumentation);
    }

    //创建Activity实例的调用该方法
    @Override
    public Activity newActivity(@Nullable ClassLoader cl, @Nullable String className, @Nullable Intent intent) {
        assert intent != null;
        ComponentName componentName = intent.getParcelableExtra(KEY_COMPONENT);
        try {
            if (componentName != null) {
                Class<?> clazz = pluginContext.getClassLoader().loadClass(componentName.getClassName());
                intent.setComponent(componentName);
                return (Activity) clazz.newInstance();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    //加载资源resource的
    private void injectActivity(@Nullable Activity activity) {
        Intent intent = activity.getIntent();
        Context base = activity.getBaseContext();
        try {
            Reflect.on(base).set("mResources", pluginContext.getResources());
            Reflect.on(activity).set("mResources", pluginContext.getResources());
            Reflect.on(activity).set("mBase", pluginContext);
            Reflect.on(activity).set("mApplication", pluginContext.getApplicationContext());
            // for native activity
            ComponentName componentName = intent.getParcelableExtra(KEY_COMPONENT);
            Intent wrapperIntent = new Intent(intent);
            if (componentName != null) {
                wrapperIntent.setClassName(componentName.getPackageName(), componentName.getClassName());
            }
        } catch (Exception e) {
        }
    }


    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        // 在这里进行资源的替换
        injectActivity(activity);
        super.callActivityOnCreate(activity, icicle);
    }


    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        // 在这里进行资源的替换
        injectActivity(activity);
        super.callActivityOnCreate(activity, icicle, persistentState);
    }

    //startActivity的时候最终会执行Instrumentation的execStartActivity
    public ActivityResult execStartActivity(Context who,
                                            IBinder contextThread,
                                            IBinder token,
                                            Activity target,
                                            Intent intent,
                                            int requestCode,
                                            Bundle options) {
        injectIntent(intent);
        return Reflect.on(base).call("execStartActivity", who, contextThread, token, target, intent, requestCode, options).get();
    }

    public void injectIntent(Intent intent) {
        //换马甲，component是在manifest中注册的站位Activity，
        ComponentName component = new ComponentName("com.example.pluginapp", "com.example.pluginapp.HookStubActivity");
       //插件中的页面的跳转信息
        ComponentName oldComponent = intent.getComponent();

        if (component.getPackageName().equals(realContext.getPackageName())) {
            intent.setComponent(component);
            intent.putExtra(KEY_COMPONENT, oldComponent);
        }
    }
}
