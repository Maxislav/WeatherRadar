package com.atlas.mars.weatherradar.Zoom;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;

import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.Rest.MeteoInfoBy;
import com.atlas.mars.weatherradar.fragments.BitmapTransfer;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by mars on 5/14/16.
 */
public class ActivityZoom extends Activity implements View.OnClickListener, MeteoInfoBy.MyEndCallback {
    ImageButton buttonPlay;
    HashMap<Integer,Bitmap> hashMapBitmap;
    SubsamplingScaleImageView imageView;
    Handler h;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        BitmapTransfer bitmapTransfer = new BitmapTransfer();
        Bitmap bitmap = bitmapTransfer.getBitmap();
        Matrix matrix = new Matrix();
        if(!getIntent().getExtras().getBoolean("isLandscapeMode")){
            matrix.postRotate(-90);
        }

        bitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);
        imageView = (SubsamplingScaleImageView)findViewById(R.id.imageView);
        imageView.setMaxScale(4);
        imageView.setImage(ImageSource.bitmap(bitmap));

        buttonPlay = (ImageButton)findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonPlay:
                if(hashMapBitmap == null || hashMapBitmap.size()==0){
                    hashMapBitmap = new HashMap<>();
                    for(int t=0; t<10; t+=1){
                        new MeteoInfoBy(this,t);
                    }
                }else{
                    onPlay();
                }
                break;
        }
    }

    @Override
    public void onSuccess(Bitmap bitmap, int i) {
        hashMapBitmap.put(i, bitmap);
        if(hashMapBitmap.size()==10){
            onPlay();
        }
    }

    private void onPlay(){
        h = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(hashMapBitmap.get(msg.what).isRecycled()){
                    hashMapBitmap.get(msg.what).recycle();
                }
                Bitmap bitmap = hashMapBitmap.get(msg.what).copy(Bitmap.Config.ARGB_8888, false);
                imageView.setImage(ImageSource.bitmap(bitmap));
            }
        };
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 9; 0<i; i--) {
                        TimeUnit.MILLISECONDS.sleep(700);
                        h.sendEmptyMessage(i);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();


        //imageView.setImage(ImageSource.bitmap(hashMapBitmap.get(9)));
    }
}
