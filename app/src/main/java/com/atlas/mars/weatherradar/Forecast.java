package com.atlas.mars.weatherradar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by mars on 8/3/15.
 */
public class Forecast implements OnLocation, ForecastFiveDay.OnAccept, ActivityCompat.OnRequestPermissionsResultCallback {
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
    public static List<HashMap> list;
    private static String packageName;


    Forecast(Activity activity, LinearLayout fr) {
        db = new DataBaseHelper(activity);

        this.activity = activity;
        toast = (ToastShow) activity;
        this.fr = fr;
        parent = (FrameLayout) fr.getParent().getParent();
        loader = new Loader(activity, parent);
        context = activity.getApplicationContext();
        packageName = context.getPackageName();


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
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        ActivityCompat.requestPermissions(this.activity,
                                new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                },
                                1);


                        return;
                    }
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
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.





                return;
            }
            locationManagerNet.removeUpdates(locationListenerNet);
            locationManagerNet = null;
        }
        new ForecastFiveDay(this, lat, lng);

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
            this.list = list;

            inflateDay(list);
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


    void onInflate(final LinearLayout layoytDay, final HashMap<String, String> map, final int iDay) {

        Timer timing = new Timer();
        timing.schedule(new TimerTask() {

            @Override
            public void run() {
                layoytDay.post(new Runnable() {
                    final HashMap<String, String> hashMap = map;

                    @Override
                    public void run() {
                        LayoutInflater inflater = (LayoutInflater) (activity.getLayoutInflater());
                        int width = (int) (80 * Density.density);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
                        View view = inflater.inflate(R.layout.forecast_container, null, false);
                        //view.findViewById(R.id.overLayout).setOnLongClickListener(new  MyLongClick(map));
                        inflateWebDrip(view, map, iDay);
                        view.setPadding(2, 2, 2, 2);
                        layoytDay.addView(view);

                        // view.setBackgroundColor(getColorHour(hashMap.get("HH")));
                        // ((TextView) view.findViewById(R.id.textDate)).setText( firstUpperCase(hashMap.get("dayWeek")) +" "+hashMap.get("date"));

                        ((TextView) view.findViewById(R.id.textTime)).setText(hashMap.get("time"));
                        ((TextView) view.findViewById(R.id.textTemp)).setText(hashMap.get("temp"));

                        if(hashMap.get("wind_speed")!=null){
                            double degText = Double.parseDouble(hashMap.get("wind_speed"));

                            degText = MathOperation.round(degText, 1);
                            ((TextView)view.findViewById(R.id.textViewWindSpeed)).setText( ""+degText+"m/s");
                        }


                       // ((TextView) view.findViewById(R.id.textViewWindSpeed)).setText(hashMap.get("wind_speed"));



                        if(hashMap.get("wind_deg")!=null){
                            ((TextView)view.findViewById(R.id.textViewWindArrow)).setText(""+ (char)0x2191);
                            ((TextView)view.findViewById(R.id.textViewWindArrow)).setRotation(Float.parseFloat(hashMap.get("wind_deg")));
                        }


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

                /*view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent = new Intent(activity, ActivityFullWeatherInfo.class);
                        activity.startActivityForResult(intent, 2);
                        return false;
                    }
                });
*/
                    }
                });
            }
        }, 50 * iDay);




        /*layoytDay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast.show("uueuueuwe");
                return false;
            }
        });*/


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            Log.d(TAG, "olol");
    }

    private class MyLongClick implements View.OnLongClickListener {
        HashMap<String, String> map;
        int iDay;

        MyLongClick(HashMap<String, String> map, int iDay) {
            this.map = map;
            this.iDay = iDay;
        }

        @Override
        public boolean onLongClick(View v) {
            // toast.show("onLongClick");

            Intent intent = new Intent(activity, ActivityFullWeatherInfo.class);
            //intent.putExtra("list", list);
            intent.putExtra("iDay", iDay);
            activity.startActivityForResult(intent, 2);

            return false;
        }
    }

    private class MyClick implements View.OnClickListener {
        String ts;

        MyClick(String ts) {
            this.ts = ts;
        }

        @Override
        public void onClick(View v) {
            toast.show(ts, Gravity.TOP | Gravity.CLIP_HORIZONTAL);
        }
    }

    void inflateWebDrip(View view, final HashMap<String, String> map, final int iDay) {
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

        if (isNeedCreate) {

          /*  String packageName = context.getPackageName();
            int resId = context.getResources().getIdentifier("hh"+map.get("HH"), "color", packageName);
            String color16 =    context.getString(resId);*/


            String color16 = getStringResourceByName("color", "hh" + map.get("HH"));
            color16 = color16.replaceAll("^#.{2}", "#");

            String clouds = map.get("clouds");

            String style ="<style type=\"text/css\">" +
                    ".drip-container{position: absolute; top: 50px; left:0}"+
                    "</style>";


            String web = "<html><head><meta name=\"viewport\" content=\"width=device-width, minimum-scale=0.1\"><title>drip.png (20×32)</title>"+style+"</head><body style=\"margin: 0px;\">" +
                    "<div style=\"position: relative; overflow: hidden; height: 65px; width: 100%; \">" +
                    "<div class=\"drip-container\">" + drip + "</div>" +
                    "<img src=\"clouds.png\" style='position:absolute; left:0; bottom: 0; width:100%; height:80%; transform: scale(0."+clouds+"); transform-origin: 10% 60%'>"+
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
            view.findViewById(R.id.overLayout).setOnClickListener(new MyClick(ts));
        }
        view.findViewById(R.id.overLayout).setOnLongClickListener(new MyLongClick(map, iDay));
    }


    private class LongClick extends AsyncTask<Void, Void, Boolean> {

        Boolean doShow;
        String text;
        HashMap<String, String> map;

        LongClick(String text, HashMap<String, String> map) {
            super();
            this.text = text;
            this.map = map;
        }

        void setCancel() {
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
            if (doShow) {
                //Intent intent = new Intent(activity, ActivityFullWeatherInfo.class);
                //activity.startActivityForResult(intent, 2);
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

    public static Integer getColorHour(final String HH) {
        switch (HH) {
            case "00":
                return R.color.hh00;
            case "04":
                return R.color.hh04;
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

    void inflateDay(List<HashMap> listMap) {
        String dayWeekNum = listMap.get(0).get("dayWeekNum").toString();
        dayWeekNum = listMap.get(0).get("dayWeekNum").toString();
        int i = 0, count = listMap.size();
        int iDay = 0;

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
            fr.addView(view);
            view.setLayoutParams(layoutParams);
            TextView dayTitle = (TextView) view.findViewById(R.id.dayTitle);
            dayTitle.setLayoutParams(layoutParamsFrorTitle);
            dayTitle.setText(firstUpperCase(_lm.get(0).get("dayWeek").toString()) + " " + _lm.get(0).get("date").toString());

            for (HashMap<String, String> map : lm) {
                onInflate(daySprite, map, iDay);
                iDay++;
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
            a = (float) Integer.parseInt(hex.substring(1, 3), 16) / 255;
            r = Integer.parseInt(hex.substring(3, 5), 16);
            g = Integer.parseInt(hex.substring(5, 7), 16);
            b = Integer.parseInt(hex.substring(7, 9), 16);
        }

    }


    public static String getStringResourceByName(String resName, String aString) {
        //String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(aString, resName, context.getPackageName());
        String result = "";
        try {
            result =context.getString(resId);
        }catch (Exception e){
            result = "#c0d3fa";
        }
        return result;
    }


}
