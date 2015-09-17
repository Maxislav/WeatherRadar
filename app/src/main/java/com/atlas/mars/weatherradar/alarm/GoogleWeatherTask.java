package com.atlas.mars.weatherradar.alarm;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Администратор on 8/2/15.
 */
public class GoogleWeatherTask extends AsyncTask<Double, Void, HashMap<String, Boolean>> {
    final String TAG = "GoogleWeatherTaskLogs";

    MyService myService;
    HttpURLConnection urlConnection;
    static ObjectMapper mapper = new ObjectMapper();
    GoogleWeatherTask(MyService myService) {
        super();
        this.myService = myService;
    }

    @Override
    protected HashMap<String, Boolean> doInBackground(Double... params) {
        URL url;
        String path = "http://api.openweathermap.org/data/2.5/forecast?lat=" + params[0] + "&lon=" + params[1]+"&cnt=3";
        StringBuilder sb = new StringBuilder();
        HashMap<String, Boolean> map = new HashMap<>();
        map.put("rain",false);
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
            map.put("rain", true);
            return map;

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if (0<sb.length()) {
            String json = sb.toString();
            ObjectNode root;
            try {
                 root = (ObjectNode) mapper.readTree(json);
            } catch (IOException e) {
                map.put("rain", true);
                Log.e(TAG, e.toString(), e);
                e.printStackTrace();
                return map;
            }


            try {
                // [{"color":"4793F8","colorRgb":"71 147 248","intensity":6,"dist":75,"xy":"153 164"},{"color":"9BE1FF","colorRgb":"155 225 255","intensity":5,"dist":75,"xy":"159 159"},{"color":"0C59FF","colorRgb":"12 89 255","intensity":7,"dist":78,"xy":"151 161"},{"color":"FF8C9B","colorRgb":"255 140 155","intensity":9,"dist":80,"xy":"151 158"},{"color":"9BEA8F","colorRgb":"155 234 143","intensity":2,"dist":108,"xy":"122 140"}]
                ArrayNode arrayNode = (ArrayNode)root.get("list");
                int forecastCount = arrayNode.size();
                String clouds;
                String description;
                String dt_txt;
                String rain = "";
                String descriptionMain;
                Log.d(TAG, "\n Path: "+ path);

                for(int i = 0; i<forecastCount; i++){
                    clouds =  arrayNode.get(i).get("clouds").path("all").asText();
                    description =  arrayNode.get(i).get("weather").get(0).path("description").asText();
                    descriptionMain =  arrayNode.get(i).get("weather").get(0).path("main").asText();
                    dt_txt = arrayNode.get(i).path("dt_txt").asText();
                    if(arrayNode.get(i).get("rain")!=null){
                        rain = arrayNode.get(i).get("rain").path("3h").asText();
                    }
                    if(descriptionMain.equals("Rain") || descriptionMain.equals("Snow")){
                        map.put("rain",true);
                        break;
                    }

                    Log.d(TAG, clouds+ " : "+ descriptionMain+" : " +description+ " :  "+ dt_txt + " : " + rain) ;
                }
            } catch (Exception e) {
                map.put("rain", true);
                Log.e(TAG, e.toString(), e);
                e.printStackTrace();
            }
        }

        return map;
    }
    @Override
    protected void onPostExecute(HashMap result) {
        //Log.d(TAG, result);
        myService.onGoogleWeatherTaskResult(result);
       // myService.onBorispolTaskResult(result);
    }
}
