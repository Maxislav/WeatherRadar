package com.atlas.mars.weatherradar.alarm;

import com.atlas.mars.weatherradar.location.OnLocation;

/**
 * Created by mars on 1/26/16.
 */
public class LocationFromAsset{

    OnLocation onLocation;
    int iPosition;

    public LocationFromAsset (OnLocation onLocation, String sPosition){
        this.onLocation = onLocation;
        iPosition = Integer.parseInt(sPosition);
    }

    void onCallback(){
        onLocation.onLocationAccept(1, 2);
    }


   /* @Override
    public void onLocationAccept(double lat, double lng) {
        onLocation.onLocationAccept(lat, lng);
    }*/
}
