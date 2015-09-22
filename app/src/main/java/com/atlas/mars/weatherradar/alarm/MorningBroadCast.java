package com.atlas.mars.weatherradar.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.atlas.mars.weatherradar.DataBaseHelper;

/**
 * Created by mars on 9/22/15.
 */
public class MorningBroadCast extends BroadcastReceiver {
    final String TAG = "MorningBroadCast";
    DataBaseHelper db;
    @Override
    public void onReceive(Context context, Intent intent) {
        db = new DataBaseHelper(context);
        context.startService(new Intent(context, MorningService.class));
    }
}
