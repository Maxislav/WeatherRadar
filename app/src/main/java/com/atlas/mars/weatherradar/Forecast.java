package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by mars on 8/3/15.
 */
public class Forecast {
    Activity activity;
    LinearLayout fr;

    Forecast(Activity activity, LinearLayout fr){
        this.activity = activity;
        this.fr = fr;
        onInflate();
    }


    void onInflate(){
        LayoutInflater inflater = (LayoutInflater)(activity.getLayoutInflater());
        int width = (int)(80*Density.density);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( width,ViewGroup.LayoutParams.MATCH_PARENT);
        for (int i = 0 ; i <10; i++){
            View view = inflater.inflate(R.layout.forecast_container, null, false);
            fr.addView(view);
            view.setLayoutParams(layoutParams);
        }
    }


}
