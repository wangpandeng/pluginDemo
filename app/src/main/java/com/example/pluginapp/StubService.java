package com.example.pluginapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;

public class StubService extends Service {
    public StubService() {
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
