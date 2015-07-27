package com.atlas.mars.weatherradar.alarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.atlas.mars.weatherradar.MainActivity;

/**
 * Created by mars on 7/27/15.
 */
public class MyService extends Service {
    final String LOG_TAG = "MyServiceLog";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        //locationManagerGps.removeUpdates(locationListenerGps);
    }

    void someTask() {
        Intent updIntent = new Intent();
        updIntent.setAction(MainActivity.LOCATION);
        updIntent.putExtra("distance", 5);
        sendBroadcast(updIntent);
        this.stopSelf();

    }
}
