package com.atlas.mars.weatherradar.loader;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.atlas.mars.weatherradar.R;

/**
 * Created by mars on 7/8/15.
 */
public class Loader {
    Activity activity;
    View main;
    View view;

    public Loader(Activity activity, View parent){
        this.activity = activity;
        this.main = parent;
    }

    private void  inflate(){
        LayoutInflater inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.loader, null, false);
      //  ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progressBar);

      //  progressBar.setProgress(100);
      //Animation anim = new ProgressBarAnimation(progressBar, progressBar.getMax(), 0);
                ((ViewGroup) main).addView(view);
    }

    public void show(){
        if(view==null){
            inflate();
        }
        view.setVisibility(View.VISIBLE);

    }
    public void hide(){
        view.setVisibility(View.INVISIBLE);
    }

}
