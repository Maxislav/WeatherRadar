package com.atlas.mars.weatherradar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.atlas.mars.weatherradar.alarm.SampleBootReceiver;
import com.atlas.mars.weatherradar.fragments.BoridpolRadar;
import com.atlas.mars.weatherradar.fragments.InfraRed;
import com.atlas.mars.weatherradar.fragments.Visible;

import java.io.IOException;
import java.util.HashMap;


public class MainActivity extends FragmentActivity implements Communicator{
    public  final static String LOCATION = "LOCATION";
    final String LOG_TAG = "MainActivityLogs";

    ViewPager pager;
    PagerAdapter pagerAdapter;
    BoridpolRadar boridpolRadar;
    InfraRed infraRed;
    Visible visible;
    DataBaseHelper db;

    NotificationManager nm;
    AlarmManager am;
    Intent intent1;
    Intent intent2;
    PendingIntent pIntent1;
    PendingIntent pIntent2;
    long startAlarm;
    static MyReceiver myReceiver;
    HashMap<String, String> mapSetting;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Density(this);
        db = new DataBaseHelper(this);
        startAlarm = db.getStartTime();
        mapSetting = DataBaseHelper.mapSetting;

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);


        if(mapSetting.get(DataBaseHelper.IS_ALARM)!=null && mapSetting.get(DataBaseHelper.IS_ALARM).equals("1")){
            alarmOn();
        }

       /* am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intent1 = createIntent("action 1", "extra 1");
        pIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT );
        am.cancel(pIntent1);
        am.set(AlarmManager.RTC_WAKEUP, startAlarm, pIntent1);
*/
        //am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pIntent1);
    }

    void alarmOn(){
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intent1 = createIntent("action 1", "extra 1");
        pIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT );
        am.cancel(pIntent1);
        //am.set(AlarmManager.RTC_WAKEUP, startAlarm, pIntent1);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5*1000, pIntent1);
    }

    void alarmCancel(){
        if(pIntent1!=null && am!=null){
            pIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT );
            am.cancel(pIntent1);
        }

    }

    Intent createIntent(String action, String extra) {
       /* SampleBootReceiver sm = new SampleBootReceiver();
        sm.setMainActivity(this);*/


        Intent intent = new Intent(this, SampleBootReceiver.class);
        intent.setAction(action);
        intent.putExtra("extra", extra);
        return intent;
    }
    void compare() {
        Log.d(LOG_TAG, "intent1 = intent2: " + intent1.filterEquals(intent2));
        Log.d(LOG_TAG, "pIntent1 = pIntent2: " + pIntent1.equals(pIntent2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ActivitySetting.class);
            startActivityForResult(intent,0);
            return true;
        }
        if (id == R.id.action_reload) {
            reloadAll();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadAll(){
        boridpolRadar.reloadImg();
        infraRed.reloadImg();
        visible.reloadImg();
    }

    @Override
    public void initView(View v, int position) {
        switch (position){
            case 0:
                boridpolRadar = new BoridpolRadar(v, this, position);
                break;
            case 1:
                infraRed = new InfraRed(v, this, position);
                break;
            case 2:
               visible =  new Visible(v, this, position);
                break;
        }
    }
    public void toastShow(String txt){
        Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //Todo нажато сохранение
                reloadAll();
                Bundle extras = intent.getExtras();
                if(extras.getBoolean(DataBaseHelper.IS_ALARM)){
                    alarmOn();
                }else {
                    alarmCancel();
                }
            }
        }
    }

    /**
     * событие из сервиса
     */
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            String distance = arg1.getExtras().getString("distance");
           // toastShow(distance);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
      //  startService(new Intent(this, MyService.class));
        onCreateMyReceiver();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            toastShow(extras.getString("item_id"));
            Log.i( "dd","Extra:" + extras.getString("item_id") );
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null){
            toastShow(extras.getString("item_id"));
            if(extras.getString("item_id").equals("1001")){
                boridpolRadar.reloadImg();
            }

          //  Log.i( "dd","Extra:" + extras.getString("item_id") );
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(myReceiver);
        super.onPause();
    }

    private void onCreateMyReceiver(){
       /* if(myReceiver!=null){
            unregisterReceiver(myReceiver);
        }*/
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.LOCATION);
        registerReceiver(myReceiver, intentFilter);
    }


}
