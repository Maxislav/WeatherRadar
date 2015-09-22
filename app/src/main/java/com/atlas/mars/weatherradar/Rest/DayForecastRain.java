package com.atlas.mars.weatherradar.Rest;

import android.util.Log;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by mars on 9/18/15.
 */
public class DayForecastRain {

    private static String TAG =  "DayForecastRain";
    Double lat,  lng;

    public DayForecastRain(Double... latLng){
        if(1<latLng.length){
            this.lat = latLng[0];
            this.lng= latLng[1];
        }
        myTask();
    }

    public interface Constants{
        //  path = "http://api.openweathermap.org/data/2.5/weather?q=Kiev,UA&units=metric";

        //public String URL = "api.openweathermap.org/data/2.5/weather?q=Kiev,UA&units=metric";

       // public String URL = "http://api.openweathermap.org/data/2.5/forecast?lat=35&lon=139";
        public String URL = "http://api.openweathermap.org/data/2.5/forecast";
    }



    void myTask(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.URL)  //call your base url
                .build();


        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);


        if(lat!=null && lng!=null){
            apiService.getForecastByLatLng(lat, lng,"metric", new Callback<Success>() {
                @Override
                public void success(Success success, Response response) {
                    // Access user here after response is parsed
                   // float lat = success.getCoord().getLat();
                    List<Item> items = success.getItems();
                    Log.d(TAG, items.toString());

                   // success.consoleLog(response);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.d(TAG, retrofitError.toString());
                    // Log error here since request failed
                }
            });
        }


    }
    public interface MyApiEndpointInterface {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter

        @GET("/")
        void getForecastByLatLng(@Query("lat") double lat, @Query("lon") double lon , @Query("units") String units, Callback<Success> cb);

        @GET("/")
        void getWeather(@Query("q") String cityCode, @Query("units") String units, Callback<Success> cb);

        @GET("/users/{username}")
        void getUser(@Path("username") String username, Callback<Success> cb);

        @GET("/group/{id}/users")
        void groupList(@Path("id") int groupId, @Query("sort") String sort, Callback<List<Success>> cb);

        @POST("/users/new")
        void createUser(@Body Success user, Callback<Success> cb);
    }



    private class Success{

        public List<Item> getItems() {
            return items;
        }

        public List<Item> items;

    }

    private class Item{
        public long getDt() {
            return dt;
        }
        long dt;

    }

}
