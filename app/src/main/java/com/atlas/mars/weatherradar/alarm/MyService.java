package com.atlas.mars.weatherradar.alarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.atlas.mars.weatherradar.DataBaseHelper;
import com.atlas.mars.weatherradar.MainActivity;
import com.atlas.mars.weatherradar.MathOperation;
import com.atlas.mars.weatherradar.R;
import com.atlas.mars.weatherradar.location.MyLocationListenerNet;
import com.atlas.mars.weatherradar.location.OnLocation;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by mars on 7/27/15.
 */
public class MyService extends Service implements OnLocation {
    BorispolTask borispolTask;
    GoogleWeatherTask googleWeatherTask;
    static ObjectMapper mapper = new ObjectMapper();
    final String TAG = "MyServiceLogs";
    final String ALARM = "AlarmLogs";
    HttpURLConnection urlConnection;
    NotificationManager nm;
    Notification notification;
    Intent intent;
    DataBaseHelper db;
    static int alarmMinDist = 40;
    HashMap<String, String> mapSetting;
    AssetManager assets;
    SoundPool sp;
    final int MAX_STREAMS = 5;
    public LocationManager locationManagerNet;
    public LocationListener locationListenerNet;
    int taskEnd = 0;
    int taskNeeded = 0;
    boolean rainGoogle = false;
    boolean rainBorispol = false;

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

        if (mapSetting.get(DataBaseHelper.RADIUS_ALARM) != null) {
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

    @Override
    public void onLocationAccept(double lat, double lng) {
        if (locationManagerNet != null) {
            locationManagerNet.removeUpdates(locationListenerNet);
        }
        if (isNetworkAvailable()) {
            if(db.getStartBorispol()){
                taskNeeded++;
                borispolTask = new BorispolTask(this);
                try {
                    borispolTask.execute(lat, lng);
                }catch (Exception e){
                    rainBorispol = true;
                    Log.e(TAG, e.toString(), e);
                }
            }

            if(db.getStartForecast()){
                taskNeeded++;
                googleWeatherTask = new GoogleWeatherTask(this);
                googleWeatherTask.execute(MathOperation.round(lat, 2), MathOperation.round(lng,2));
            }
        }

        if(taskNeeded == 0){
          //  alarmRestart();
            onStop();
        }
    }

    void someTask() {

        //todo тестовый вызов нотификатион
/*        HashMap <String, Integer> map = new HashMap();
        map.put("dist", 10);
        onNotification(map);*/

        //todo не удалять. Задача для активити
        Intent updIntent = new Intent();
        updIntent.setAction(MainActivity.LOCATION);
        updIntent.putExtra("distance", "Wake Up");
        sendBroadcast(updIntent);

        if (isNetworkAvailable() && db.isWorkTime()) {
            locationManagerNet = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListenerNet = new MyLocationListenerNet(this);
            locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
        } else {
            onStop();
        }


    }

    void onStop() {
        if(mapSetting.get(DataBaseHelper.IS_ALARM)!=null && mapSetting.get(DataBaseHelper.IS_ALARM).equals("1")){
            alarmRestart();
        }
        this.stopSelf();
    }

    void onNotification(HashMap<String, Integer> map) {

        if (!db.permitNotify()) {
            Log.d(TAG, "Blocked notify");
            return;
        }
        // Log.d(TAG, db.getTimeNotify().toString());
        playSound();
        String message = "Distance: " + map.get("dist") + "Km" + " " + getIntensity(map.get("intensity"));

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("dist", map.get("dist"));
        notificationIntent.putExtra("were_from", "my_service");

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this).setContentTitle("Rain alarm")
                .setContentText(message)
                .setSmallIcon(R.drawable.notification_ico)
                .setContentIntent(intent)
                .build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 700, 600, 700, 700};
        //todo раскоментировать для вибрации
        vibrator.vibrate(pattern, -1);

