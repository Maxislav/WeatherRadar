package com.atlas.mars.weatherradar.Rest;

import android.util.Log;

import com.atlas.mars.weatherradar.BuildConfig;
import com.atlas.mars.weatherradar.MathOperation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private interface MyApiEndpointInterface {
        @GET("/")
        void getForecastById(@Query("id") String cityId,@Query("cnt") Integer cnt, @Query("APPID") String appid, @Query("units") String units, Callback<Result> cb);

        @GET("/")
        void getForecastByLatLng(@Query("lat") String lat, @Query("lon") String lon, @Query("cnt") Integer cnt, @Query("APPID") String appid, @Query("units") String units, Callback<Result> cb);
    }

    void restByCityId() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(BuildConfig.FORECAST_URL)
                .build();
        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);
        apiService.getForecastById(cityId, cnt, BuildConfig.APPID, "metric", new Callback<Result>() {

            @Override
            public void success(Result result, Response response) {
                Log.d(TAG, "getForecastById result ok");
                Success(result);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "RetrofitError error", error);
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
                Success(result);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "RetrofitError error", error);
            }
        });
    }

    void Success(Result result) {
        list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat dayMonth = new SimpleDateFormat("dd.MM"); //2015-08-03 18:00:00
        SimpleDateFormat time = new SimpleDateFormat("HH:mm"); //2015-08-03 18:00:00
        SimpleDateFormat HH = new SimpleDateFormat("HH"); //2015-08-03 18:00:00
        SimpleDateFormat dayWeek = new SimpleDateFormat("EE");
        for (Item item : result.getList()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("icon", item.getWeather().getIcon());
            Calendar cal = new GregorianCalendar();
            String snow="", rain="";
            if(item.getRain()!=null){
                rain = item.getRain().getD3h()+"";
                Log.d(TAG,item.getRain().getD3h()+"");
            }
            if(item.getSnow()!=null){
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
                map.put("date", dayMonth.format(date));
                map.put("time", time.format(date));
                map.put("HH", HH.format(date));
                map.put("dayWeek", dayWeek.format(date));

                map.put("dayWeekNum", Integer.toString(cal.get(Calendar.DAY_OF_WEEK)));
            } catch (ParseException e) {
                Log.e(TAG, e.toString(), e);
                // e.printStackTrace();
            }

            map.put("dt_txt", item.getDt_txt());
            list.add(map);
        }

        onAccept.accept(list);
    }


    public interface OnAccept {
        void accept(List<HashMap> list);
    }

    public class Param {

    }

    private class Result {
        public List<Item> getList() {
            return list;
        }

        public List<Item> list;
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
