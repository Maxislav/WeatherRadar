package com.atlas.mars.weatherradar.fragments;

import android.app.Activity;
import android.view.View;

/**
 * Created by mars on 7/8/15.
 */
public class InfraRed extends BoridpolRadar {

    public InfraRed(View view, Activity activity, int position) {
        super(view, activity, position);
    }

   /* @Override
    public void setImageUrl(){
        imageUrl = "http://www.sat24.com/image2.ashx?region=eu&ir=true";
    }*/
    @Override
    public void setTitle(){
        if(mapSetting.get("title2")==null){
            title.setText("Infrared");
        }else{
            title.setText(mapSetting.get("title2"));
        }
    }
}
