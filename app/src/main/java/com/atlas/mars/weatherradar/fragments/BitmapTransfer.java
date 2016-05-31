package com.atlas.mars.weatherradar.fragments;

import android.graphics.Bitmap;

/**
 * Created by mars on 5/31/16.
 */
public class BitmapTransfer {
    public static Bitmap bitmap;

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

}
