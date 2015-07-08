package com.atlas.mars.weatherradar.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atlas.mars.weatherradar.Density;
import com.atlas.mars.weatherradar.LogTags;
import com.atlas.mars.weatherradar.MainActivity;
import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.loader.Loader;

import java.io.InputStream;

/**
 * Created by mars on 7/8/15.
 */
public class BoridpolRadar {
    private View view;
    private ImageView imageView;
    private FrameLayout mainLayout;
    private static Activity activity;
    public String imageUrl;
    public TextView title;
    FrameLayout containerImg;
    public BoridpolRadar(View view, Activity activity){
        this.activity = activity;
        this.view = view;
        imageView = (ImageView)view.findViewById(R.id.image);
        containerImg = (FrameLayout)view.findViewById(R.id.containerImg);
        //FrameLayout.LayoutParams parms = new FrameLayout.LayoutParams(Density.widthPixels,Density.heightPixels);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(Density.widthPixels*1.366));
        containerImg.setLayoutParams(parms);
        //parms.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
       // imageView.setLayoutParams(parms);
        title = (TextView) view.findViewById(R.id.title);
        //parms = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT ,(int)(Density.heightPixels - Density.widthPixels*1.366));
       // parms = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT ,100);
       // title.setLayoutParams(parms);

       // mainLayout = (FrameLayout)view.findViewById(R.id.main);
       // mainLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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
            loader = new Loader(activity, containerImg);
            loader.show();

        }

        @Override
        protected Bitmap doInBackground(String... params) {
           // String imageUrl = "http://meteoinfo.by/radar/UKBB/UKBB_latest.png";
            Bitmap mIcon11 = null;
            Bitmap modyfy = null;
            try {
                Matrix matrix = new Matrix();

                matrix.postRotate(90);
                InputStream in = new java.net.URL(imageUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

                modyfy = Bitmap.createBitmap(mIcon11, 0, 0,  mIcon11.getWidth(),  mIcon11.getHeight(), matrix, true);
            } catch (Exception e) {
                toastShow("Error Load img");
                Log.e(LogTags.TAG, e.getMessage());
                e.printStackTrace();
            }
            return modyfy;
        }

        @Override

        protected void onPostExecute(Bitmap result) {
            loader.hide();
            setBitmap(result);
        }
    }

    private void toastShow(String txt){
        ((MainActivity)activity).toastShow(txt);
    }
}