        nm.notify(1, notification);
        timeStampDateBase();
        Log.d(TAG, "onNotification " + (new Date(System.currentTimeMillis())));

    }

    void timeStampDateBase() {
        mapSetting.put(DataBaseHelper.TIME_NOTIFY, getTimeStamp());
        db.saveSetting();
    }


    void onBorispolTaskResult(HashMap<String, Integer> map) {

        if (map.get("dist") != null && map.get("dist") < alarmMinDist) {
            onNotification(map);
        }

        if(map.get("isIntensity")!=null && 0 <map.get("isIntensity")){
            rainBorispol = true;
        }

        if(map.get("error")!=null && map.get("error")==1){
            rainBorispol = true;
        }
        mapSetting.put(DataBaseHelper.BORISPOL_TIME, getTimeStamp());
        db.saveSetting();

        Log.d(TAG, "onBorispolTaskResult " + (new Date(System.currentTimeMillis())));

        allTaskResult();
    }

    void onGoogleWeatherTaskResult(HashMap<String, Boolean> map) {
        rainGoogle = map.get("rain");
        mapSetting.put(DataBaseHelper.FORECAST_TIME, getTimeStamp());
        db.saveSetting();
        allTaskResult();
    }

    void allTaskResult() {
        taskEnd++;
        if (taskNeeded <= taskEnd) {
            if(rainBorispol || rainGoogle){
                mapSetting.put(DataBaseHelper.FORECAST_RAIN, "1");
            }else {
                mapSetting.put(DataBaseHelper.FORECAST_RAIN, "0");
            }
            db.saveSetting();
           // alarmRestart();
            onStop();
        }
    }

    private void alarmRestart(){

        // am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10*60*1000, pendingIntent);
       long startTime = 0;
        try {
           startTime = db.getStartTime();
        }catch (Exception e){
           Log.e(TAG, e.toString(), e);
           Log.e(ALARM, e.toString(), e);
        }

        if(startTime!=0){
            Intent intent1 = createIntent("action 1", "extra 1");
            PendingIntent pendingIntent;
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, db.getStartTime(), pendingIntent);
            //todo убрать
            // am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+30*1000, pendingIntent);
            Log.d(ALARM, "Alarm Restart: " + new Date(db.getStartTime()).toString());
        }
        //am.cancel(pendingIntent);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    void playSound() {
        sp = buildSoundPool();
        assets = getAssets();
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                sp.play(sampleId, 1, 1, 0, 0, 1);
            }
        });
        loadSound("bul_bul.ogg");
    }

    private SoundPool buildSoundPool() {
        SoundPool soundPool;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(25)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(25, AudioManager.STREAM_MUSIC, 0);
        }
        return soundPool;
    }

    private int loadSound(String fileName) {
        AssetFileDescriptor afd = null;
        try {
            afd = assets.openFd(fileName);
        } catch (IOException e) {
            Log.d(TAG, e.toString(), e);
            return -1;
        }
        return sp.load(afd, 1);
    }

    private String getIntensity(Integer i) {
        String intensity = "Unknown";
        if (i == null) {
            return intensity;
        }


        switch (i) {
            case 1:
                return "Слостая облачность";
            case 2:
                return "Осадки слабые";
            case 3:
                return "Осадки умеренные";
            case 4:
                return "Осадки сильные";
            case 5:
                return "Конвективная облачность";
            case 6:
                return "Конвективные осадки слабые";
            case 7:
                return "Конвективные осадки умеренные";
            case 8:
                return "Конвективные осадки сильные";
            case 9:
                return "Гроза вероятность 30%-70%";
            case 10:
                return "Гроза вероятность 70%-90%";
            case 11:
                return "Гроза вероятность 90%-00%";
            default:
                return intensity;
        }

    }

    private String getTimeStamp(){
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
        return currentTimestamp.toString();
    }

    Intent createIntent(String action, String extra) {
       /* SampleBootReceiver sm = new SampleBootReceiver();
        sm.setMainActivity(this);*/


        Intent intent = new Intent(this, SampleBootReceiver.class);
        intent.setAction(action);
        intent.putExtra("extra", extra);
        return intent;
    }


}
