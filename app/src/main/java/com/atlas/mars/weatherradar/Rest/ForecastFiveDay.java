package com.atlas.mars.weatherradar.Rest;

import android.util.Log;

import com.atlas.mars.weatherradar.BuildConfig;
import com.atlas.mars.weatherradar.MathOperation;
import com.atlas.mars.weatherradar.alarm.MyService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by mars on 1/29/16.
 */
public class ForecastFiveDay {
    private final String TAG = "ForecastFiveDayLog";
    OnAccept onAccept;
    String cityId;
    String lat, lng;
    List<HashMap> list;
    Integer cnt;
    MyService myService;

    public ForecastFiveDay(OnAccept onAccept, String cityId, Integer cnt) {
        this.onAccept = onAccept;
        this.cityId = cityId;
        this.cnt = cnt;
        list = new ArrayList<>();
        restByCityId();
    }

    public ForecastFiveDay(OnAccept onAccept, double lat, double lng, Integer cnt) {
        this.onAccept = onAccept;
        this.cnt = cnt;
        this.lat = String.valueOf(MathOperation.round(lat, 4));
        this.lng = String.valueOf(MathOperation.round(lng, 4));
        restByLatLng();
    }

    /**
     * Для фонового процесса по сити ид
     * @param myService
     * @param cityId
     * @param cnt
     */
    public ForecastFiveDay(MyService myService, String cityId, Integer cnt) {
        this.myService = myService;
        this.cityId = cityId;
        this.cnt = cnt;
        list = new ArrayList<>();
        restByCityId();
    }

    /**
     * Для фонового процесса по координатам
     * @param myService
     * @param lat
     * @param lng
     * @param cnt
     */
    public ForecastFiveDay(MyService myService, double lat, double lng, Integer cnt) {
        this.myService = myService;
        this.cnt = cnt;
        this.lat = String.valueOf(MathOperation.round(lat, 4));
        this.lng = String.valueOf(MathOperation.round(lng, 4));
        restByLatLng();
    }

    private interface Constant{
        String URL = BuildConfig.URL_API_OPENWEATHERMAP;
    }
    private interface MyApiEndpointInterface {
        @GET("/forecast")
        void getForecastById(@Query("id") String cityId,@Query("cnt") Integer cnt, @Query("APPID") String appid, @Query("units") String units, Callback<Result> cb);

        @GET("/forecast")
        void getForecastByLatLng(@Query("lat") String lat, @Query("lon") String lon, @Query("cnt") Integer cnt, @Query("APPID") String appid, @Query("units") String units, Callback<Result> cb);
    }

    void restByCityId() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constant.URL)
                .build();
        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);
        apiService.getForecastById(cityId, cnt, BuildConfig.APPID, "metric", new Callback<Result>() {

            @Override
            public void success(Result result, Response response) {
                Log.d(TAG, "getForecastById result ok");
                if(myService!=null){
                    onResultForService (result);
                }else {
                    Success(result);
                }
            }

            @Override
            public void failure(RetrofitError error) {
               // Log.e(TAG, "RetrofitError error", error);
                if(myService!=null) {
                    onResultForService(null);
                }
            }
        });

    }

    void restByLatLng() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(BuildConfig.FORECAST_URL)
                .build();
        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);
        apiService.getForecastByLatLng(lat, lng, cnt, BuildConfig.APPID, "metric", new Callback<Result>() {

            @Override
            public void success(Result result, Response response) {
                Log.d(TAG, "restByLatLng result ok");
                if(myService!=null){
                    onResultForService (result);
                }else {
                    Success(result);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if(myService!=null) {
                    onResultForService(null);
                }
                Log.e(TAG, "RetrofitError error", error);
            }
        });
    }
    void onResultForService(Result result){
        HashMap<String, Boolean> map = new HashMap<>();
        map.put("rain", true);
        if(result!=null){
            map.put("rain", false);
            for (Item item : result.getList()) {
                if(item.getRain()!=null && item.getRain().getD3h()!=null && !item.getRain().getD3h().isEmpty()){
                    map.put("rain", true);
                }
                if(item.getSnow()!=null && item.getSnow().getD3h()!=null && !item.getSnow().getD3h().isEmpty()){
                    map.put("rain", true);
                }
            }
        }

        myService.onForecastFiveDayResult(map);
    }

    void Success(Result result) {
        list = new ArrayList<>();
        String cityName = result.getCity().getName();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat _date = new SimpleDateFormat("dd.MM"); //18.01
        SimpleDateFormat dayOfMonth = new SimpleDateFormat("dd"); //18
        SimpleDateFormat time = new SimpleDateFormat("HH:mm"); //2015-08-03 18:00:00
        SimpleDateFormat HH = new SimpleDateFormat("HH"); //2015-08-03 18:00:00
        SimpleDateFormat dayWeek = new SimpleDateFormat("EE");
        for (Item item : result.getList()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("icon", item.getWeather().getIcon());
            Calendar cal = new GregorianCalendar();
            String snow="", rain="";
            if(item.getRain()!=null && item.getRain().getD3h()!=null){
                rain = item.getRain().getD3h()+"";
                Log.d(TAG,item.getRain().getD3h()+"");
            }
            if(item.getSnow()!=null && item.getSnow().getD3h()!=null){
                snow = item.getSnow().getD3h()+"";
                Log.d(TAG,item.getSnow().getD3h()+"");
            }
            double dTemp = item.getMain().getTemp();

            map.put("snow", snow);
            map.put("rain", rain);
            map.put("temp", (0<dTemp ? "+": "")+ String.valueOf(MathOperation.round(item.getMain().getTemp(),1)));

            try {
                Date date = format.parse(item.getDt_txt());
                cal.setTime(date);
                map.put("date", _date.format(date));
                map.put("dayOfMonth", dayOfMonth.format(date));
                map.put("time", time.format(date));
                map.put("HH", HH.format(date));
                map.put("dayWeek", dayWeek.format(date));

                map.put("dayWeekNum", Integer.toString(cal.get(Calendar.DAY_OF_WEEK)));
            } catch (ParseException e) {
                Log.e(TAG, e.toString(), e);
            }

            map.put("dt_txt", item.getDt_txt());
            list.add(map);
        }
        onAccept.accept(list, cityName);
    }


    public interface OnAccept {
        void accept(List<HashMap> list, String cityName);
    }




    private class Result {

        public List<Item> getList() {
            return list;
        }
        public City city;

        public City getCity() {
            return city;
        }

        public List<Item> list;
    }

    private  class City{
        String name;
        public String getName() {
            return name;
        }

    }

    /**
     * list
     */
    private class Item {
       public String dt;
        public String dt_txt;

        public String getDt_txt() {
            return dt_txt;
        }

        public Main main;
        public List<Weather> weather;

        public Weather getWeather() {
            return weather.get(0);
        }

        public String getDt() {
            return dt + "000";
        }

        public Main getMain() {
            return main;
        }
        public Rain getRain() {
            return rain;
        }

        Rain rain;

        public Snow getSnow() {
            return snow;
        }

        Snow snow;
    }

    private class Rain {



      //  @JsonProperty(value = "3h")
        public String getD3h() {
            return d3h;
        }


        @SerializedName(value="3h")
        public String d3h;


    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class Snow {

        public String getD3h() {
            return d3h;
        }

        @SerializedName("3h")
        public String d3h;

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
