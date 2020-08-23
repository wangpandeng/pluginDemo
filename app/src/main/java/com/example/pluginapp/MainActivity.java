package com.example.pluginapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.pluginapp.hook.AppInstrumentation;
import com.example.pluginapp.hook.PluginContext;
import com.example.pluginapp.pluginhook.HookTestActivity;
import com.example.pluginapp.pluginhook.ProxyInstrumentationUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dalvik.system.DexClassLoader;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    //    private String nativeLibPath;
    private String pluginDexOutPath;
    private String pluginActivityName;
    public static DexClassLoader myDexClassLoader;
    private String pluginApkPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 100);
        View textView1 = findViewById(R.id.textView1);
        View textView2 = findViewById(R.id.textView2);
        View textView3 = findViewById(R.id.textView3);
        View textView4 = findViewById(R.id.textView4);
        View textView5 = findViewById(R.id.textView5);

//        textView1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                StubActivity.startPluginActivity(MainActivity.this, pluginApkPath, pluginActivityName);
//            }
//        });
//        textView2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                StubInterfaceMainActivity.startPluginActivity(MainActivity.this, pluginApkPath, pluginActivityName);
//            }
//        });

        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProxyInstrumentationUtil.replaceInstrumentation(MainActivity.this);
                startActivity(new Intent(MainActivity.this, HookTestActivity.class));
            }
        });
        textView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProxyInstrumentationUtil.attachContext();
                startActivity(new Intent(MainActivity.this, HookTestActivity.class));
            }
        });

        textView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, myDexClassLoader.loadClass(pluginActivityName));
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void init() {
        pluginApkPath = getPluginApkPath();
        pluginActivityName = "com.example.plugin.PluginActivity";
//        //获取pluginlib地址
//        nativeLibPath = new File(getFilesDir(), "pluginlib").getAbsolutePath();
//        log("nativeLibPath", nativeLibPath);
        //优化后的odex地址
        File dexOutfile = new File(getFilesDir(), "dexout");
        pluginDexOutPath = dexOutfile.getAbsolutePath();
        myDexClassLoader = new DexClassLoader(pluginApkPath, pluginDexOutPath, null, getClassLoader());

        //注册反射
        PluginContext pluginContext = new PluginContext(pluginApkPath, this, getApplication(), myDexClassLoader);
        AppInstrumentation.inject(this, pluginContext);
    }


    public void log(String tag, String string) {
        Log.i("pluginTag====" + tag, string);
    }

    private String getPluginApkPath() {
        String pluginApkName = "plugin.apk";
        OutputStream outputStream = null;
        InputStream inputStream = null;
        File file = new File(getExternalCacheDir(), pluginApkName);
//        if (file.exists()) {
//            return file.getAbsolutePath();
//        }
        try {
            outputStream = new FileOutputStream(file);
            inputStream = getAssets().open(pluginApkName);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log("pluginapk", "写入失败");
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                log("pluginapk", "写入成功");
            } catch (Exception e) {
                e.printStackTrace();
                log("pluginapk", "关闭失败");
            }

        }
        return file.getAbsolutePath();
    }
}