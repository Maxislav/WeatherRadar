package com.atlas.mars.weatherradar.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.atlas.mars.weatherradar.DataBaseHelper;
import com.atlas.mars.weatherradar.MainActivity;
import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.Rest.DayForecastRain;
import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mars on 9/22/15.
 */
public class MorningService extends Service implements OnLocation, DayForecastRain.Callbackwqw {
    private static final String TAG = "MorningServiceLogs";
    private LocationManager locationManagerNet;
    private LocationListener locationListenerNet;
    DataBaseHelper db;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        db = new DataBaseHelper(this);
        super.onCreate();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }
    public void onDestroy() {
        super.onDestroy();
    }

    private void   someTask(){
        if (isNetworkAvailable()){
            locationManagerNet = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationListenerNet = new MyLocationListenerNet(this);
            if(locationManagerNet.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)){
                locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
            }else{
                new DayForecastRain(this);
            }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public void onLocationAccept(double lat, double lng) {
        if (locationManagerNet != null) {
            locationManagerNet.removeUpdates(locationListenerNet);
            locationManagerNet = null;
        }
        new DayForecastRain(this, lat, lng);
    }

    @Override
    public void Success(List<HashMap> list, String name) {
        if(list==null) {
            this.stopSelf();
            return;
        }
        Calendar calendarCur = Calendar.getInstance();
        calendarCur.setTimeInMillis(System.currentTimeMillis());

        int dateOfMonthCur = calendarCur.get(Calendar.DAY_OF_MONTH);


        for (HashMap<String, Object> map : list){
            Long dt = (Long)(map.get("dt"));
            String main = (String)map.get("main");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dt*1000);

            int dateOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            if(dateOfMonth==dateOfMonthCur){
                java.util.Date now = calendar.getTime();
                java.sql.Timestamp timestamp = new java.sql.Timestamp(now.getTime());
                if(main.equals("Rain") && now.after(calendarCur.getTime())){
                    String hh = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
                    notificationCreate(hh, name);
                    Log.d(TAG,"///////////");
                    break;
                }
                Log.d(TAG, timestamp+" : " +main);
            }
        }

        /*if(map==null) {
            this.stopSelf();
            return;
        }
        for (Map.Entry entry : map.entrySet()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((Long)entry.getKey()*1000);
            //Date date =  (Long)entry.getKey()*1000;
            java.util.Date now = calendar.getTime();
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

            Log.d(TAG, currentTimestamp +" " +entry.getValue());
           *//* System.out.println("Key: " + entry.getKey() + " Value: "
                    + entry.getValue());*//*
        }*/
        this.stopSelf();
    }

    void notificationCreate(String HH, String city){
       // String contentText = "Возможен дождь в "+HH+"ч";

       // CharSequence cs = contentText;

        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.notification_morning);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("time", HH+"ч");
        notificationIntent.putExtra("were_from", "morning_service");

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this).setContentTitle("Rain alarm")
                .setContentText(city+". Возможен дождь в "+HH+"ч")
                //.setContent(remoteViews)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.notification_rain)
                .build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
       // remoteViews.setTextViewText(R.id.contentT,cs);

      /*  Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("item_id", "10052");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = intent;*/
        nm.notify(2, notification);

    }
}


