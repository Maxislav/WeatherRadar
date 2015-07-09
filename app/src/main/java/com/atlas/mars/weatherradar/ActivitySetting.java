package com.atlas.mars.weatherradar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Администратор on 7/8/15.
 */
public class ActivitySetting extends AppCompatActivity {

    DataBaseHelper db;
    HashMap <String, String> mapSetting;
    MyJQuery jQuery;
    ArrayList<View> arrayEditText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        db = new DataBaseHelper(this);
        mapSetting = db.mapSetting;
        jQuery = new MyJQuery();
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.globalLayout);
        arrayEditText = jQuery.findViewByTagClass(linearLayout, EditText.class);
        inflateSetting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_save_and_close:
                saveAndClose();
                return true;
            case R.id.action_close:
                finish();
        }
        return  false;
    }

    private void inflateSetting(){

        if(mapSetting.get("title1")!=null){
            ((EditText)arrayEditText.get(0)).setText(mapSetting.get("title1"));
        }

        if(mapSetting.get("title2")!=null){
            ((EditText)arrayEditText.get(2)).setText(mapSetting.get("title2"));
        }

        if(mapSetting.get("title3")!=null){
            ((EditText)arrayEditText.get(4)).setText(mapSetting.get("title3"));
        }


        if(mapSetting.get("url1")!=null){
            ((EditText)arrayEditText.get(1)).setText(mapSetting.get("url1"));
        }
        if(mapSetting.get("url1")!=null){
            ((EditText)arrayEditText.get(3)).setText(mapSetting.get("url2"));
        }
        if(mapSetting.get("url3")!=null){
            ((EditText)arrayEditText.get(5)).setText(mapSetting.get("url3"));
        }




    }


    private  void  saveAndClose(){
        saveSetting();
        Intent answerIntent = new Intent();
        setResult(RESULT_OK, answerIntent);
        finish();
    }

    private void saveSetting(){
        String title1 = ((EditText)arrayEditText.get(0)).getText().toString();
        String title2 = ((EditText)arrayEditText.get(2)).getText().toString();
        String title3 = ((EditText)arrayEditText.get(4)).getText().toString();

        String url1 = ((EditText)arrayEditText.get(1)).getText().toString();
        String url2 = ((EditText)arrayEditText.get(3)).getText().toString();
        String url3 = ((EditText)arrayEditText.get(5)).getText().toString();
        mapSetting.put("title1", title1);
        mapSetting.put("title2", title2);
        mapSetting.put("title3", title3);


        mapSetting.put("url1", url1);
        mapSetting.put("url2", url2);
        mapSetting.put("url3", url3);
        db.saveSetting();


    }

}
