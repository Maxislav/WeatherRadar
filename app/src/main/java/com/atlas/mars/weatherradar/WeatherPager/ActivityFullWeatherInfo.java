package com.atlas.mars.weatherradar.WeatherPager;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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
    static List<HashMap> list;
    int iDay;


    TextView dayWeekFull, textViewDate, textViewTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = Forecast.list;
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

       // inflateWebDrip(v)

       // WebView browser = (WebView) v.findViewById(R.id.webViewDrips);
        //browser.setBackgroundColor(Color.TRANSPARENT);


        TextView textViewTemp = (TextView)v.findViewById(R.id.textViewTemp);
        HashMap<String, String> map = list.get(position);

        inflateWebDrip(v, map);

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
        if(map.get("wind_deg")!=null){
            textViewArrow.setText(""+ (char)0x2191);
            textViewArrow.setRotation(Float.parseFloat(map.get("wind_deg")));
        }
        if(position == iDay){
            _setTitle(map) ;
        }

    }

    void inflateWebDrip(View view, final HashMap<String, String> map) {
        WebView browser = (WebView) view.findViewById(R.id.webViewDrips);
        browser.setBackgroundColor(Color.TRANSPARENT);
        String drip = "";
        boolean isNeedCreate = false;

        String rain3h = null, snow3h = null;

        if (map.get("rain") != null && !map.get("rain").isEmpty()) {
            isNeedCreate = true;
            rain3h = map.get("rain");
            float f = Float.parseFloat(map.get("rain"));
            f = f * 10;
            for (int i = 0; i < f; i++) {
                drip += "<img style=\"-webkit-user-select: none; width: 15px; height:24px; margin: 2px\" src=\"drip.png\">";
            }


            //  browser.loadDataWithBaseURL("file:///android_asset/", web, "text/html", "UTF-8", null);
            //Todo касание и отображение тоста
            /*browser.setOnTouchListener(new WebView.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    toast.show("Rain 3h: " + map.get("rain"), Gravity.TOP | Gravity.CLIP_HORIZONTAL);
                    return false;
                }
            });*/
        }

        if (map.get("snow") != null && !map.get("snow").isEmpty()) {
            isNeedCreate = true;
            snow3h = map.get("snow");
            float f = Float.parseFloat(map.get("snow"));
            f = f * 10;
            for (int i = 0; i < f; i++) {
                drip += "<img style=\"-webkit-user-select: none; width: 22px; height:30px; margin: 4px 0px\" src=\"snow.png\">";
            }
        }

        if (isNeedCreate) {

          /*  String packageName = context.getPackageName();
            int resId = context.getResources().getIdentifier("hh"+map.get("HH"), "color", packageName);
            String color16 =    context.getString(resId);*/


            String color16 = Forecast.getStringResourceByName("color", "hh" + map.get("HH"));
            color16 = color16.replaceAll("^#.{2}", "#");


            String web = "<html><head><meta name=\"viewport\" content=\"width=device-width, minimum-scale=0.1\"><title>drip.png (20×32)</title><style type=\"text/css\"></style></head><body style=\"margin: 0px;\">" +
                    "<div style=\"position: relative; overflow: hidden; height: 120px; width: 100%; \">" +
                    "<div>" + drip + "</div>" +
                    "<div style=\"position: absolute; height: 120px; width: 100%; top: 0;left: 0; background:  linear-gradient(to bottom, rgba(0,0,0,0) 0%, " + color16 + " 100%);\">" +

                    "</div>" +
                    "</div>" +
                    "</body></html>";
            browser.loadDataWithBaseURL("file:///android_asset/", web, "text/html", "UTF-8", null);

            String ts = "";

            if (rain3h != null) {
                ts += "Rain 3h: " + rain3h;
            }
            if (!ts.isEmpty()) {
                ts += " ";
            }

            if (snow3h != null) {
                ts += "Snow 3h: " + snow3h;
            }
            // onWebShowToast3h(browser, ts, map);
           // view.findViewById(R.id.overLayout).setOnClickListener(new MyClick(ts));
        }
       // view.findViewById(R.id.overLayout).setOnLongClickListener(new MyLongClick(map, iDay));
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
