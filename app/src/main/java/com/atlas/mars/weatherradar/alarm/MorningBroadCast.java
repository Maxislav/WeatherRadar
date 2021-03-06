package com.atlas.mars.weatherradar.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
    PendingIntent pendingIntent;
    @Override
    public void onReceive(Context context, Intent intent) {
        db = new DataBaseHelper(context);


      //  //pIntent2 =  PendingIntent.getBroadcast(this, 0, morningIntent, PendingIntent.FLAG_CANCEL_CURRENT );
        //alarmManagerMorning.cancel(pIntent2);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, db.getMorningWakeUp(), pendingIntent);
        context.startService(new Intent(context, MorningService.class));
    }
}
