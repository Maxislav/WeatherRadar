package com.atlas.mars.weatherradar.fragments;

import android.app.Activity;
import android.view.View;

/**
 * Created by mars on 7/8/15.
 */
public class Visible extends BoridpolRadar {

    public Visible(View view, Activity activity) {
        super(view, activity);
    }

    @Override
    public void setImageUrl(){
        imageUrl = "http://www.sat24.com/image2.ashx?region=eu";
    }
    @Override
    public void setTitle(){
        title.setText("Visible");
    }
}
