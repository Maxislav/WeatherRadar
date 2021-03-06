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
    String lat, lng;
    String deviceId ="0000";


    private interface Constants {
        public String URL = BuildConfig.BorispolParseRain;
    }

    private interface MyApiEndpointInterface {
        @GET("/")
        void getDistanceById(@Query("id") String cityId, @Query("deviceId") String deviceId,  Callback<Result> cb);

        @GET("/")
        void getDistanceByLatLng(@Query("lat") String lat, @Query("lng") String lng, @Query("deviceId") String deviceId, Callback<Result> cb);
    }

    public BorispolRest(MyService myService, String cityId) {
        this.myService = myService;
        this.cityId = cityId;
        map = new HashMap<String, Integer>();
        myTaskByCityId();
    }
    public BorispolRest(MyService myService, double lat, double lng) {
        this.myService = myService;
        map = new HashMap<String, Integer>();
        this.lat = String.valueOf(lat);
        this.lng = String.valueOf(lng);
        myTaskByLatLng();
    }

    void onCallback(HashMap<String, Integer> map) {
        myService.onBorispolTaskResult(map);
    }

    void myTaskByLatLng(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.URL)
                .build();

        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);

        apiService.getDistanceByLatLng(lat, lng, deviceId, new Callback<Result>() {
            @Override
            public void success(Result result, Response response) {
                Success(result);
            }

            @Override
            public void failure(RetrofitError error) {
                map.put("isIntensity", 5);
                map.put("isRainy",1);
                map.put("error", 1);
                onCallback(map);
                Log.e(TAG, "RetrofitError error", error);
            }
        });
    }
    void myTaskByCityId() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.URL)
                .build();

        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);

        apiService.getDistanceById(cityId, deviceId, new  Callback<Result>() {
            @Override
            public void success(Result result, Response response) {
                Success(result);
            }

            @Override
            public void failure(RetrofitError error) {
                map.put("isRainy",1);
                map.put("isIntensity", 5);
                map.put("error", 1);
                onCallback(map);
                Log.e(TAG, "RetrofitError error", error);
            }
        });
    }

    void Success(Result result){


        List<CustomObject> customObjects = result.getDist();


        distance = MyService.alarmMinDist;
        for (CustomObject customObject : customObjects) {
            intensity = customObject.getIntensity();
            if(0<intensity){
                map.put("rainBorispol", intensity);
            }
            if (distance == null || customObject.getDist() < distance) {
                //запрет срабатывания на слоистую облачность
                if(intensity!=5){
                    distance = customObject.getDist();
                    map.put("dist", distance);
                    map.put("intensity", intensity);
                }
            }
            map.put("isIntensity", intensity);
        }
        map.put("isRainy", result.getIsRainy() ? 1:0);
        onCallback(map);
    }

    private class Result{
        public  List <CustomObject> dist;
        public  boolean isRainy;

        public boolean getIsRainy() {
            return isRainy;
        }

        public List<CustomObject> getDist() {
            return dist;
        }
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
