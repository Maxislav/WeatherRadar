package com.atlas.mars.weatherradar.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.atlas.mars.weatherradar.MainActivity;

/**
 * Created by mars on 3/31/16.
 */
public class RegenBorispolBroadCast  extends BroadcastReceiver {
    final String TAG = "RegenBorispolBroadCastLogs";
    @Override
    public void onReceive(Context context, Intent intent) {
        context.getPackageName();

        Intent updIntent = new Intent();
        updIntent.setAction(MainActivity.LOCATION);
        updIntent.putExtra("regenBorispol", true);
        context.sendBroadcast(updIntent);

        //context.sendBroadcast(new Intent("INTERNET_LOST"));


        Log.d(TAG, "INTERNET_LOST");
       // ((MainActivity)getActivity()).printSomething();

    }

    MainActivity main = null;
    void setMainActivityHandler(MainActivity main){
        this.main=main;
    }
}
