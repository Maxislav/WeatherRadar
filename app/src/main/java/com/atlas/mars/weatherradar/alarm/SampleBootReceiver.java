package com.atlas.mars.weatherradar.alarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.atlas.mars.weatherradar.MainActivity;
import com.atlas.mars.weatherradar.R;

/**
 * Created by Администратор on 7/26/15.
 */
public class SampleBootReceiver extends BroadcastReceiver {
    final String LOG_TAG = "BootReceiverLogs";
    NotificationManager nm;
    @Override
    public void onReceive(Context context, Intent intent) {
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification = new Notification(R.drawable.logo, "Test", System.currentTimeMillis());

        Notification notification;
        notification = new Notification.Builder(context).setContentTitle("Rain alarm")
                .setContentText("Do something!")
                .setSmallIcon(R.drawable.logo)
                .build();

        Intent intentTL = new Intent(context, MainActivity.class);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        Intent updIntent = new Intent();
        updIntent.setAction(MainActivity.LOCATION);
        updIntent.putExtra("distance", 5);
        lbm.sendBroadcast(updIntent);
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        nm.notify(1, notification);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10*1000, pendingIntent);
        Log.d(LOG_TAG, "onReceive");

        context.startService(new Intent(context, MyService.class));

    }


}