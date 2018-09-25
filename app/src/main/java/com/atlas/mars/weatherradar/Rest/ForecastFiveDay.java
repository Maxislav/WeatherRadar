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
    HashMap<String, Boolean> map;
    Integer cnt;
    MyService myService;

    public ForecastFiveDay(OnAccept onAccept, String cityId, Integer cnt) {
        this.onAccept = onAccept;
        this.cityId = cityId;
        this.cnt = cnt;
        list = new ArrayList<>();
       // map = new HashMap<>();
        restByCityId();
    }

    public ForecastFiveDay(OnAccept onAccept, double lat, double lng) {
        this.onAccept = onAccept;
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
        map = new HashMap<>();
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
        list = new ArrayList<>();
        map = new HashMap<>();
        restByLatLng();
    }

    private interface Constant{
        String URL = BuildConfig.URL_API_OPENWEATHERMAP;
    }
    private interface MyApiEndpointInterface {
        @GET("/forecast")
        void getForecastById(@Query("id") String cityId,@Query("cnt") Integer cnt, @Query("APPID") String appid, @Query("units") String units, Callback<Result> cb);

        @GET("/forecast")
        void getForecastByLatLng(@Query("lat") String lat, @Query("lon") String lon, @Query("APPID") String appid, @Query("units") String units, Callback<Result> cb);
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
                }else {
                    resultError(error);
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
        apiService.getForecastByLatLng(lat, lng, BuildConfig.APPID, "metric", new Callback<Result>() {

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
                }else{
                    Success(null);
                }
                Log.e(TAG, "RetrofitError error", error);
            }
        });
    }
    void onResultForService(Result result){
        map.put("rain", true);
        if(result == null){
            myService.onForecastFiveDayResult(map);
            return;
        }else{
            int code = result.getCod();
            if(code==200){
                map.put("rain", false);
                for (Item item : result.getList()) {
                    if(item.getRain()!=null && item.getRain().getD3h()!=null && !item.getRain().getD3h().isEmpty()){
                        map.put("rain", true);
                    }
                    if(item.getSnow()!=null && item.getSnow().getD3h()!=null && !item.getSnow().getD3h().isEmpty()){
                        map.put("rain", true);
                    }
                }
            }else{
                map.put("rain", true);
            }
        }
        myService.onForecastFiveDayResult(map);
    }

    void resultError(RetrofitError error){
        Log.d(TAG, "resultError", error);
        onAccept.accept(list, null, 500);
    }

    void Success(Result result) {
        list = new ArrayList<>();
        if(result == null){
            onAccept.accept(list, null, 500);
            return;
        }
        int code = result.getCod();

        if(code!=200){
            onAccept.accept(list, null, code);
            return;
        }

        String cityName = result.getCity().getName();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat _date = new SimpleDateFormat("dd.MM"); //18.01
        SimpleDateFormat dayOfMonth = new SimpleDateFormat("dd"); //18
        SimpleDateFormat time = new SimpleDateFormat("HH:mm"); //2015-08-03 18:00:00
        SimpleDateFormat HH = new SimpleDateFormat("HH"); //2015-08-03 18:00:00
        SimpleDateFormat dayWeek = new SimpleDateFormat("EE");
        SimpleDateFormat dayWeekFull = new SimpleDateFormat("EEEE");
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
            map.put("temp", (0<dTemp ? "+": "")+ (int)dTemp);

            try {
                Date date = format.parse(item.getDt_txt());
                cal.setTime(date);
                map.put("date", _date.format(date));
                map.put("dayOfMonth", dayOfMonth.format(date));
                map.put("time", time.format(date));
                map.put("HH", HH.format(date));
                map.put("dayWeek", dayWeek.format(date));
                map.put("dayWeekFull", dayWeekFull.format(date));
                map.put("dayWeekNum", Integer.toString(cal.get(Calendar.DAY_OF_WEEK)));
            } catch (ParseException e) {
                Log.e(TAG, e.toString(), e);
            }

            map.put("dt_txt", item.getDt_txt());
            map.put("humidity", item.getMain().getHumidity());
            map.put("temp_min", item.getMain().getTemp_min());
            map.put("temp_max", item.getMain().getTemp_max());
            map.put("pressure", item.getMain().getPressure());
            map.put("clouds", item.getClouds().getAll());

            Wind wind = item.getWind();
            if(wind!=null){
                map.put("wind_speed", wind.getSpeed());
                map.put("wind_deg", wind.getDeg());
            }

            list.add(map);
        }
        onAccept.accept(list, cityName, code);
    }


    public interface OnAccept {
        void accept(List<HashMap> list, String cityName, int code);
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

        public int getCod() {
            return cod;
        }
        int cod;

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
        public Wind wind;

        public Wind getWind() {
            return wind;
        }

        public String getDt_txt() {
            return dt_txt;
        }

        public Main main;
        public List<Weather> weather;
        public Clouds clouds;


        public Weather getWeather() {
            return weather.get(0);
        }

        public Clouds getClouds(){
            return clouds;
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

    private class Wind{
        String speed;
        double deg;
        String strDeg;


        public String getSpeed() {
            return speed;
        }

        public String getDeg() {
           // Log.d(TAG, "Degree :  "+ deg);

            Double d = MathOperation.round(deg+180, 0);
            strDeg = Integer.toString(d.intValue());

            return  strDeg;
        }
    }

    private class Clouds{
        String all;
        public String getAll() {
            return all;
        }

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
        String pressure;

        double temp_min, temp_max;

        public String getPressure() {
            return pressure;
        }



        public String getTemp_min(){
            if(temp_min<0){
                return "-"+temp_min;
            }else {
                return "+"+temp_min;
            }
        }

        public String getTemp_max(){
            if(temp_max<0){
                return "-"+temp_max;
            }else {
                return "+"+temp_max;
            }
        }

        public String getHumidity() {
            return humidity+"%";
        }

        String humidity;
    }

    private class Weather {
        String icon;
        public String getIcon() {
            return icon.replaceAll("^\\d+", "01");
        }
    }
}
