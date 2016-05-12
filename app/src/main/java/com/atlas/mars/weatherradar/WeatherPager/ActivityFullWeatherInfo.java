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


    TextView dayWeekFull, textViewDate, textViewTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iDay = getIntent().getExtras().getInt("iDay");



        setContentView(R.layout.activity_full_weather_info);
        dayWeekFull = (TextView)findViewById(R.id.dayWeekFull);
        textViewDate = (TextView)findViewById(R.id.textViewDate);
        textViewTime = (TextView)findViewById(R.id.textViewTime);

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), list.size());
        pager.setAdapter(pagerAdapter);
        //cтартуем на соответсвующей странице
        pager.setCurrentItem(iDay);
        pager.setOnPageChangeListener(this);
        onPageSelected(iDay);


    }

    void _setTitle(HashMap<String, String> map){
        if(map!=null && map.get("dayWeekFull")!=null){
            dayWeekFull.setText(map.get("dayWeekFull").toUpperCase());
            textViewDate.setText(map.get("date"));
            textViewTime.setText(map.get("time"));
        }

    }

    @Override
    public void initView(View v, int position) {
        Log.d(TAG, "initView: " + position);
        TextView textViewTemp = (TextView)v.findViewById(R.id.textViewTemp);
        HashMap<String, String> map = list.get(position);
        textViewTemp.setText(list.get(position).get("temp").toString()+(char)0x00B0+"C");
        getIcon((ImageView)v.findViewById(R.id.image), map.get("icon").toString());
        ViewGroup viewGroup = (ViewGroup) v;
        FrameLayout childView = (FrameLayout) viewGroup.getChildAt(0);
        Integer color = Forecast.getColorHour(map.get("HH"));
        childView.setBackgroundColor(getResources().getColor( Forecast.getColorHour( map.get("HH"))));
        ((TextView)v.findViewById(R.id.textViewTempMax)).setText(map.get("temp_max"));
        ((TextView)v.findViewById(R.id.textViewTempMin)).setText(map.get("temp_min"));
        ((TextView)v.findViewById(R.id.textViewHumidity)).setText(map.get("humidity"));
        ((TextView)v.findViewById(R.id.textViewPressure)).setText(map.get("pressure"));
        ((TextView)v.findViewById(R.id.textViewClouds)).setText(map.get("clouds")+(char)0x0025);
        ((TextView)v.findViewById(R.id.textView3h)).setText( map.get("rain")!=null? (map.get("rain")+"mm") : "нет");

        ((TextView)v.findViewById(R.id.textViewWindSpeed)).setText( map.get("wind_speed")!=null? (map.get("wind_speed")+"m/s") : "нет");
        ((TextView)v.findViewById(R.id.textViewWindDeg)).setText( map.get("wind_deg")!=null? (map.get("wind_deg")+(char)0x00B0) : "");
        TextView textViewArrow = (TextView)v.findViewById(R.id.textViewArrow);
        textViewArrow.setText(""+ (char)0x2191);
        textViewArrow.setRotation(Float.parseFloat(map.get("wind_deg")));




        if(position == iDay){
            _setTitle(map) ;
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "position: " + position);
        _setTitle(list.get(position));
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
