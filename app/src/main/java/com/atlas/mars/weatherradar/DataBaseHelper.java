package com.atlas.mars.weatherradar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mars on 7/9/15.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    final String TAG = "DataBaseHelperLogs";
    SQLiteDatabase sdb;
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "weather";
    private static final String UID = "_id";
    private static final String TABLE_SETTING = "tableSetting";
    private static final String KEY = "key";
    private static final String VALUE = "value";

    public static final String URL1 = "url1";
    public static final String URL2 = "url2";
    public static final String URL3 = "url3";

    public static final String TITLE1 = "title1";
    public static final String TITLE2 = "title2";
    public static final String TITLE3 = "title3";

    public static final String IS_VIBRATION = "isVibration"; // 0 || 1
    public static final String RADIUS_ALARM = "radiusAlarm"; // 0 || 1
    public static final String IS_ALARM = "isAlarm"; // 0 || 1
    public static final String TIME_REPEAT = "timeRepeat"; //   время возобновления работы после уведомления по умолчанию 2
    public static final String TIME_NOTIFY = "timeNotify"; // 2015-07-29 19:12:40.878  //время срабатывания уведомления

    public static final String TIME_FROM_HOUR = "timeFromHour";
    public static final String TIME_FROM_MINUTE = "timeFromMinute";
    public static final String TIME_TO_HOUR = "timeToHour";
    public static final String TIME_TO_MINUTE = "timeToMinute";
    public static final String FORECAST_RAIN = "forecastRain"; // 0 || 1
    public static final String FORECAST_TIME = "forecastTime"; // Время запроса прогноза
    public static final String BORISPOL_TIME = "borispolTime"; // Время запроса борисполя
    public static final String MORNING_ALARM = "morningAlarm"; // 0 || 1

    public static final String LICENCE = "licence"; // 0 || 1
    public static final String TIMESTAMP_CURRENT_WEATHER = "tsCurrentWeather";  // 2015-07-29 19:12:40.878  //время прогноза
    public static final String CURRENT_WEATHER_ICON = "currentWeatherIcon";  //
    public static final String CURRENT_WEATHER_HUMIDITY = "currentWeatherHumidity";  //
    public static final String CURRENT_WEATHER_TEMP = "currentWeatherTemp";  //
    public static final String CURRENT_WEATHER_WIND = "currentWeatherWind";  //
    public static final String CURRENT_WEATHER_CITY = "currentWeatherCity";  //
    public static final String TIMESTAMP_FORECAST = "timestampForecast";  //
    public static final String MY_LOCATION = "myLocation";  // int  0, 1, 2 ...


    Calendar fromNotSleep, toNotSleep;

    final static String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    final DateFormat formatter = new SimpleDateFormat(NEW_FORMAT);

    static long intervalTime = 10 * 60 * 1000;
    //static long intervalTime = 20*1000;


    private static final String SQL_CREATE_TABLE_SETTING = "CREATE TABLE if not exists "
            + TABLE_SETTING + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY + " VARCHAR(255), " + VALUE + " VARCHAR(255) " + ");";


    public static HashMap<String, String> mapSetting;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (mapSetting == null) {
            mapSetting = new HashMap<>();
            getSetting();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_SETTING);
        ContentValues cv = new ContentValues();
        for (int i = 0; i < 3; i++) {
            cv.put(KEY, "url" + (i + 1));
            cv.put(VALUE, "url" + (i + 1));
            db.insert(TABLE_SETTING, null, cv);
        }
        for (int i = 0; i < 3; i++) {
            cv.put(KEY, "title" + (i + 1));
            cv.put(VALUE, "title" + (i + 1));
            db.insert(TABLE_SETTING, null, cv);
        }
        cv.put(KEY, LICENCE);
        cv.put(VALUE, "0");
        db.insert(TABLE_SETTING, null, cv);

        cv.put(KEY, MY_LOCATION);
        cv.put(VALUE, "1");
        db.insert(TABLE_SETTING, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    synchronized public void saveSetting() {
        sdb = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_SETTING;
        Cursor cursor = sdb.rawQuery(query, null);
        HashMap<String, String> oldSetting = new HashMap<>();
        while (cursor.moveToNext()) {
            String key = cursor.getString(cursor.getColumnIndex(KEY));
            String value = cursor.getString(cursor.getColumnIndex(VALUE));
            oldSetting.put(key, value);
        }

        ContentValues cv = new ContentValues();
        for (Map.Entry entry : mapSetting.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            if (oldSetting.get(key) == null) {
                //todo надо записать новое значение
                cv.put(KEY, key);
                cv.put(VALUE, value);
                sdb.insert(TABLE_SETTING, null, cv);

            } else if (!oldSetting.get(key).equals(value)) {
                //todo новое значение
                query = "UPDATE " + TABLE_SETTING + " SET " + VALUE + "='" + value + "'" + " WHERE " + KEY + "='" + key + "'";
                sdb.execSQL(query);
            }
        }
        cursor.close();
        sdb.close();
    }

    private void getSetting() {
        sdb = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_SETTING;
        Cursor cursor = sdb.rawQuery(query, null);
        while (cursor.moveToNext()) {
            String key = cursor.getString(cursor.getColumnIndex(KEY));
            String value = cursor.getString(cursor.getColumnIndex(VALUE));
            mapSetting.put(key, value);
        }
        cursor.close();
        sdb.close();
    }


    public boolean isWorkTime() {
        long curMills = System.currentTimeMillis();
        Calendar c = new GregorianCalendar();
        fromNotSleep = new GregorianCalendar(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        toNotSleep = new GregorianCalendar(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        fromNotSleep.add(Calendar.HOUR_OF_DAY, Integer.parseInt(mapSetting.get(TIME_FROM_HOUR) != null ? mapSetting.get(TIME_FROM_HOUR) : "0"));
        fromNotSleep.add(Calendar.MINUTE, Integer.parseInt(mapSetting.get(TIME_FROM_MINUTE) != null ? mapSetting.get(TIME_FROM_MINUTE) : "0"));

        toNotSleep.add(Calendar.HOUR_OF_DAY, Integer.parseInt(mapSetting.get(TIME_TO_HOUR) != null ? mapSetting.get(TIME_TO_HOUR) : "0"));
        toNotSleep.add(Calendar.MINUTE, Integer.parseInt(mapSetting.get(TIME_TO_MINUTE) != null ? mapSetting.get(TIME_TO_MINUTE) : "0"));

        if (fromNotSleep.getTimeInMillis() < curMills && curMills < toNotSleep.getTimeInMillis()) {
            return true;
        } else {
            return false;
        }
    }


    public Long getStartTime() {
        Calendar c = new GregorianCalendar();
        fromNotSleep = new GregorianCalendar(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        toNotSleep = new GregorianCalendar(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));


        fromNotSleep.add(Calendar.HOUR_OF_DAY, Integer.parseInt(mapSetting.get(TIME_FROM_HOUR) != null ? mapSetting.get(TIME_FROM_HOUR) : "0"));
        fromNotSleep.add(Calendar.MINUTE, Integer.parseInt(mapSetting.get(TIME_FROM_MINUTE) != null ? mapSetting.get(TIME_FROM_MINUTE) : "0"));

        toNotSleep.add(Calendar.HOUR_OF_DAY, Integer.parseInt(mapSetting.get(TIME_TO_HOUR) != null ? mapSetting.get(TIME_TO_HOUR) : "0"));
        toNotSleep.add(Calendar.MINUTE, Integer.parseInt(mapSetting.get(TIME_TO_MINUTE) != null ? mapSetting.get(TIME_TO_MINUTE) : "0"));

        int timeRepeat = mapSetting.get(TIME_REPEAT) != null ? Integer.parseInt(mapSetting.get(TIME_REPEAT)) : 2;
        long timeRepeatLong = 3600 * 1000 * timeRepeat;
        String timeNotify = mapSetting.get(TIME_NOTIFY) != null ? mapSetting.get(TIME_NOTIFY) : null;


        Date dateNotify = null;
        try {
            if (timeNotify != null) {
                dateNotify = formatter.parse(timeNotify);
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString(), e);
            e.printStackTrace();
        }
        long dif = 0;
        if (dateNotify != null) {
            dif = System.currentTimeMillis() - dateNotify.getTime();
        }

        long startAlarm;
        if (dif == 0) {
            startAlarm = System.currentTimeMillis() + intervalTime;
        } else if (dif < timeRepeatLong) {
            startAlarm = System.currentTimeMillis() + (timeRepeatLong - dif);
        } else {
            startAlarm = System.currentTimeMillis() + intervalTime;
        }

        if (getForecastTime() != null && !getStartForecast()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getForecastTime());
            if (mapSetting.get(FORECAST_RAIN) != null && mapSetting.get(FORECAST_RAIN).equals("0")) {
                cal.add(Calendar.HOUR_OF_DAY, 3); //не реже чем черз 3 часа
                startAlarm = cal.getTimeInMillis();
            }
        }

        if (startAlarm < fromNotSleep.getTimeInMillis()) {
            startAlarm = fromNotSleep.getTimeInMillis();
        }

        if (toNotSleep.getTimeInMillis() < startAlarm) {
            fromNotSleep.add(Calendar.DAY_OF_YEAR, 1);
            startAlarm = fromNotSleep.getTimeInMillis() + 60 * 1000;
        }


        Log.d(TAG, "FORECAST_RAIN:  " + mapSetting.get(FORECAST_RAIN) + " : getStartForecast " + getStartForecast());
        return startAlarm;
    }

    public Long getTimeNotify() {
        DateFormat formatter = new SimpleDateFormat(NEW_FORMAT);
        String timeNotify = mapSetting.get(TIME_NOTIFY) != null ? mapSetting.get(TIME_NOTIFY) : null;
        Date dateNotify = null;
        try {
            if (timeNotify != null) {
                dateNotify = formatter.parse(timeNotify);
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString(), e);
            e.printStackTrace();
        }
        if (dateNotify != null) {
            return dateNotify.getTime();
        } else {
            return null;
        }
    }

    public boolean getStartForecast() {
        String time = mapSetting.get(FORECAST_TIME);
        if (time == null) {
            return true;
        }
        Date date = null;
        try {
            date = formatter.parse(time);
        } catch (ParseException e) {
            Log.e(TAG, e.toString(), e);
            e.printStackTrace();
        }
        if (date != null && date.getTime() + (3 * 3600 * 1000) <= System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public boolean getStartBorispol() {
        boolean start = false;
        int timeRepeat = mapSetting.get(TIME_REPEAT) != null ? Integer.parseInt(mapSetting.get(TIME_REPEAT)) : 2;//время возобновления работы после уведомление
        long timeRepeatLong = 3600 * 1000 * timeRepeat;

        String timeNotify = mapSetting.get(TIME_NOTIFY);
        long timeNotifyLong = 0;

        if (timeNotify != null) {
            try {
                timeNotifyLong = formatter.parse(timeNotify).getTime();
            } catch (ParseException e) {
                Log.e(TAG, e.toString(), e);
                e.printStackTrace();
            }
            if (System.currentTimeMillis() < timeNotifyLong + timeRepeatLong) {
                return false;
            }
        }


        String borispolTime = mapSetting.get(BORISPOL_TIME);
        long borispolTimeLong;
        if (borispolTime == null) {
            return true;
        } else {
            try {
                borispolTimeLong = formatter.parse(borispolTime).getTime();
            } catch (ParseException e) {
                Log.e(TAG, e.toString(), e);
                e.printStackTrace();
                return true;
            }
            if (borispolTimeLong + 10 * 60 * 1000 <= System.currentTimeMillis()) { //ессли прошло более 10ми с прошлого запроса к борисполю
                return true;
            } else {
                return false;
            }
        }
    }

    Date getForecastTime() {
        String time = mapSetting.get(FORECAST_TIME);
        if (time == null) {
            return null;
        }
        Date date = null;
        try {
            date = formatter.parse(time);
        } catch (ParseException e) {
            Log.e(TAG, e.toString(), e);
            e.printStackTrace();
        }
        return date;
    }

    public boolean permitNotify() {

        int timeRepeat = mapSetting.get(TIME_REPEAT) != null ? Integer.parseInt(mapSetting.get(TIME_REPEAT)) : 2;
        long timeRepeatLong = 3600 * 1000 * timeRepeat;
        if (getTimeNotify() == null) {
            return true;
        }
        return (getTimeNotify() + timeRepeatLong) < (System.currentTimeMillis());
    }

    public void deleteValue(String key) {
        sdb = getWritableDatabase();
        String query = "DELETE FROM " + TABLE_SETTING + " WHERE " + KEY + "='" + key + "'";
        try {
            sdb.execSQL(query);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            sdb.close();
            // return false;
        }
        // cursor.close();
        sdb.close();
    }

    public long getMorningWakeUp() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());


        mapSetting.get(TIME_FROM_HOUR);
        mapSetting.get(TIME_FROM_MINUTE);
        Calendar calendarWakeUp = Calendar.getInstance();
        calendarWakeUp.set(Calendar.YEAR, c.get(Calendar.YEAR));
        calendarWakeUp.set(Calendar.MONTH, c.get(Calendar.MONTH));
        calendarWakeUp.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
        calendarWakeUp.set(Calendar.HOUR_OF_DAY, Integer.parseInt(mapSetting.get(TIME_FROM_HOUR)));
        calendarWakeUp.set(Calendar.MINUTE, Integer.parseInt(mapSetting.get(TIME_FROM_MINUTE)));
        calendarWakeUp.set(Calendar.SECOND, 0);
        calendarWakeUp.set(Calendar.MILLISECOND, 0);

        if (calendarWakeUp.getTimeInMillis() <= c.getTimeInMillis()) {
            calendarWakeUp.add(c.DAY_OF_MONTH, 1);
        }
        return calendarWakeUp.getTimeInMillis();
    }

    public String getTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
        return currentTimestamp.toString();
    }


    public Date stringToDate(String timeText) {
        DateFormat formatter = new SimpleDateFormat(NEW_FORMAT);
        //String timeNotify = mapSetting.get(TIME_NOTIFY) != null ? mapSetting.get(TIME_NOTIFY) : null;
        Date dateNotify = null;
        try {
            dateNotify = formatter.parse(timeText);
            ;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return dateNotify;
    }
}
