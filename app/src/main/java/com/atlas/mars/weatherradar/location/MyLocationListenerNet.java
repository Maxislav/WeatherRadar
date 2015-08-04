package com.atlas.mars.weatherradar.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.atlas.mars.weatherradar.alarm.MyService;

/**
 * Created by Администратор on 8/1/15.
 */
public class MyLocationListenerNet implements LocationListener {
    OnLocation onLocation;
    public MyLocationListenerNet(OnLocation onLocation){
        this.onLocation = onLocation;
    }

    public void onCallback(double lat, double lng){
        onLocation.onLocationAccept(lat, lng);
    }



    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        onCallback(lat, lng);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
