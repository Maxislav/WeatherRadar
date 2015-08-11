package com.atlas.mars.weatherradar.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.atlas.mars.weatherradar.DataBaseHelper;
import com.atlas.mars.weatherradar.Density;
import com.atlas.mars.weatherradar.LogTags;
import com.atlas.mars.weatherradar.MainActivity;
import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.loader.Loader;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by mars on 8/3/15.
 */
public abstract class MyFragment  implements View.OnClickListener, PopupMenu.OnMenuItemClickListener  {

    private View view;
    private ImageView imageView;
    private LinearLayout mainLayout;
    private static Activity activity;
    public String imageUrl;
    public TextView title;
    // ImageButton buttonReload;
    //  ImageButton buttonMenu;
    FrameLayout containerImg;
    static HashMap<String, String> mapSetting;
    int position;

    public MyFragment(View view, Activity activity, int position){
        this.activity = activity;
        this.view = view;
        this.position = position;
        imageView = (ImageView)view.findViewById(R.id.image);
        containerImg = (FrameLayout)view.findViewById(R.id.containerImg);
        // buttonReload = (ImageButton)view.findViewById(R.id.buttonReload);
        //buttonMenu = (ImageButton)view.findViewById(R.id.buttonMenu);
        //  buttonMenu.setOnClickListener(this);
        //  buttonReload.setOnClickListener(this);
        mainLayout = (LinearLayout)view.findViewById(R.id.main);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(Density.widthPixels*1.34));
        containerImg.setLayoutParams(parms);
        final int heightButton = (int)(Density.heightPixels - (Density.widthPixels*1.34));
        final double g= (float)Density.heightPixels - (float)Density.widthPixels * 1.34;
        // title = (TextView) view.findViewById(R.id.title);
        mapSetting = DataBaseHelper.mapSetting;



        // setSisze();
        setImageUrl();
        setTitle();
        loadImg();
    }

    private void setSisze(){
      /*  ViewTreeObserver observer= ((FrameLayout)buttonReload.getParent()).getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonReload.setLayoutParams(new  FrameLayout.LayoutParams (buttonReload.getHeight(),buttonReload.getHeight() ));
               // buttonMenu.setLayoutParams(new  FrameLayout.LayoutParams ((int)(buttonMenu.getHeight()/2),buttonMenu.getHeight() ));
            }
        });*/
    }



    public void setTitle(){
      /*  if(mapSetting.get("title1")==null){
            title.setText("Title1");
        }else{
            title.setText(mapSetting.get("title1"));
        }*/
    }

    public void setImageUrl(){
        String key = "url"+(position+1);
        if(mapSetting.get(key)==null){
            imageUrl = null;
        }else{
            imageUrl = mapSetting.get(key);
        }
    }

    private void loadImg(){
        if(imageUrl == null || imageUrl.isEmpty()){
            toastShow("No url detect. Set url" + (position+1));
        }else if(isNetworkAvailable()){
            LoadImage li = new LoadImage();
            li.execute();
        }



    }

    public void reloadImg(){
        setImageUrl();
        loadImg();
    }

    private void setBitmap(Bitmap bitmap){
        if(bitmap!=null){
            imageShow(imageView);
            imageView.setImageBitmap(bitmap);

        }else{
            toastShow("Error load IMG on page№ " +(position+1));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonReload:
                reloadImg();
                break;
            case R.id.buttonMenu:
                PopupMenu popupMenu = new PopupMenu(activity, v);
                popupMenu.inflate(R.menu.menu_main);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                break;
        }
    }



    @Override
    public boolean onMenuItemClick(MenuItem item) {
        activity.onOptionsItemSelected(item);
        return false;
    }


    private class LoadImage extends AsyncTask<String, Void, Bitmap> {

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
        ((MainActivity)activity).show(txt);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
