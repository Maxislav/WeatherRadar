package com.atlas.mars.weatherradar.Rest;

import android.util.Log;

import com.atlas.mars.weatherradar.MathOperation;

import java.util.HashMap;
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
    DayForecastRain.Callbackwqw callbackwqw;

    public DayForecastRain(DayForecastRain.Callbackwqw callbackwqw, Double... latLng){
        this.callbackwqw = callbackwqw;
        if(1<latLng.length){
            this.lat = MathOperation.round(latLng[0], 2);
            this.lng= MathOperation.round(latLng[1], 2);
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
                    HashMap<String, Object > map = new HashMap<String, Object>();
                    List<Item> items = success.getItems();
                    String name = success.getCity().getName();
                    for(Item it : items){
                        Log.d(TAG, it.dt+" : "+it.weather.get(0).main);
                    }

                    map.put("name",name);
                    map.put("list", items);

                    Log.d(TAG, items.toString());
                    callbackwqw.Success();
                   // success.consoleLog(response);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.d(TAG, retrofitError.toString());
                    callbackwqw.Success();
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
        public List<Item> list;
        City city;
        public List<Item> getItems() {
            return list;
        }
        public City getCity() {
            return city;
        }
    }

    class City{
        String name;
        public String getName() {
            return name;
        }
    }
    private class Item{
        long dt;
        List<Weather> weather;
        public long getDt() {
            return dt;
        }
        public Weather getWeather() {
            return weather.get(0);
        }
    }
    class Weather{
        String main;
        public String getMain() {
            return main;
        }
    }

    public interface Callbackwqw{
        public void Success();
    }

}
