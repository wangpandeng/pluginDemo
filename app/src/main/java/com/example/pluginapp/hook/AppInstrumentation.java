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

    //第一步，Instrumentation代理实例替换MainThread和Activity类中的mInstrumentation
    public static void inject(Activity activity, PluginContext pluginContext) {
        //Reflect是反射工具
        Reflect reflect = Reflect.on(activity);
        Object mMainThread = reflect.get("mMainThread");
        Instrumentation mInstrumentation = Reflect.on(mMainThread).get("mInstrumentation");
        AppInstrumentation appInstrumentation = new AppInstrumentation(activity, mInstrumentation, pluginContext);
        Reflect.on(mMainThread).set("mInstrumentation", appInstrumentation);
        Reflect.on(activity).set("mInstrumentation", appInstrumentation);
    }

    //第二步，startActivity的时候最终会执行Instrumentation的execStartActivity
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
        //插件中的页面的跳转信息，真正要跳转的页面
        ComponentName oldComponent = intent.getComponent();
        if (component.getPackageName().equals(realContext.getPackageName())) {
            //设置站位Activity的信息
            intent.setComponent(component);
            //但是却把真正要跳转的component设置参数，也就是用StubActivity传递插件的页面数据
            intent.putExtra(KEY_COMPONENT, oldComponent);
        }
    }

    //第三步，创建Activity实例的调用该方法
    @Override
    public Activity newActivity(@Nullable ClassLoader cl, @Nullable String className, @Nullable Intent intent) {
        assert intent != null;
        ComponentName componentName = intent.getParcelableExtra(KEY_COMPONENT);
        try {
            if (componentName != null) {
                //获取要跳转的页面的全类名Class对象
                Class<?> clazz = pluginContext.getClassLoader().loadClass(componentName.getClassName());
                intent.setComponent(componentName);
                //创建新的Activity实例
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

    //加载资源resource的
    private void injectActivity(@Nullable Activity activity) {
        Intent intent = activity.getIntent();
        Context baseContext = activity.getBaseContext();
        try {
            Reflect.on(baseContext).set("mResources", pluginContext.getResources());
            Reflect.on(activity).set("mResources", pluginContext.getResources());
            Reflect.on(activity).set("mBase", pluginContext);
            Reflect.on(activity).set("mApplication", pluginContext.getApplicationContext());
            ComponentName componentName = intent.getParcelableExtra(KEY_COMPONENT);
            Intent wrapperIntent = new Intent(intent);
            if (componentName != null) {
                wrapperIntent.setClassName(componentName.getPackageName(), componentName.getClassName());
            }
        } catch (Exception e) {
        }
    }

}
