package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by mars on 8/6/15.
 */
public class CurrentWeather extends Fragment implements OnLocation {

    //private final String TAG =  "CurrentWeather"
    MainActivity mainActivity;
    Activity activity;
    Context context;
    FrameLayout frLayoutCurrent;
    final String TAG = "CurrentWeatherLogs";
    public LocationManager locationManagerNet;
    public LocationListener locationListenerNet;


    TextView textViewTitle, textViewTemp, textViewWind, textViewHumidity;
    ImageView imageCurrentWeather;
    CurrentWeatherTask currentWeatherTask;
    boolean isDoing = false;

    private int onTaskResult = 0;

    View weatherView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        weatherView = inflater.inflate(R.layout.fragmet_weather, null);
        textViewTemp = (TextView) weatherView.findViewById(R.id.textViewTemp);
        textViewWind = (TextView) weatherView.findViewById(R.id.textViewWind);
        textViewHumidity = (TextView) weatherView.findViewById(R.id.textViewHumidity);
        textViewTitle = (TextView) weatherView.findViewById(R.id.textViewTitle);
        imageCurrentWeather = (ImageView) weatherView.findViewById(R.id.imageCurrentWeather);
        return weatherView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        _onStart();
    }

   /* CurrentWeather(MainActivity mainActivity, FrameLayout frLayoutCurrent){
        this.mainActivity = mainActivity;
        textViewTemp = (TextView)frLayoutCurrent.findViewById(R.id.textViewTemp);
        textViewWind = (TextView)frLayoutCurrent.findViewById(R.id.textViewWind);
        textViewHumidity = (TextView)frLayoutCurrent.findViewById(R.id.textViewHumidity);
        imageCurrentWeather = (ImageView)frLayoutCurrent.findViewById(R.id.imageCurrentWeather);

    }*/

    public void _onStart() {

        locationManagerNet = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationListenerNet = new MyLocationListenerNet(this);
        locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);

    }


    @Override
    public void onLocationAccept(double lat, double lng) {
        if (locationManagerNet != null) {
            locationManagerNet.removeUpdates(locationListenerNet);
            locationManagerNet = null;
        }
        onStartWeatherTask(lat, lng);
    }


    void onStartWeatherTask(double lat, double lng) {
        if (!isDoing) {
            if (lat != 0 && lng != 0) {
                currentWeatherTask = new CurrentWeatherTask();
                currentWeatherTask.execute(MathOperation.round(lat, 4), MathOperation.round(lng, 4));
            } else {
                currentWeatherTask = new CurrentWeatherTask();
                currentWeatherTask.execute();
            }

        }
    }

    void onAccept(ObjectNode root) {

        if (root == null) {
            return;
        }
       /* Log.d(TAG, root.toString());
        switch (root.path("cod").asInt()) {
            case 404:
                mainActivity.show("City not found. Try Kiev get");
                if (onTaskResult == 0) {
                    onTaskResult++;
                    onStartWeatherTask(0, 0);
                } else {
                    mainActivity.show("Error current weather task");
                }

                return;
        }*/




        int cod = 200;
        try{
            cod= root.path("cod").asInt();
        }catch (Exception e){
            cod = 404;
            mainActivity.show("Error current weather task");
            Log.d(TAG, e.toString(), e);
        }
        if(cod==404){
            mainActivity.show("City not found. Try Kiev get");
            if(onTaskResult == 0){
                onTaskResult++;
                onStartWeatherTask(0,0);
            }else{
                mainActivity.show("Error forecast task");
            }
            return;
        }





        String icon = root.get("weather").get(0).path("icon").asText();
        String nameCity = root.path("name").asText();
        textViewTitle.setText(nameCity);
        //mainActivity.setCityName(nameCity);

        int temp = root.get("main").path("temp").asInt();
        String strtTemp = Integer.toString(temp);
        String humidity = root.get("main").path("humidity").asText() + "%";
        String wind = root.get("wind").path("speed").asText() + "m/s " + root.get("wind").path("deg").asText();

        setIcon(imageCurrentWeather, icon);

        textViewTemp.setText(strtTemp);
        textViewHumidity.setText(humidity);
        textViewWind.setText(wind);

        Log.d(TAG, root.toString());

    }


    private void setIcon(ImageView imageView, String icon) {
        Log.d(TAG, "Icon " + icon);
        int resId = mainActivity.getResources().getIdentifier("i" + icon, "drawable", mainActivity.getPackageName());
        if (resId != 0) {
            imageView.setBackgroundResource(resId);
        } else {
            new IconForecast(imageView, icon);
        }

    }


    private class CurrentWeatherTask extends AsyncTask<Double, Void, ObjectNode> {
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
                path = "http://api.openweathermap.org/data/2.5/weather?lat=" + params[0] + "&lon=" + params[1] + "&units=metric";
                ;
            } else {
                path = "http://api.openweathermap.org/data/2.5/weather?q=Kiev,UA&units=metric";
            }
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
            //  loader.hide();
            isDoing = false;
            onAccept(result);

        }

    }




}
