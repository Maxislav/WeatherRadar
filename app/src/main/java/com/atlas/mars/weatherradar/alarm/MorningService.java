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
import android.widget.RemoteViews;

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

    private void   someTask(){
        if (isNetworkAvailable()){
            if(db.mapSetting.get(db.MY_LOCATION)!=null && !db.mapSetting.get(db.MY_LOCATION).equals("0")){
                new LocationFromAsset(this, db.mapSetting.get(db.MY_LOCATION));
            }else {
                locationManagerNet = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                if(locationManagerNet.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManagerNet.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    locationListenerNet = new MyLocationListenerNet(this);
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
        new  ForecastFiveDay(this, lat, lng, 8);
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

        for (HashMap<String, String> map : list){
            dateOfMonth = map.get("dayOfMonth");
            if(dateOfMonthCur.equals(dateOfMonth)){
                if(map.get("rain")!=null && !map.get("rain").isEmpty()){
                    String hh = map.get("time");
                    notificationCreate(hh, cityName);
                    break;
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


