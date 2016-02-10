package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atlas.mars.weatherradar.Rest.ForecastFiveDay;
import com.atlas.mars.weatherradar.alarm.LocationFromAsset;
import com.atlas.mars.weatherradar.loader.Loader;
import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mars on 8/3/15.
 */
public class Forecast implements OnLocation, ForecastFiveDay.OnAccept {
    final String TAG = "ForecastLogs";
    Activity activity;
    LinearLayout fr;
    FrameLayout parent;
    public LocationManager locationManagerNet;
    public LocationListener locationListenerNet;
    Loader loader;
    ToastShow toast;
    private boolean isDoing = false;
    private static int onTaskResult = 0;
    DataBaseHelper db;

    Forecast(Activity activity, LinearLayout fr) {
        db = new DataBaseHelper(activity);

        this.activity = activity;
        toast = (ToastShow) activity;
        this.fr = fr;
        parent = (FrameLayout) fr.getParent().getParent();
        loader = new Loader(activity, parent);

        _onStart();


        //  onInflate();
    }

    private void _onStart() {

        String myLocation = db.mapSetting.get(db.MY_LOCATION);
        String permitsLocation = db.mapSetting.get(db.LICENCE);

        if (isNetworkAvailable()) {
            if (myLocation != null && !myLocation.equals("0")) {
                loader.show();
                new LocationFromAsset(activity, this,  myLocation);
            }else{
                if(permitsLocation == null || permitsLocation.equals("0")){
                    toast.show("Set agree licence first");
                    return;
                }
                locationManagerNet = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                if (locationManagerNet.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManagerNet.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationListenerNet = new MyLocationListenerNet(this);
                    loader.show();
                    locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
                }else {
                    /**
                     * Локация недоступна
                     */
                    //locationManagerNet.removeUpdates(locationListenerNet);
                    toast.show("Location is not available in setting");
                }
            }
        }else{
            toast.show("NetworkAvailable=false");
        }
    }

    @Override
    public void onLocationAccept(String cityId) {
        new ForecastFiveDay(this, cityId, null);
    }

    @Override
    public void onLocationAccept(double lat, double lng) {
        Log.d(TAG, "onLocationAccept lat lng: " + lat + " : " + lng);

        if (locationManagerNet != null) {
            locationManagerNet.removeUpdates(locationListenerNet);
            locationManagerNet = null;
        }
        new ForecastFiveDay(this, lat, lng, null);

    }

    @Override
    public void accept(List<HashMap> list, String cityName, int statusCode) {
          //  Log.d(TAG, "diid"+list);
        if(statusCode!=200){
            toast.show("Open weather map say Error!");
            return;
        }
        if (0 < list.size()) {
            infladeDay(list);
            db.mapSetting.put(db.TIMESTAMP_FORECAST,  db.getTimeStamp());
            db.saveSetting();
        } else {
            toast.show("City not found");
        }
        loader.hide();
    }


    public void onRegen() {
        fr.removeAllViews();
        _onStart();
    }


