package com.atlas.mars.weatherradar.alarm;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import com.atlas.mars.weatherradar.Cities;
import com.atlas.mars.weatherradar.DataBaseHelper;
import com.atlas.mars.weatherradar.MainActivity;
import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.Rest.ForecastFiveDay;
import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mars on 9/22/15.
 */
public class MorningService extends Service implements OnLocation, ForecastFiveDay.OnAccept {
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

    private void someTask() {
        if (isNetworkAvailable()) {
            if (db.mapSetting.get(db.MY_LOCATION) != null && !db.mapSetting.get(db.MY_LOCATION).equals("0")) {
                new LocationFromAsset(this, db.mapSetting.get(db.MY_LOCATION));
            } else {
                locationManagerNet = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManagerNet.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManagerNet.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationListenerNet = new MyLocationListenerNet(this);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
                }else{
                    // new DayForecastRain(this);
                }
            }

        }
    }

    @Override
    public void onLocationAccept(double lat, double lng) {
        if (locationManagerNet != null) {
            locationManagerNet.removeUpdates(locationListenerNet);
            locationManagerNet = null;
        }
       // new DayForecastRain(this, lat, lng);
        new  ForecastFiveDay(this, lat, lng);
    }

    @Override
    public void onLocationAccept(String cityId) {
       new  ForecastFiveDay(this, cityId, 8);
    }

    @Override
    public void accept(List<HashMap> list, String cityName, int statusCode) {
        if(list==null || list.size()==0) {
            this.stopSelf();
            return;
        }
        SimpleDateFormat formatDayMont = new SimpleDateFormat("dd"); //18
        String dateOfMonthCur = formatDayMont.format(new Date());
        String dateOfMonth = "";

        Calendar calendarCur = Calendar.getInstance();
        calendarCur.setTimeInMillis(System.currentTimeMillis());

       // int dateOfMonthCur = calendarCur.get(Calendar.DAY_OF_MONTH);
        double seedBarrValue = 0, rainValue = 0;
        for (HashMap<String, String> map : list){
            dateOfMonth = map.get("dayOfMonth");
            if(dateOfMonthCur.equals(dateOfMonth)){
                if(map.get("rain")!=null && !map.get("rain").isEmpty()){
                    int hh =Integer.parseInt(map.get("HH"));

                    if(db.mapSetting.get(db.SEED_BARR_VALUE)!=null){
                        rainValue = Double.parseDouble(map.get("rain"));
                        seedBarrValue = Double.parseDouble(db.mapSetting.get(db.SEED_BARR_VALUE));
                        if(seedBarrValue<rainValue &&  calendarCur.get(Calendar.HOUR_OF_DAY)<hh){
                            String time = map.get("time");
                            notificationCreate(time, cityName);
                            break;
                        }
                    }else {
                        String time = map.get("time");
                        notificationCreate(time, cityName);
                        break;
                    }

                }
            }
        }
        this.stopSelf();
    }



    void notificationCreate(String HH, String city){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("time", HH + "ч");
        intent.putExtra("were_from", "morning_service");
        intent.putExtra("cityName", city);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this).setContentTitle("Rain alarm")
                .setContentText(city+". "+ Cities.getStringResourceByName("probability_rain", this)+" " +HH+ Cities.getStringResourceByName("hh", this))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.notification_rain)

                .build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        nm.notify(2, notification);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}


