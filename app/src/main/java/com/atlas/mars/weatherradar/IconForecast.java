package com.atlas.mars.weatherradar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.atlas.mars.weatherradar.loader.Loader;

import java.io.InputStream;

/**
 * Created by Администратор on 8/3/15.
 */
public class IconForecast {
    public String imageUrl;
    ImageView imageView;

    public IconForecast(ImageView imageView, String icon){
        this.imageView = imageView;


        //int resId=.getResources().getIdentifier("ball_red", "drawable", YourActivity.this.getPackageName());



        imageUrl = "http://openweathermap.org/img/w/"+icon+".png";
        LoadImage loadImage = new LoadImage();
        loadImage.execute();
    }

    private void setBitmap(Bitmap bitmap){
        if(bitmap!=null){
            //imageShow(imageView);
            imageView.setImageBitmap(bitmap);

        }else{

        }
    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap> {

        Loader loader;

        protected void onPreExecute() {
            super.onPreExecute();
            /*loader = new Loader(activity, containerImg);
            loader.show();*/

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // String imageUrl = "http://meteoinfo.by/radar/UKBB/UKBB_latest.png";
            Bitmap mIcon11 = null;
            Bitmap modyfy = null;
            try {
                Matrix matrix = new Matrix();

              //  matrix.postRotate(90);
                InputStream in = new java.net.URL(imageUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

                modyfy = Bitmap.createBitmap(mIcon11, 0, 0,  mIcon11.getWidth(),  mIcon11.getHeight(), matrix, true);
            } catch (Exception e) {
                Log.e(LogTags.TAG, e.getMessage());
                e.printStackTrace();
            }
            return modyfy;
        }

        @Override

        protected void onPostExecute(Bitmap result) {
           // loader.hide();
            setBitmap(result);
        }
    }
}
