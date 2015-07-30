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

    public  static final String URL1 = "url1";
    public  static final String URL2 = "url2";
    public  static final String URL3 = "url3";

    public  static final String TITLE1 = "title1";
    public  static final String TITLE2 = "title2";
    public  static final String TITLE3 = "title3";

    public  static final String IS_VIBRATION = "isVibration"; // 0 || 1
    public  static final String IS_ALARM = "isAlarm"; // 0 || 1
    public  static final String TIME_REPEAT = "timeRepeat"; // long
    public  static final String TIME_NOTIFY = "timeNotify"; // 2015-07-29 19:12:40.878

    public  static final String TIME_FROM_HOUR = "timeFromHour";
    public  static final String TIME_FROM_MINUTE = "timeFromMinute";
    public  static final String TIME_TO_HOUR = "timeToHour";
    public  static final String TIME_TO_MINUTE = "timeToMinute";
    final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";






    private static final String SQL_CREATE_TABLE_SETTING =  "CREATE TABLE if not exists "
            +TABLE_SETTING+" (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            +KEY +" VARCHAR(255), " +  VALUE +  " VARCHAR(255) " +");";





    public  static  HashMap<String, String> mapSetting;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if(  mapSetting == null){
            mapSetting = new HashMap<>();
            getSetting();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_SETTING);
        ContentValues cv = new ContentValues();
        for(int i = 0; i<3; i++){
            cv.put(KEY, "url"+(i+1) );
            cv.put(VALUE,  "url"+(i+1) );
            db.insert(TABLE_SETTING, null, cv);
        }
        for(int i = 0; i<3; i++){
            cv.put(KEY, "title"+(i+1) );
            cv.put(VALUE,  "title"+(i+1) );
            db.insert(TABLE_SETTING, null, cv);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public void saveSetting(){
        sdb = getWritableDatabase();
        String query =  "SELECT * FROM " + TABLE_SETTING;
        Cursor cursor = sdb.rawQuery(query,null);
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

            if(oldSetting.get(key) == null){
                //todo надо записать новое значение
                cv.put(KEY, key );
                cv.put(VALUE, value );
                sdb.insert(TABLE_SETTING, null, cv);

            }else if(!oldSetting.get(key).equals(value)){
                //todo новое значение
                query = "UPDATE " + TABLE_SETTING + " SET " + VALUE + "='"+ value+"'" + " WHERE " + KEY +"='"+key+"'";
                sdb.execSQL(query);
            }
        }
        cursor.close();
        sdb.close();
    }

    private void getSetting(){
        sdb = getWritableDatabase();
        String query =  "SELECT * FROM " + TABLE_SETTING;
        Cursor cursor = sdb.rawQuery(query,null);
        while (cursor.moveToNext()) {
            String key = cursor.getString(cursor.getColumnIndex(KEY));
            String value = cursor.getString(cursor.getColumnIndex(VALUE));
            mapSetting.put(key, value);
        }
        cursor.close();
        sdb.close();
    }
    Calendar fromNotSleep, toNotSleep;
    public  Long getStartTime(){

        Calendar c = new GregorianCalendar();
        fromNotSleep = new GregorianCalendar(c.get(Calendar.YEAR),c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        toNotSleep = new GregorianCalendar(c.get(Calendar.YEAR),c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));


        fromNotSleep.add(Calendar.HOUR_OF_DAY, Integer.parseInt(mapSetting.get(TIME_FROM_HOUR)!=null? mapSetting.get(TIME_FROM_HOUR):"0"));
        fromNotSleep.add(Calendar.MINUTE, Integer.parseInt(mapSetting.get(TIME_FROM_MINUTE)!=null? mapSetting.get(TIME_FROM_MINUTE):"0"));

        toNotSleep.add(Calendar.HOUR_OF_DAY, Integer.parseInt(mapSetting.get(TIME_TO_HOUR)!=null? mapSetting.get(TIME_TO_HOUR):"0"));
        toNotSleep.add(Calendar.MINUTE, Integer.parseInt(mapSetting.get(TIME_TO_MINUTE)!=null? mapSetting.get(TIME_TO_MINUTE):"0"));

        Log.d(TAG, "" +fromNotSleep.getTimeInMillis());


        //Date fromNotSleep = new Date(d.getYear() )


        int timeRepeat = mapSetting.get(TIME_REPEAT)!=null ? Integer.parseInt(mapSetting.get(TIME_REPEAT)):2;
        long timeRepeatLong = 3600*1000*timeRepeat;
        String timeNotify = mapSetting.get(TIME_NOTIFY)!=null ? mapSetting.get(TIME_NOTIFY) : null;

        DateFormat formatter = new SimpleDateFormat(NEW_FORMAT);
        Date dateNotify = null;
        try {
            if(timeNotify!=null){
                dateNotify = formatter.parse(timeNotify);
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString(), e);
            e.printStackTrace();
        }
        long dif = 0;
        if(dateNotify!=null){
            dif = System.currentTimeMillis() - dateNotify.getTime();
        }

        long startAlarm;
        if(dif == 0){
            startAlarm = System.currentTimeMillis()+10*60*1000;
        }else if(dif<timeRepeatLong){
            startAlarm = System.currentTimeMillis()+(timeRepeatLong-dif);
        }else{
            startAlarm = System.currentTimeMillis()+10*60*1000;
        }

        if(startAlarm<fromNotSleep.getTimeInMillis()){
            startAlarm = fromNotSleep.getTimeInMillis();
        }

        if(toNotSleep.getTimeInMillis()<startAlarm){
            fromNotSleep.add(Calendar.DAY_OF_YEAR, 1);
            startAlarm = fromNotSleep.getTimeInMillis()+60*1000;
        }

        return startAlarm;
    }
}
