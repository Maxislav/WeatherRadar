package com.atlas.mars.weatherradar.Rest;

import android.util.Log;

import com.atlas.mars.weatherradar.BuildConfig;
import com.atlas.mars.weatherradar.alarm.MyService;

import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by mars on 1/28/16.
 */
public class BorispolRest {
    private static String TAG = "BorispolRestLog";
    MyService myService;
    String cityId;
    Integer intensity;
    Integer distance;
    HashMap<String, Integer> map;

    private interface Constants {
        public String URL = BuildConfig.BorispolParseRain;
    }

    private interface MyApiEndpointInterface {
        @GET("/")
        void getDistance(@Query("id") String cityId, Callback<List<CustomObject>> cb);
    }

    public BorispolRest(MyService myService, String cityId) {
        this.myService = myService;
        this.cityId = cityId;
        map = new HashMap<String, Integer>();
        myTask();
    }

    void onCallback(HashMap<String, Integer> map) {
        myService.onBorispolTaskResult(map);
    }

    void myTask() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.URL)
                .build();

        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);

        apiService.getDistance(cityId, new Callback<List<CustomObject>>() {
            @Override
            public void success(List<CustomObject> customObjects, Response response) {
                List<CustomObject> _customObjects = customObjects;
                distance = MyService.alarmMinDist;
                for (CustomObject customObject : customObjects) {
                    if (distance == null || customObject.getDist() < distance) {
                        distance = customObject.getDist();
                        intensity = customObject.getIntensity();
                        map.put("dist", distance);
                        map.put("intensity", intensity);
                    }
                    map.put("isIntensity", intensity);
                }
                onCallback(map);
            }

            @Override
            public void failure(RetrofitError error) {
                map.put("isIntensity", 5);
                onCallback(map);
                Log.e(TAG, "RetrofitError error", error);
            }
        });
    }

    private class CustomObject {
        public String color;
        Integer intensity;
        Integer dist;

        public Integer getDist() {
            return dist;
        }

        public String getColor() {
            return color;
        }

        public Integer getIntensity() {
            return intensity;
        }
    }
}
