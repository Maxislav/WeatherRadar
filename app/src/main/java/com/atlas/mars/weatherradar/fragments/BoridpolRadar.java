package com.atlas.mars.weatherradar.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.atlas.mars.weatherradar.Density;
import com.atlas.mars.weatherradar.LogTags;
import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.loader.Loader;

import java.io.InputStream;

/**
 * Created by mars on 7/8/15.
 */
public class BoridpolRadar {
    private static View view;
    private static ImageView imageView;
    private static Activity activity;
    public BoridpolRadar(View view, Activity activity){
        this.activity = activity;
        this.view = view;
        imageView = (ImageView)view.findViewById(R.id.image);
        FrameLayout.LayoutParams parms = new FrameLayout.LayoutParams(Density.heightPixels,Density.widthPixels);
        parms.gravity = Gravity.CENTER;
        imageView.setLayoutParams(parms);
        reloadImg();
    }

    public void reloadImg(){
        LoadImage li = new LoadImage();
        li.execute();
    }


    private void setBitmap(Bitmap bitmap){
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
        }
    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap>{

        Loader loader;

        protected void onPreExecute() {
            super.onPreExecute();
            loader = new Loader(activity, view.findViewById(R.id.main));
            loader.show();

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String urldisplay = "http://meteoinfo.by/radar/UKBB/UKBB_latest.png";
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(LogTags.TAG, e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;


          //  return null;
        }

        @Override

        protected void onPostExecute(Bitmap result) {
            loader.hide();
            setBitmap(result);
        }
    }
}
