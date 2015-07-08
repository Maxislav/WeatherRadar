package com.atlas.mars.weatherradar.loader;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.atlas.mars.weatherradar.R;

/**
 * Created by mars on 7/8/15.
 */
public class Loader {
    Activity activity;
    View main;
    View view;
    AlphaAnimation fadeOutAnimation;
    AlphaAnimation fadeInAnimation;

    public Loader(Activity activity, View parent) {
        this.activity = activity;
        this.main = parent;
        fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnimation.setDuration(300);
        fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnimation.setDuration(300);
    }

    private void inflate() {
        LayoutInflater inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.loader, null, false);
        view.setVisibility(View.INVISIBLE);
        ((ViewGroup) main).addView(view);
    }

    public void show() {
        if (view == null) {
            inflate();
        }
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(fadeInAnimation);

    }

    public void hide() {
        fadeOutAnimation.setFillAfter(true);
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(fadeOutAnimation);

    }

}
