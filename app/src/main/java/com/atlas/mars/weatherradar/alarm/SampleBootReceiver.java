package com.atlas.mars.weatherradar.alarm;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.atlas.mars.weatherradar.DataBaseHelper;

import java.util.Date;

/**
 * Created by Администратор on 7/26/15.
 */
public class SampleBootReceiver extends BroadcastReceiver {
    final String TAG = "BootReceiverLogs";
    final String ALARM = "AlarmLogs";
    NotificationManager nm;
    DataBaseHelper db;
    PendingIntent pendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        db = new DataBaseHelper(context);
      /*  mapSetting = DataBaseHelper.mapSetting;
        int timeRepet = mapSetting.get(DataBaseHelper.TIME_REPEAT)!=null ? Integer.parseInt(mapSetting.get(DataBaseHelper.TIME_REPEAT)):2;
        long timeRepeatMilSec = 3600*1000*timeRepet;
        String timeNotify = mapSetting.get(DataBaseHelper.TIME_NOTIFY)!=null ? mapSetting.get(DataBaseHelper.TIME_NOTIFY) : null;

        DateFormat formatter = new SimpleDateFormat(NEW_FORMAT);
        Date dateNotify = null;
        try {
            if(timeNotify!=null){
                dateNotify = formatter.parse(timeNotify);
            }
        } catch (ParseException e) {
            Log.e(TAG,e.toString(),e);
            e.printStackTrace();
        }
        long dif = 0;
        if(dateNotify!=null){
            dif = System.currentTimeMillis() - dateNotify.getTime();
        }

        //long startAlarm;
        if(dif == 0){
            startAlarm = System.currentTimeMillis()+10*60*1000;
        }else if(dif<timeRepeatMilSec){
            startAlarm = System.currentTimeMillis()+(timeRepeatMilSec-dif);
        }else{
            startAlarm = System.currentTimeMillis()+10*60*1000;
        }*/


       // startAlarm = db.getStartTime();

      //  nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


      /*  Notification notification;
        notification = new Notification.Builder(context).setContentTitle("Rain alarm")
                .setContentText("Do something!")
                .setSmallIcon(R.drawable.logo)
                .build();

                  notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        nm.notify(1, notification);*/


      /*  Intent intentTL = new Intent(context, MainActivity.class);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        Intent updIntent = new Intent();
        updIntent.setAction(MainActivity.LOCATION);
        updIntent.putExtra("distance", 5);
        lbm.sendBroadcast(updIntent);*/





        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
      // am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10*60*1000, pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, db.getStartTime(), pendingIntent);
      // am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+10*1000, pendingIntent);


       // am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5*1000, -1, pendingIntent);
       // am.setRepeating(AlarmManager.RTC_WAKEUP, startAlarm, -1, pendingIntent);
        Log.d(TAG, "Next alarm" + new Date(db.getStartTime()).toString());
        Log.d(ALARM, "Alarm start: "+  new Date(db.getStartTime()).toString());
        context.startService(new Intent(context, MyService.class));
    }

    public void CancelAlarm(Context context) {

        Intent intent = new Intent(context, SampleBootReceiver.class);

        //PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent); // Отменяем будильник, связанный с интентом данного класса

    }

}