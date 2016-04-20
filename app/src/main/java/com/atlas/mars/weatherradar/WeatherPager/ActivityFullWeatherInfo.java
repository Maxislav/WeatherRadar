package com.atlas.mars.weatherradar.WeatherPager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.atlas.mars.weatherradar.Communicator;
import com.atlas.mars.weatherradar.R;

/**
 * Created by mars on 4/20/16.
 */
public class ActivityFullWeatherInfo extends FragmentActivity implements Communicator, ViewPager.OnPageChangeListener {
    final String TAG = "ActivityFullWeatherInfoLogs";

    ViewPager pager;
    PagerAdapter pagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_weather_info);

       // FragmentManager fm = getSupportFragmentManager();

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), 10);
        pager.setAdapter(pagerAdapter);
        //cтартуем на соответсвующей странице
        pager.setCurrentItem(2);
        pager.setOnPageChangeListener(this);
        
    }

    @Override
    public void initView(View v, int position) {
        Log.d(TAG, "initView: " + position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "position: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
