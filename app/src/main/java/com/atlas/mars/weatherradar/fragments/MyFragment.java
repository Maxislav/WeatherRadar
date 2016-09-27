package com.atlas.mars.weatherradar.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
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
import com.atlas.mars.weatherradar.Zoom.ActivityZoom;
import com.atlas.mars.weatherradar.loader.Loader;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by mars on 8/3/15.
 */
public abstract class MyFragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private View view;
    private ImageView loadingImageView, imageView;
    MainActivity mainActivity;
    private LinearLayout mainLayout;
    private static Activity activity;
    public String imageUrl;
    public TextView title;
    LoadImage loadImage;
    // ImageButton buttonReload;
    //  ImageButton buttonMenu;
    FrameLayout containerImg;
    static HashMap<String, String> mapSetting;
    int position;
    Bitmap bitmap;
    static Resources resources;
    BitmapTransfer bitmapTransfer;


    public MyFragment(View view, Activity activity, int position) {
        this.activity = activity;
        mainActivity = (MainActivity) activity;
        resources = mainActivity.getResources();
        bitmapTransfer = new BitmapTransfer();
        this.view = view;
        this.position = position;
        loadImage = new LoadImage();
        containerImg = (FrameLayout) view.findViewById(R.id.containerImg);
        mainLayout = (LinearLayout) view.findViewById(R.id.main);

        LinearLayout.LayoutParams parms;

        if (isLandscapeMode()) {
            //  parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(Density.widthPixels/1.34));
        } else {
            //  parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(Density.widthPixels));
        }

        //  containerImg.setLayoutParams(parms);
        final int heightButton = (int) (Density.heightPixels - (Density.widthPixels * 1.34));
        final double g = (float) Density.heightPixels - (float) Density.widthPixels * 1.34;
        mapSetting = DataBaseHelper.mapSetting;
        setImageUrl();
        setTitle();
    }


    public void setTitle() {
      /*  if(mapSetting.get("title1")==null){
            title.setText("Title1");
        }else{
            title.setText(mapSetting.get("title1"));
        }*/
    }

    public void setImageUrl() {
        String key = "url" + (position + 1);
        if (mapSetting.get(key) == null) {
            imageUrl = null;
        } else {
            imageUrl = mapSetting.get(key);
        }
    }

    private void loadImg() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            toastShow("No url detect. Set url" + (position + 1));
        } else if (isNetworkAvailable()) {
            if (loadImage.getStatus() != AsyncTask.Status.RUNNING) {
                loadImage.execute();
            }
        }
    }

    public void firstLoad() {
        if (bitmap == null) {
            loadImg();
        }
    }

    public void reloadImg() {
        setImageUrl();
        loadImg();
    }

    private void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            this.bitmap = bitmap;
            loadingImageView.setImageBitmap(bitmap);
            imageShow(loadingImageView);

        } else {
            toastShow("Error load IMG on page№ " + (position + 1));
        }
    }

    private void imageShow(final ImageView imageView) {
        final Context context = activity;
        AlphaAnimation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnimation.setDuration(500);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                replaceImage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(fadeInAnimation);
    }

    private void imageHide() {

    }

    void replaceImage() {
        if (imageView != null) {
            containerImg.removeView(imageView);
        }
        imageView = new ImageView(activity);
        setLayoutParams(imageView);
        containerImg.addView(imageView);
        imageView.setImageBitmap(((BitmapDrawable) loadingImageView.getDrawable()).getBitmap());
        containerImg.removeView(loadingImageView);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                bitmapTransfer.setBitmap(bitmap);

                Intent intent = new Intent(activity, ActivityZoom.class);
                intent.putExtra("isLandscapeMode", isLandscapeMode());
                intent.putExtra("position", position);
                activity.startActivityForResult(intent, 3);
                return false;
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

    private void setLayoutParams(ImageView imageView) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }


    private class LoadImage extends AsyncTask<String, Void, Bitmap> {

        Loader loader;

        protected void onPreExecute() {
            super.onPreExecute();
            loadingImageView = new ImageView(activity);
            containerImg.addView(loadingImageView);
            loadingImageView.setVisibility(View.INVISIBLE);
            setLayoutParams(loadingImageView);
            loader = new Loader(activity, containerImg);
            loader.show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // String imageUrl = "http://meteoinfo.by/radar/UKBB/UKBB_latest.png";
            Bitmap mIcon11 = null;
            Bitmap modyfy = null;
            try {

                InputStream in = new java.net.URL(imageUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e(LogTags.TAG, e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override

        protected void onPostExecute(Bitmap result) {
            loader.hide();

            Matrix matrix = new Matrix();
            // matrix.postScale(0.5f, 0.5f);
            // Bitmap croppedBitmap = Bitmap.createBitmap(result, 0, 0, result.getHeight(), result.getHeight(), matrix, true);

            matrix.postRotate(0);
            if (!isLandscapeMode()) {

                if (result != null) {
                    switch (position) {
                        case 0:
                            result = Bitmap.createBitmap(result, 0, 0, result.getHeight(), result.getHeight(), matrix, true);
                            break;
                        default:
                            result = Bitmap.createBitmap(result, result.getWidth() - result.getHeight(), 0, result.getHeight(), result.getHeight(), matrix, true);


                    }

                }

            }
            loadImage = new LoadImage();
            if (result != null) {
                setBitmap(result);
            }
        }
    }

    private void toastShow(String txt) {
        ((MainActivity) activity).show(txt);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private boolean isLandscapeMode() {
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return false;
        } else {
            return true;
        }
    }
}
