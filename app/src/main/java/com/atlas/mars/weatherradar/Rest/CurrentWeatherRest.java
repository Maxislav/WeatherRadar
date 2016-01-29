package com.atlas.mars.weatherradar.Rest;

import android.util.Log;

import com.atlas.mars.weatherradar.BuildConfig;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by mars on 1/29/16.
 */
public class CurrentWeatherRest {
    private final String TAG = "CurrentWeatherRestLog";
    OnAccept onAccept;
    String cityId;
    Param param;


    public CurrentWeatherRest(OnAccept onAccept, String cityId) {
        this.onAccept = onAccept;
        this.cityId = cityId;
        param = new Param();

        myTask();
    }

    private interface Constant {
        public String URL = BuildConfig.CURRENT_WEATHER_URL;
    }

    private interface MyApiEndpointInterface {
        @GET("/")
        void getCurrentWeather(@Query("id") String cityId, @Query("APPID") String appid, @Query("units") String units, Callback<Result> cb);
    }

    public interface OnAccept {
        void accept(Param param);
    }

    void myTask() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constant.URL)
                .build();
        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);

        apiService.getCurrentWeather(cityId, BuildConfig.APPID, "metric", new Callback<Result>() {
            @Override
            public void success(Result result, Response response) {
                param.setName(result.getName());
                param.setIcon(result.getWeather().getIcon());
                param.setHumidity(result.getMain().getHumidity());
                param.setTemp(result.getMain().getTemp());
                param.setSpeed(result.getWind().getSpeed());
                param.setDeg(result.getWind().getDeg());
                param.setWind(result.getWind().getSpeed(), result.getWind().getDeg());
                onAccept.accept(param);
            }
            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "RetrofitError error", error);
            }
        });
    }

    public class Param {
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        String icon;
        String name;

        public String getHumidity() {
            return humidity+"%";
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }

        String humidity;


        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }

        double temp;


        String speed;

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        String deg;

        public String getDeg() {
            return deg;
        }

        public void setDeg(String deg) {
            this.deg = deg;
        }

        public String getWind() {
            return wind;
        }

        public void setWind(String speed, String deg) {
            this.wind = speed+"m/s "+ deg;
        }

        String wind;


    }

    private class Result {
        String name;

        public Weather getWeather() {
            return weather.get(0);
        }

        List<Weather> weather;

        public String getName() {
            return name;
        }

        Main main;

        public Main getMain() {
            return main;
        }
        Wind wind;

        public Wind getWind() {
            return wind;
        }
    }

    private class Wind{
        public String getSpeed() {
            return speed;
        }

        String speed;

        public String getDeg() {
            return deg;
        }

        String deg;


    }

    private class Main {
        public double getTemp() {
            return temp;
        }
        double temp;
        public String getHumidity() {
            return humidity;
        }
        String humidity;
    }

    private class Weather {
        String icon;

        public String getIcon() {
            return icon;
        }
    }
}
