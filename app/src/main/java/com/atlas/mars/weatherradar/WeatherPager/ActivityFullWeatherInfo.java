package com.atlas.mars.weatherradar.WeatherPager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.atlas.mars.weatherradar.Communicator;
import com.atlas.mars.weatherradar.Forecast;
import com.atlas.mars.weatherradar.IconForecast;
import com.atlas.mars.weatherradar.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mars on 4/20/16.
 */
public class ActivityFullWeatherInfo extends FragmentActivity implements Communicator, ViewPager.OnPageChangeListener {
    final String TAG = "ActivityFullWeatherInfoLogs";

    ViewPager pager;
    PagerAdapter pagerAdapter;
    static List<HashMap> list = Forecast.list;
    int iDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iDay = getIntent().getExtras().getInt("iDay");

        setContentView(R.layout.activity_full_weather_info);

       // FragmentManager fm = getSupportFragmentManager();

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), list.size());
        pager.setAdapter(pagerAdapter);
        //cтартуем на соответсвующей странице
        pager.setCurrentItem(iDay);
        pager.setOnPageChangeListener(this);
        
    }

    @Override
    public void initView(View v, int position) {
        Log.d(TAG, "initView: " + position);
        TextView textViewTemp = (TextView)v.findViewById(R.id.textViewTemp);
        HashMap<String, String> map = list.get(position);

        textViewTemp.setText(list.get(position).get("temp").toString());


        getIcon((ImageView)v.findViewById(R.id.image), map.get("icon").toString());

        ViewGroup viewGroup = (ViewGroup) v;
        FrameLayout childView = (FrameLayout) viewGroup.getChildAt(0);

        Integer color = Forecast.getColorHour(map.get("HH"));

       // childView.setBackgroundColor(Forecast.getColorHour(map.get("HH")));
        childView.setBackgroundColor(getResources().getColor( Forecast.getColorHour( map.get("HH"))));

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
    public void getIcon(ImageView imageView, String icon) {

        int resId = getResources().getIdentifier("i" + icon, "drawable", getPackageName());
        if (resId != 0) {
            imageView.setBackgroundResource(resId);
        } else {
            // Log.d(TAG, "Icon not exist " + icon);
            new IconForecast(imageView, icon);
        }

    }
}
