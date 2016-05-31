package com.atlas.mars.weatherradar.Zoom;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;

import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.fragments.BitmapTransfer;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by mars on 5/14/16.
 */
public class ActivityZoom extends Activity {
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
        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(R.id.imageView);
        imageView.setMaxScale(4);
        imageView.setImage(ImageSource.bitmap(bitmap));
    }
}
