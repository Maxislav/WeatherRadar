package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atlas.mars.weatherradar.Rest.ForecastFiveDay;
import com.atlas.mars.weatherradar.WeatherPager.ActivityFullWeatherInfo;
import com.atlas.mars.weatherradar.alarm.LocationFromAsset;
import com.atlas.mars.weatherradar.loader.Loader;
import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mars on 8/3/15.
 */
public class Forecast implements OnLocation, ForecastFiveDay.OnAccept {
    final String TAG = "ForecastLogs";
    Activity activity;
    LinearLayout fr;

    HorizontalScrollView horizontalScroll;

    FrameLayout parent;
    public LocationManager locationManagerNet;
    public LocationListener locationListenerNet;
    Loader loader;
    ToastShow toast;
    private boolean isDoing = false;
    private static int onTaskResult = 0;
    DataBaseHelper db;
    static Context context;


    Forecast(Activity activity, LinearLayout fr) {
        db = new DataBaseHelper(activity);

        this.activity = activity;
        toast = (ToastShow) activity;
        this.fr = fr;
        parent = (FrameLayout) fr.getParent().getParent();
        loader = new Loader(activity, parent);
        context = activity.getApplicationContext();


        _onStart();


        //  onInflate();
    }

    private void _onStart() {

        String myLocation = db.mapSetting.get(db.MY_LOCATION);
        String permitsLocation = db.mapSetting.get(db.LICENCE);

        if (isNetworkAvailable()) {
            if (myLocation != null && !myLocation.equals("0")) {
                loader.show();
                new LocationFromAsset(activity, this, myLocation);
            } else {
                if (permitsLocation == null || permitsLocation.equals("0")) {
                    toast.show("Set agree licence first");
                    return;
                }
                locationManagerNet = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                if (locationManagerNet.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManagerNet.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationListenerNet = new MyLocationListenerNet(this);
                    loader.show();
                    locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
                } else {
                    /**
                     * Локация недоступна
                     */
                    //locationManagerNet.removeUpdates(locationListenerNet);
                    toast.show("Location is not available in setting");
                }
            }
        } else {
            toast.show("Network is not available");
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
        if (statusCode != 200) {
            toast.show("Open weather map say Error!");
            loader.hide(100);
            return;
        }
        if (0 < list.size()) {
            infladeDay(list);
            db.mapSetting.put(db.TIMESTAMP_FORECAST, db.getTimeStamp());
            db.saveSetting();
        } else {
            toast.show("City not found");
        }
        loader.hide(100);
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
                view.setPadding(2,2,2,2);
                layoytDay.addView(view);

               // view.setBackgroundColor(getColorHour(hashMap.get("HH")));
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
                shape.setCornerRadius(4);

                shape.setColor(activity.getResources().getColor(getColorHour(hashMap.get("HH"))));
                childView.setBackground(shape);

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent = new Intent(activity, ActivityFullWeatherInfo.class);
                        activity.startActivityForResult(intent, 2);
                        return false;
                    }
                });

            }
        });

        /*layoytDay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast.show("uueuueuwe");
                return false;
            }
        });*/


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

          /*  String packageName = context.getPackageName();
            int resId = context.getResources().getIdentifier("hh"+map.get("HH"), "color", packageName);
            String color16 =    context.getString(resId);*/


            String color16 = getStringResourceByName("color", "hh"+map.get("HH"));
            color16 = color16.replaceAll("^#.{2}", "#");



            String web = "<html><head><meta name=\"viewport\" content=\"width=device-width, minimum-scale=0.1\"><title>drip.png (20×32)</title><style type=\"text/css\"></style></head><body style=\"margin: 0px;\">" +
                    "<div style=\"position: relative; overflow: hidden; height: 65px; width: 100%; \">" +
                        "<div>" +drip +"</div>"+
                    "<div style=\"position: absolute; height: 65px; width: 100%; top: 0;left: 0; background:  linear-gradient(to bottom, rgba(0,0,0,0) 0%, "+color16+" 100%);\">"+

                    "</div>"+
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
            onWebShowToast3h(browser, ts, map);
        }
    }

    void onWebShowToast3h(WebView mWebView, final String text, final HashMap<String, String> map){

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            LongClick longClick;

            float startX, dX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.d(TAG, event.getAction() + "" );

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        toast.show(text, Gravity.TOP | Gravity.CLIP_HORIZONTAL);
                        longClick = new LongClick(text, map);
                        longClick.execute();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(longClick!=null && longClick.getStatus() == AsyncTask.Status.RUNNING){
                            longClick.setCancel();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        dX = startX - event.getRawX();
                        Log.d(TAG, "dx "+ dX + "");
                        if(1<dX || dX<-1){
                            if(longClick!=null && longClick.getStatus() == AsyncTask.Status.RUNNING){
                                longClick.setCancel();
                            }
                        }

                        break;
                }
                return false;
            }
        });
    }



    private class LongClick extends AsyncTask<Void, Void, Boolean>{

        Boolean doShow;
        String text;
        HashMap<String, String> map;

        LongClick(String text, HashMap<String, String> map){
            super();
            this.text = text;
            this.map = map;
        }

        void setCancel(){
           doShow = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            doShow = true;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return doShow;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(doShow){
                Intent intent = new Intent(activity, ActivityFullWeatherInfo.class);
                activity.startActivityForResult(intent, 2);
            }
        }
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

   /* public static Color hex2Rgb(String colorStr) {
        return new Color(Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }*/

    private class Rgb {
        int r, g, b;
        float a;

        public Rgb(String hex) {
            a = (float)Integer.parseInt(hex.substring(1, 3), 16) / 255;
            r = Integer.parseInt(hex.substring(3, 5), 16);
            g = Integer.parseInt(hex.substring(5, 7), 16);
            b = Integer.parseInt(hex.substring(7, 9), 16);
        }

    }


    public static String getStringResourceByName(String resName, String aString) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(aString, resName, packageName);
        return context.getString(resId);
    }
}
