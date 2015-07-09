package com.atlas.mars.weatherradar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mars on 7/9/15.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    SQLiteDatabase sdb;
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "weather";
    private static final String UID = "_id";
    private static final String TABLE_SETTING = "tableSetting";
    private static final String KEY = "key";
    private static final String VALUE = "value";




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
}
