package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atlas.mars.weatherradar.loader.Loader;
import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by mars on 8/3/15.
 */
public class Forecast implements OnLocation {
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
        if (isNetworkAvailable()) {
            locationManagerNet = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            locationListenerNet = new MyLocationListenerNet(this);
            if (locationManagerNet.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
                loader.show();
            } else {
                onForecastAccept(null);
            }
        } else {
            onForecastAccept(null);
        }
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

    ForecastGoogleApi forecastGoogleApi;

    @Override
    public void onLocationAccept(double lat, double lng) {
        Log.d(TAG, "lat lng: " + lat + " : " + lng);

        if (locationManagerNet != null) {
            locationManagerNet.removeUpdates(locationListenerNet);
            locationManagerNet = null;
        }
        if (forecastGoogleApi != null && forecastGoogleApi.getStatus() == AsyncTask.Status.RUNNING) {

        } else {
            onStartWeatherTask(lat, lng);
        }

    }

    private void onStartWeatherTask(double lat, double lng) {
        if (lat != 0 && lng != 0 && !isDoing) {
            forecastGoogleApi = new ForecastGoogleApi();
            forecastGoogleApi.execute(MathOperation.round(lat, 4), MathOperation.round(lng, 4));
        } else {
            forecastGoogleApi = new ForecastGoogleApi();
            forecastGoogleApi.execute();
        }

    }

    void onForecastAccept(ObjectNode root) {
        List<HashMap> listMap = new ArrayList<>();

        if (root == null) {
            toast.show("City not found");
            return;
        }

        int cod = 200;
        try {
            cod = root.path("cod").asInt();
        } catch (Exception e) {
            cod = 404;
            toast.show("Error forecast task");
            Log.d(TAG, e.toString(), e);
        }
        if (cod == 404) {
            toast.show("City not found. Try Kiev get");
            if (onTaskResult == 0) {
                onTaskResult++;
                onStartWeatherTask(0, 0);
            } else {
                toast.show("Error forecast task");
            }
            return;
        }

        ArrayNode list = (ArrayNode) root.get("list");
        SimpleDateFormat dayMonth = new SimpleDateFormat("dd.MM"); //2015-08-03 18:00:00
        SimpleDateFormat time = new SimpleDateFormat("HH:mm"); //2015-08-03 18:00:00
        SimpleDateFormat HH = new SimpleDateFormat("HH"); //2015-08-03 18:00:00
        SimpleDateFormat dayWeek = new SimpleDateFormat("EE");

        //  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2015-08-03 18:00:00
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        if (list != null) {
            for (JsonNode jsonNode : list) {
                HashMap<String, String> map = new HashMap<>();
                String dt_txt = jsonNode.path("dt_txt").asText();
                String temp = Integer.toString(jsonNode.get("main").path("temp").asInt());
                String main = jsonNode.get("weather").get(0).path("main").asText();
                String icon = jsonNode.get("weather").get(0).path("icon").asText();
                String rain = "", snow = "";
                if (jsonNode.get("rain") != null) {
                    rain = jsonNode.get("rain").path("3h").asText();
                }
                if (jsonNode.get("snow") != null) {
                    snow = jsonNode.get("snow").path("3h").asText();
                }

                String description = jsonNode.get("weather").get(0).path("description").asText();

                map.put("rain", rain);
                map.put("snow", snow);

                map.put("temp", temp);
                map.put("main", main);
                map.put("description", description);
                map.put("icon", icon);
                Calendar cal = new GregorianCalendar();
                try {
                    Date date = format.parse(dt_txt);
                    dayMonth.format(date);
                    time.format(date);

                    cal.setTime(date);

                    map.put("date", dayMonth.format(date));
                    map.put("time", time.format(date));
                    map.put("HH", HH.format(date));
                    map.put("dayWeek", dayWeek.format(date));
                    map.put("dayWeekNum", Integer.toString(cal.get(Calendar.DAY_OF_WEEK)));


                } catch (ParseException e) {
                    Log.e(TAG, e.toString(), e);
                    e.printStackTrace();
                }
                listMap.add(map);
            }
        }

        if (0 < listMap.size()) {
            infladeDay(listMap);
        } else {
            toast.show("City not found");
        }

        db.mapSetting.put(db.TIMESTAMP_FORECAST,  db.getTimeStamp());
        db.saveSetting();

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


    private class ForecastGoogleApi extends AsyncTask<Double, Void, ObjectNode> {
        ObjectMapper mapper = new ObjectMapper();
        HttpURLConnection urlConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isDoing = true;
        }

        @Override
        protected ObjectNode doInBackground(Double... params) {
            URL url;
            String path;
            if (params.length == 2) {
                path = "http://api.openweathermap.org/data/2.5/forecast?lat=" + params[0] + "&lon=" + params[1] + "&APPID=" + BuildConfig.APPID + "&units=metric";
            } else {
                path = "http://api.openweathermap.org/data/2.5/forecast?q=Kiev,UA&units=metric";
            }

            StringBuilder sb = new StringBuilder();
            try {
                //http://api.openweathermap.org/data/2.5/forecast?lat=35&lon=139
                url = new URL(path);
                urlConnection = (HttpURLConnection) url.openConnection();
                //   urlConnection.setDoOutput(true);
                Scanner inStream = new Scanner(urlConnection.getInputStream());
                while (inStream.hasNextLine()) {
                    sb.append(inStream.nextLine());
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            ObjectNode root = null;
            if (0 < sb.length()) {
                String json = sb.toString();
                try {
                    // [{"color":"4793F8","colorRgb":"71 147 248","intensity":6,"dist":75,"xy":"153 164"},{"color":"9BE1FF","colorRgb":"155 225 255","intensity":5,"dist":75,"xy":"159 159"},{"color":"0C59FF","colorRgb":"12 89 255","intensity":7,"dist":78,"xy":"151 161"},{"color":"FF8C9B","colorRgb":"255 140 155","intensity":9,"dist":80,"xy":"151 158"},{"color":"9BEA8F","colorRgb":"155 234 143","intensity":2,"dist":108,"xy":"122 140"}]
                    root = (ObjectNode) mapper.readTree(json);
                } catch (IOException e) {
                    Log.e(TAG, e.toString(), e);
                    e.printStackTrace();
                }
            }


            return root;
        }

        @Override
        protected void onPostExecute(ObjectNode result) {
            isDoing = false;
            loader.hide();
            onForecastAccept(result);
        }
    }

    private String firstUpperCase(String word) {
        if (word == null || word.isEmpty()) return "";//или return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
   /* private double round(double d, int prec) {
        return new BigDecimal(d).setScale(prec, RoundingMode.UP).doubleValue();
    }*/

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
