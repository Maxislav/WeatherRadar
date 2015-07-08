package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by mars on 7/8/15.
 */
public class Density {
    public static int density;
    public static int widthPixels;
    public static int heightPixels;
    public Density(Activity activity){
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        density = displayMetrics.densityDpi;
        widthPixels = displayMetrics.widthPixels;
        heightPixels = displayMetrics.heightPixels;
    }
}
