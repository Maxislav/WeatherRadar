package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import java.util.Date;
import java.util.HashMap;
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

    Forecast(Activity activity, LinearLayout fr) {
        this.activity = activity;
        this.fr = fr;
        parent = (FrameLayout)fr.getParent().getParent();
        loader = new Loader(activity, parent);

        locationManagerNet = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationListenerNet = new Location(this);
        locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
        loader.show();
        //  onInflate();
    }


    void onInflate(HashMap<String, String> hashMap) {
        LayoutInflater inflater = (LayoutInflater) (activity.getLayoutInflater());
        int width = (int) (80 * Density.density);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);

        View view = inflater.inflate(R.layout.forecast_container, null, false);
        fr.addView(view);

        ((TextView) view.findViewById(R.id.textDate)).setText(hashMap.get("date"));
        ((TextView) view.findViewById(R.id.textTime)).setText(hashMap.get("time"));
        ((TextView) view.findViewById(R.id.textTemp)).setText(hashMap.get("temp"));
        view.setLayoutParams(layoutParams);

    }

    @Override
    public void onLocationAccept(double lat, double lng) {
        Log.d(TAG, "lat lng: " + lat + " : " + lng);
        // onInflate();
        if (locationManagerNet != null) {
            locationManagerNet.removeUpdates(locationListenerNet);
        }
        ForecastGoogleApi forecastGoogleApi = new ForecastGoogleApi();
        forecastGoogleApi.execute(lat, lng);
    }

    void onForecastAccept(ObjectNode root) {

        ArrayNode list = (ArrayNode) root.get("list");
        SimpleDateFormat dayMonth = new SimpleDateFormat("dd.MM"); //2015-08-03 18:00:00
        SimpleDateFormat time = new SimpleDateFormat(" HH:mm"); //2015-08-03 18:00:00
        //  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2015-08-03 18:00:00
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        for (JsonNode jsonNode : list) {
            HashMap<String, String> map = new HashMap<>();
            String dt_txt = jsonNode.path("dt_txt").asText();
            String temp = jsonNode.get("main").path("temp").asText();
            String main =  jsonNode.get("weather").get(0).path("main").asText();
            String description =  jsonNode.get("weather").get(0).path("description").asText();
            map.put("temp", temp);
            map.put("main", main);
            map.put("description", description);

            try {
                Date date = format.parse(dt_txt);
                dayMonth.format(date);
                time.format(date);

                map.put("date", dayMonth.format(date));
                map.put("time", time.format(date));


            } catch (ParseException e) {
                Log.e(TAG, e.toString(), e);
                e.printStackTrace();
            }
            onInflate(map);

        }
        loader.hide();
        Log.d(TAG, "olo");
        Log.d(TAG, "olo");
    }


    private class Location extends MyLocationListenerNet {
        public Location(Object myService) {
            super(myService);
        }

        @Override
        public void onCallback(double lat, double lng) {
            onLocationAccept(lat, lng);
        }
    }

    private class ForecastGoogleApi extends AsyncTask<Double, Void, ObjectNode> {
        ObjectMapper mapper = new ObjectMapper();
        HttpURLConnection urlConnection;

        @Override
        protected ObjectNode doInBackground(Double... params) {
            URL url;
            String path = "http://api.openweathermap.org/data/2.5/forecast?lat=" + params[0] + "&lon=" + params[1] + "&units=metric";
            StringBuilder sb = new StringBuilder();
            try {
                //http://api.openweathermap.org/data/2.5/forecast?lat=35&lon=139
                url = new URL(path);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                Scanner inStream = new Scanner(urlConnection.getInputStream());
                while (inStream.hasNextLine()) {
                    sb.append(inStream.nextLine());
                    // response += (inStream.nextLine());
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
            onForecastAccept(result);
        }
    }
}
