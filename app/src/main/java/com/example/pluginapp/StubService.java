package com.example.pluginapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StubService extends Service {
    public StubService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
