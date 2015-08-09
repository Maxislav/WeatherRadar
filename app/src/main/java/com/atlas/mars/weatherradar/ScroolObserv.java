package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

/**
 * Created by mars on 8/3/15.
 */
public class ScroolObserv implements View.OnTouchListener {
    final String TAG = "ScroolObservLogs";
    MainActivity mainActivity;
    ScrollView scrollView;
    ViewTreeObserver observer;


    ScroolObserv(MainActivity mainActivity, ScrollView scrollView){
        this.mainActivity = mainActivity;
        this.scrollView = scrollView;
        scrollView.setOnTouchListener(this);
    }

    final ViewTreeObserver.OnScrollChangedListener onScrollChangedListener = new
            ViewTreeObserver.OnScrollChangedListener() {

                @Override
                public void onScrollChanged() {
                    //do stuff here
                    Log.d(TAG, ""+scrollView.getScrollY());
                    /*scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, ""+scrollView.getScrollY());
                        }
                    });*/

                }
            };

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (observer == null) {
            observer = scrollView.getViewTreeObserver();
            observer.addOnScrollChangedListener(onScrollChangedListener);
        }
        else if (!observer.isAlive()) {
            observer.removeOnScrollChangedListener(onScrollChangedListener);
            observer = scrollView.getViewTreeObserver();
            observer.addOnScrollChangedListener(onScrollChangedListener);
        }

        return false;

    }


   /* @Override
    public boolean onTouch(View v, MotionEvent event) {
        ViewTreeObserver observer;

        public boolean onTouch(View v, MotionEvent event) {
            if (observer == null) {
                observer = scrollView.getViewTreeObserver();
                observer.addOnScrollChangedListener(onScrollChangedListener);
            }
            else if (!observer.isAlive()) {
                observer.removeOnScrollChangedListener(onScrollChangedListener);
                observer = scrollView.getViewTreeObserver();
                observer.addOnScrollChangedListener(onScrollChangedListener);
            }

            return false;
        }
    }*/
}
