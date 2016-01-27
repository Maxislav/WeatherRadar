package com.atlas.mars.weatherradar.alarm;

import android.content.Context;
import android.content.res.AssetManager;

import com.atlas.mars.weatherradar.location.OnLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mars on 1/26/16.
 */
public class LocationFromAsset{
    final String TAG = "LocationFromAssetLog";
    OnLocation onLocation;
    int iPosition;
    static ObjectMapper mapper = new ObjectMapper();

    public LocationFromAsset (Context context, String sPosition){
        this.onLocation = (OnLocation)context;
        iPosition = Integer.parseInt(sPosition);

        String jsonLocation = null;
        try {
            jsonLocation = AssetJSONFile("cities.json", context);
          //  jsonLocation = jsonLocation.replaceAll("\\n", "");
        } catch (IOException e) {
            e.printStackTrace();
        }


        ObjectNode root = null;
        try {

            JSONArray obj = new JSONArray(jsonLocation);
            JSONObject item = obj.getJSONObject(iPosition);


            /*root = (ObjectNode) mapper.readTree(jsonLocation);
            JsonNode jsonNode = (JsonNode)root.get(iPosition);
            String _id = jsonNode.path("id").asText();
            Log.d(TAG, _id+"" );*/


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }





    void onCallback(){
        onLocation.onLocationAccept(1, 2);
    }


    public static String AssetJSONFile (String filename, Context context) throws IOException {
        AssetManager manager = context.getAssets();
        InputStream file = manager.open(filename);
        byte[] formArray = new byte[file.available()];
        file.read(formArray);
        file.close();

        return new String(formArray);
    }


   /* @Override
    public void onLocationAccept(double lat, double lng) {
        onLocation.onLocationAccept(lat, lng);
    }*/
}
