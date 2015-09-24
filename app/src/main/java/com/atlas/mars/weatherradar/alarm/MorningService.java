package com.atlas.mars.weatherradar.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import com.atlas.mars.weatherradar.Rest.DayForecastRain;
import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;

/**
 * Created by mars on 9/22/15.
 */
public class MorningService extends Service implements OnLocation, DayForecastRain.Callbackwqw {
    private LocationManager locationManagerNet;
    private LocationListener locationListenerNet;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
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
    public void Success() {

        this.stopSelf();
    }
}


