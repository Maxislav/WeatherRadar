package com.atlas.mars.weatherradar.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.atlas.mars.weatherradar.DataBaseHelper;
import com.atlas.mars.weatherradar.Rest.DayForecastRain;
import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    public void Success(HashMap<Long, String> map, String name) {
        if(map==null) {
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
           /* System.out.println("Key: " + entry.getKey() + " Value: "
                    + entry.getValue());*/
        }
        this.stopSelf();
    }
}


