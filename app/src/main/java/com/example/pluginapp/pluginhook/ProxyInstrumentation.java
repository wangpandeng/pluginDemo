package com.example.pluginapp.pluginhook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

//代理Instrumentation类ProxyInstrumentation
public class ProxyInstrumentation extends Instrumentation {
    private String TAG = "ActivityProxyInstrumentation";

    private Instrumentation mBase;

    public ProxyInstrumentation(Instrumentation mBase) {
        this.mBase = mBase;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return super.newActivity(cl, className, intent);
    }

    //内部调用Instrumentation的execStartActivity
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        // Hook之前, 可以输出你想要的!
        Log.d(TAG, "xxxx: 执行了startActivity, 参数如下: " + "who = [" + who + "], " +
                "contextThread = [" + contextThread + "], token = [" + token + "], " +
                "target = [" + target + "], intent = [" + intent +
                "], requestCode = [" + requestCode + "], options = [" + options + "]");

        try {
            Method execStartActivity = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);
            return (ActivityResult) execStartActivity.invoke(mBase, who,
                    contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
            return null;
        }
    }
}



