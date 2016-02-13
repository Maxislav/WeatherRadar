package com.atlas.mars.weatherradar;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Администратор on 1/31/16.
 */
public class Cities {

    String cityName, cityId;
    Context context;
    JSONArray jsonArray;


    public Cities(Context context) {
        this.context = context;
        setJsonArray();
    }

    public void setJsonArray() {
        String stringJson = null;
        try {
            stringJson =  AssetJSONFile("cities.json", context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            jsonArray = new JSONArray(stringJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCityName(String position) {
        try {
            cityName = jsonArray.getJSONObject(Integer.parseInt(position)).getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    public String getCityId(String position) {
        try {
            cityId = jsonArray.getJSONObject(Integer.parseInt(position)).getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cityId;
    }

    public String[] getCities(){
        String[] planets = new String[jsonArray.length()];
        for (int i = 0 ; i<jsonArray.length();i++){
            try {
                String name = getStringResourceByName(jsonArray.getJSONObject(i).getString("name"), context);
                planets[i]= name;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return planets;
    }

    private static String AssetJSONFile (String filename, Context context) throws IOException {
        AssetManager manager = context.getAssets();
        InputStream file = manager.open(filename);
        byte[] formArray = new byte[file.available()];
        file.read(formArray);
        file.close();
        return new String(formArray);
    }
    public static String getStringResourceByName(String aString, Context context) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(aString, "string", packageName);
        return context.getString(resId);
    }
}
