package com.atlas.mars.weatherradar.Rest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.atlas.mars.weatherradar.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by mars on 6/8/16.
 */
public class MeteoInfoBy {
    private static final String URL = BuildConfig.URL_METEO_INFO_BY;
    private static final String TAG = "MeteoInfoByLogs";
    private String q = "UKBB";
    private Integer t;
    private int count;
    private String nashNum;
    MyEndCallback myEndCallback;
    Bitmap bitmap;

    public MeteoInfoBy(MyEndCallback myEndCallback, Integer t) {
        this.t = t*10;
        this.count = t;
        this.myEndCallback = myEndCallback;
        onGetHash();
    }


    private interface MyApiEndpointInterface{
        @GET("/radar/")
        void getHashNum(@Query("q") String q, @Query("t") Integer t, Callback<Response> cb);

        @GET("/radar/UKBB/{img}")
        void getBitmap(@Path("img")String img, Callback<Response> cb);

    }
    public interface MyEndCallback{
        void onSuccess(Bitmap bitmap, int i);
    }


    private void onGetBitmap(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(URL)
                .build();
        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);
        apiService.getBitmap("UKBB_" + nashNum + ".png", new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                try {
                    bitmap = BitmapFactory.decodeStream(response2.getBody().in());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(bitmap!=null){
                    myEndCallback.onSuccess(bitmap,count);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG,"olol");
            }
        });




    }


    private void onGetHash(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(URL)
                .build();
        MyApiEndpointInterface apiService =
                restAdapter.create(MyApiEndpointInterface.class);

        apiService.getHashNum(q, t, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader reader = null;
                StringBuilder sb = new StringBuilder();
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(response.getBody().in()));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Result result1 = new Result(sb.toString());
                nashNum = result1.getHash();
                onGetBitmap();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, error.toString());
            }
        });
    }



    private class  Result{
        String bodyText;
        ArrayList<String> arrayList;
        public Result(String bodyText) {
            this.bodyText = bodyText;
            arrayList = new ArrayList<>();
        }

        public String getHash(){
            Pattern p = Pattern.compile("\\.\\/UKBB.+?\"" );
            Matcher m = p.matcher(bodyText);
            while (m.find()) {
                String strNum = getNum(m.group());
                arrayList.add(strNum);
            }
            if(0<arrayList.size()){
                return arrayList.get(0);
            }
            return null;
        }
        private String getNum(String s){
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(s);
            while (m.find()){
                return m.group();
            }
            return null;
        }
    }
}
