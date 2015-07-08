package com.atlas.mars.weatherradar.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.atlas.mars.weatherradar.Density;
import com.atlas.mars.weatherradar.LogTags;
import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.loader.Loader;

import java.io.InputStream;

/**
 * Created by mars on 7/8/15.
 */
public class BoridpolRadar {
    private View view;
    private ImageView imageView;
    private static Activity activity;
    public String imageUrl;
    public TextView title;
    public BoridpolRadar(View view, Activity activity){
        this.activity = activity;
        this.view = view;
        imageView = (ImageView)view.findViewById(R.id.image);
     //   FrameLayout.LayoutParams parms = new FrameLayout.LayoutParams(Density.widthPixels,Density.heightPixels);
        FrameLayout.LayoutParams parms = new FrameLayout.LayoutParams(Density.heightPixels,Density.widthPixels);
        parms.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        title = (TextView) view.findViewById(R.id.title);
        imageView.setLayoutParams(parms);
        setImageUrl();
        setTitle();
        loadImg();
    }

    public void setImageUrl(){
        imageUrl = "http://meteoinfo.by/radar/UKBB/UKBB_latest.png";
    }
    public void setTitle(){
        title.setText("Borispol");
    }

    private void loadImg(){
        LoadImage li = new LoadImage();
        li.execute();
    }

    public void reloadImg(){
        LoadImage li = new LoadImage();
        li.execute();
    }

    private void setBitmap(Bitmap bitmap){
        if(bitmap!=null){
            imageShow(imageView);
            imageView.setImageBitmap(bitmap);
        }
    }



    private void imageShow(final ImageView imageView){
        AlphaAnimation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnimation.setDuration(500);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(fadeInAnimation);
    }

    private void imageHide(){

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
           // String imageUrl = "http://meteoinfo.by/radar/UKBB/UKBB_latest.png";
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                toastShow("Error Load img");
                Log.e(LogTags.TAG, e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override

        protected void onPostExecute(Bitmap result) {
            loader.hide();
            setBitmap(result);
        }
    }

    private void toastShow(String txt){
        Context context = activity.getApplicationContext();
        CharSequence text = txt;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
