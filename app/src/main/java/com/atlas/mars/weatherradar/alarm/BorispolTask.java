package com.atlas.mars.weatherradar.alarm;

import android.os.AsyncTask;
import android.util.Log;

import com.atlas.mars.weatherradar.BuildConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Администратор on 8/2/15.
 */
public class BorispolTask extends AsyncTask<Double, Void, HashMap<String, Integer>> {
    final String TAG = "BorispolTaskLogs";
    HttpURLConnection urlConnection;
    static ObjectMapper mapper = new ObjectMapper();
    MyService myService;
    int alarmMinDist = MyService.alarmMinDist;

    BorispolTask(MyService myService) {
        super();
        this.myService = myService;

    }

    @Override
    protected HashMap<String, Integer> doInBackground(Double... params) {
        int _intensity = 0, _dist = 0;
        HashMap<String, Integer> map = new HashMap<>();
        URL url = null;
        InputStream in = null;
        StringBuilder sb = new StringBuilder();
        try {
            url = new URL(BuildConfig.BorispolParseRain+"?lat=" + params[0] + "&lng=" + params[1]);
            urlConnection = (HttpURLConnection) url.openConnection();
           // urlConnection.setDoOutput(true);
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


        if (0 < sb.length()) {
            String json = sb.toString();
            Log.d(TAG, json);
            try {
                // [{"color":"4793F8","colorRgb":"71 147 248","intensity":6,"dist":75,"xy":"153 164"},{"color":"9BE1FF","colorRgb":"155 225 255","intensity":5,"dist":75,"xy":"159 159"},{"color":"0C59FF","colorRgb":"12 89 255","intensity":7,"dist":78,"xy":"151 161"},{"color":"FF8C9B","colorRgb":"255 140 155","intensity":9,"dist":80,"xy":"151 158"},{"color":"9BEA8F","colorRgb":"155 234 143","intensity":2,"dist":108,"xy":"122 140"}]
                ArrayNode root = (ArrayNode) mapper.readTree(json);
                for (JsonNode jsonNode : root) {
                    int dist = jsonNode.path("dist").asInt();
                    int intensity = jsonNode.path("intensity").asInt();
                    map.put("isIntensity", intensity);
                    if (_intensity < intensity && dist < alarmMinDist) {
                        _intensity = intensity;
                        map.put("dist", dist);
                        map.put("intensity", intensity);
                    }

                }

            } catch (IOException e) {
                Log.e(TAG, e.toString(), e);
                map.put("error", 1);
                e.printStackTrace();
            }
        }


        return map;
    }

    @Override
    protected void onPostExecute(HashMap result) {
        //Log.d(TAG, result);
        myService.onBorispolTaskResult(result);
    }
}
