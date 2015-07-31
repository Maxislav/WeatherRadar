package com.atlas.mars.weatherradar.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.atlas.mars.weatherradar.DataBaseHelper;
import com.atlas.mars.weatherradar.MainActivity;
import com.atlas.mars.weatherradar.R;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by mars on 7/27/15.
 */
public class MyService extends Service {
    MyAsynkTask myAsynkTask;
    static ObjectMapper mapper = new ObjectMapper();
    final String TAG = "MyServiceLog";
    HttpURLConnection urlConnection;
    NotificationManager nm;
    Notification notification;
    Intent intent;
    DataBaseHelper db;
    int alarmMinDist = 40;
    HashMap<String, String> mapSetting;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        db = new DataBaseHelper(this);
        mapSetting = db.mapSetting;

        if(mapSetting.get(DataBaseHelper.RADIUS_ALARM)!=null){
            alarmMinDist = Integer.parseInt(mapSetting.get(DataBaseHelper.RADIUS_ALARM));
        }
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        Log.d(TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        //locationManagerGps.removeUpdates(locationListenerGps);
    }

    void someTask() {
        //todo не удалять. Задача для активити
        Intent updIntent = new Intent();
        updIntent.setAction(MainActivity.LOCATION);
        updIntent.putExtra("distance", "Wake Up");
        sendBroadcast(updIntent);


        if (isNetworkAvailable()) {
            myAsynkTask = new MyAsynkTask();
            myAsynkTask.execute();
        }
    }

    void onStop() {
        this.stopSelf();
    }

    void onNotification(HashMap<String, Integer> map) {

        if (!db.permitNotify()) {
            Log.d(TAG, "Blocked notify");
            return;
        }
       // Log.d(TAG, db.getTimeNotify().toString());

        String message = "Distance: " + map.get("dist") + " Intensity: " + map.get("intensity");

        Notification notification = new Notification.Builder(this).setContentTitle("Rain alarm")
                .setContentText(message)
                .setSmallIcon(R.drawable.notification_ico)
                .build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 700, 600, 700, 700};
        //todo раскоментировать для вибрации
        vibrator.vibrate(pattern, -1);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("item_id", "1001");

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = intent;
        nm.notify(1, notification);
        timeStampDateBase();
        Log.d(TAG, "onNotification " + (new Date(System.currentTimeMillis())));

    }

    void timeStampDateBase() {
        // 1) create a java calendar instance
        Calendar calendar = Calendar.getInstance();

// 2) get a java.util.Date from the calendar instance.
//    this date will represent the current instant, or "now".
        java.util.Date now = calendar.getTime();

// 3) a java current time (now) instance
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
        String timeStamp = currentTimestamp.toString();
        Log.d(TAG, timeStamp);
        mapSetting.put(DataBaseHelper.TIME_NOTIFY, timeStamp);
        db.saveSetting();
    }


    void onCallback(HashMap<String, Integer> map) {
        if (map.get("dist") != null && map.get("dist") < alarmMinDist) {
            onNotification(map);
        }
        Log.d(TAG, "onCallback " + (new Date(System.currentTimeMillis())));
        // onNotification(map);
        onStop();
    }

    private class MyAsynkTask extends AsyncTask<String, Void, HashMap<String, Integer>> {

        @Override
        protected HashMap<String, Integer> doInBackground(String... params) {
            int _intensity = 0, _dist = 0;
            HashMap<String, Integer> map = new HashMap<>();
            URL url = null;
            InputStream in = null;
            StringBuilder sb = new StringBuilder();
            try {
                url = new URL("http://178.62.44.54/php/parserain.php");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
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
                        if (_intensity < intensity && dist < alarmMinDist) {
                            _intensity = intensity;
                            map.put("dist", dist);
                            map.put("intensity", intensity);
                        }
                    }

                } catch (IOException e) {
                    Log.e(TAG, e.toString(), e);
                    e.printStackTrace();
                }
            }


            return map;
        }

        @Override
        protected void onPostExecute(HashMap result) {
            //Log.d(TAG, result);
            onCallback(result);
        }


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