    void onInflate(final LinearLayout layoytDay, final HashMap<String, String> map) {
        layoytDay.post(new Runnable() {
            final HashMap<String, String> hashMap = map;

            @Override
            public void run() {
                LayoutInflater inflater = (LayoutInflater) (activity.getLayoutInflater());
                int width = (int) (80 * Density.density);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
                View view = inflater.inflate(R.layout.forecast_container, null, false);
                inflateWebDrip(view, map);
                layoytDay.addView(view);
                // ((TextView) view.findViewById(R.id.textDate)).setText( firstUpperCase(hashMap.get("dayWeek")) +" "+hashMap.get("date"));

                ((TextView) view.findViewById(R.id.textTime)).setText(hashMap.get("time"));
                ((TextView) view.findViewById(R.id.textTemp)).setText(hashMap.get("temp"));
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                // new IconForecast(imageView,hashMap.get("icon") );
                getIcon(imageView, hashMap.get("icon"));

                layoutParams.setMargins(2, 0, 2, 0);
                view.setLayoutParams(layoutParams);
                ViewGroup viewGroup = (ViewGroup) view;
                LinearLayout childView = (LinearLayout) viewGroup.getChildAt(0);
                //childView.setBackgroundColor(activity.getResources().getColor(getColorHour(hashMap.get("HH"))));
                GradientDrawable shape = new GradientDrawable();
                shape.setCornerRadius(8);
                shape.setColor(activity.getResources().getColor(getColorHour(hashMap.get("HH"))));
                childView.setBackground(shape);
            }
        });
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
                drip += "<img style=\"-webkit-user-select: none; width: 7px; height:13px; margin: 1.5px\" src=\"drip.png\">";
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
                drip += "<img style=\"-webkit-user-select: none; width: 12px; height:13px; margin: 2px 0px\" src=\"snow.png\">";
            }
        }

        if(isNeedCreate){
            String web = "<html><head><meta name=\"viewport\" content=\"width=device-width, minimum-scale=0.1\"><title>drip.png (20×32)</title><style type=\"text/css\"></style></head><body style=\"margin: 0px;\">" +
                    "<div style=\"border-bottom-right-radius: 60%; overflow: hidden; height: 65px; width: 100%; box-shadow: 2px 2px 10px rgba(0,0,0,0.2);\">" +
                    drip +
                    "</div>" +
                    "</body></html>";
            browser.loadDataWithBaseURL("file:///android_asset/", web, "text/html", "UTF-8", null);

            String ts = "";

            if(rain3h!=null){
                ts+= "Rain 3h: " + rain3h;
            }
            if(!ts.isEmpty()){
                ts+=" ";
            }

            if(snow3h!=null){
                ts+= "Snow 3h: " + snow3h;
            }
            onShowToast3h(view, ts);
        }

    }

    void onShowToast3h(View view, final String text){
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                toast.show(text, Gravity.TOP | Gravity.CLIP_HORIZONTAL);
            }
        });
    }


    private void getIcon(ImageView imageView, String icon) {

        int resId = activity.getResources().getIdentifier("i" + icon, "drawable", activity.getPackageName());
        if (resId != 0) {
            imageView.setBackgroundResource(resId);
        } else {
            Log.d(TAG, "Icon not exist " + icon);
            new IconForecast(imageView, icon);
        }

    }

    private Integer getColorHour(final String HH) {
        switch (HH) {
            case "00":
                return R.color.hh00;
            case "03":
                return R.color.hh03;
            case "06":
                return R.color.hh06;
            case "09":
                return R.color.hh09;
            case "12":
                return R.color.hh12;
            case "15":
                return R.color.hh15;
            case "18":
                return R.color.hh18;
            case "21":
                return R.color.hh21;
            default:
                return R.color.hh15;
        }
    }

    void infladeDay(List<HashMap> listMap) {
        String dayWeekNum = listMap.get(0).get("dayWeekNum").toString();
        dayWeekNum = listMap.get(0).get("dayWeekNum").toString();
        int i = 0, count = listMap.size();

        HashMap<String, List> mapDays = new HashMap<>();

        List<List> listList = new ArrayList<>();


        for (HashMap<String, String> map : listMap) {
            String dN = map.get("dayWeekNum");
            List<HashMap> ll = mapDays.get(dN) == null ? new ArrayList<>() : mapDays.get(dN);
            ll.add(map);
            if (mapDays.get(dN) == null) {
                mapDays.put(dN, ll);
                listList.add(ll);
            }
        }
        final LinearLayout.LayoutParams layoutParamsFrorTitle = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsFrorTitle.setMargins(2, 2, 2, 2);
        for (List<HashMap> lm : listList) {
            LayoutInflater inflater = (LayoutInflater) (activity.getLayoutInflater());
            final View view = inflater.inflate(R.layout.coll_week, null, false);
            final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            final LinearLayout daySprite = (LinearLayout) view.findViewById(R.id.daySprite);
            final List<HashMap> _lm = lm;
            fr.post(new Runnable() {
                @Override
                public void run() {
                    fr.addView(view);
                    view.setLayoutParams(layoutParams);


                    TextView dayTitle = (TextView) view.findViewById(R.id.dayTitle);
                    dayTitle.setLayoutParams(layoutParamsFrorTitle);
                    //firstUpperCase(hashMap.get("dayWeek")) +" "+hashMap.get("date")
                    dayTitle.setText(firstUpperCase(_lm.get(0).get("dayWeek").toString()) + " " + _lm.get(0).get("date").toString());

                }
            });
            for (HashMap<String, String> map : lm) {
                onInflate(daySprite, map);
            }
        }
        Log.d(TAG, "od" + listList.toString());
        Log.d(TAG, "od" + mapDays.toString());
    }

    private String firstUpperCase(String word) {
        if (word == null || word.isEmpty()) return "";//или return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
