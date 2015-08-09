package com.atlas.mars.weatherradar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.atlas.mars.weatherradar.alarm.SampleBootReceiver;
import com.atlas.mars.weatherradar.fragments.BoridpolRadar;
import com.atlas.mars.weatherradar.fragments.InfraRed;
import com.atlas.mars.weatherradar.fragments.MyFragment;
import com.atlas.mars.weatherradar.fragments.Visible;

import java.util.HashMap;


public class MainActivity extends FragmentActivity implements Communicator, ViewPager.OnPageChangeListener, View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    public  final static String LOCATION = "LOCATION";
    final String TAG = "MainActivityLogs";

    ViewPager pager;
    PagerAdapter pagerAdapter;
    BoridpolRadar boridpolRadar;
    int posinion;

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

    ImageButton buttonReload;
    ImageButton buttonMenu;
    TextView title;
    LinearLayout forecastLinearLayout;

    HashMap<Integer, Object> fragmetMap;
    ScrollView scrollView;
    FrameLayout frLayoutCurrent;
    CurrentWeather currentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Density(this);
        db = new DataBaseHelper(this);
        startAlarm = db.getStartTime();
        mapSetting = DataBaseHelper.mapSetting;

        buttonReload = (ImageButton)findViewById(R.id.buttonReload);
        buttonMenu = (ImageButton)findViewById(R.id.buttonMenu);
        title = (TextView)findViewById(R.id.title);
        forecastLinearLayout = (LinearLayout)findViewById(R.id.forecastLinearLayout);
        frLayoutCurrent = (FrameLayout)findViewById(R.id.frLayoutCurrent);
        currentWeather = new CurrentWeather(this, frLayoutCurrent);

        new Forecast(this, forecastLinearLayout);

        buttonMenu.setOnClickListener(this);
        buttonReload.setOnClickListener(this);



        fragmetMap = new HashMap<>();

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);
        pager.setOnPageChangeListener(this);

        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(Density.widthPixels*1.34));
        pager.setLayoutParams(parms);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        setSisze();

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

        new ScroolObserv(this, scrollView);
    }

    private void setSisze(){
        ViewTreeObserver observer = ((LinearLayout)buttonReload.getParent()).getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonReload.setLayoutParams(new  LinearLayout.LayoutParams (buttonReload.getHeight(),buttonReload.getHeight() ));
                buttonMenu.setLayoutParams(new  LinearLayout.LayoutParams ((int)(buttonReload.getHeight()/1.5),buttonMenu.getHeight() ));
            }
        });

        ViewTreeObserver observer1 = (scrollView).getViewTreeObserver();
        observer1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              //  scrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                if (Build.VERSION.SDK_INT < 16) {
                    scrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                Log.d(TAG, "" + scrollView.getHeight() + " : " + scrollView.getChildAt(0).getHeight());

                //todo установка позиции скрода при старте
              //  scrollView.scrollTo(0, scrollView.getChildAt(0).getHeight() - scrollView.getHeight());
            }
        });


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
        Log.d(TAG, "intent1 = intent2: " + intent1.filterEquals(intent2));
        Log.d(TAG, "pIntent1 = pIntent2: " + pIntent1.equals(pIntent2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        onOptionsItemSelected(item);
        return false;
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
               // LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams().

            //LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(Density.widthPixels*1.34));
                boridpolRadar = new BoridpolRadar(v, this, position);
                fragmetMap.put(position, boridpolRadar);
                break;
            case 1:
                infraRed = new InfraRed(v, this, position);
                fragmetMap.put(position, infraRed);
                break;
            case 2:
               visible =  new Visible(v, this, position);
                fragmetMap.put(position, visible);
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
       setMyTitle(position);
       Log.d(TAG, "position: "+ position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonReload:

                MyFragment myFragment = (MyFragment)fragmetMap.get(posinion);// (MyFragment)pager.getChildAt(posinion);
                myFragment.reloadImg();
             //   reloadImg();
                break;
            case R.id.buttonMenu:
                PopupMenu popupMenu = new PopupMenu(this, v);
                popupMenu.inflate(R.menu.menu_main);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                break;
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
        setMyTitle(pager.getCurrentItem());
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            toastShow(extras.getString("item_id"));
            Log.i( "dd","Extra:" + extras.getString("item_id") );
        }
        currentWeather.onResum();
    }

    public void setCityName(String name){
        title.setText(name);
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

    void setMyTitle(int pos){
        posinion = pos;
        String titleText = "";
      /*  if(mapSetting.get("title"+(pos+1))==null){
            title.setText(titleText);
        }else{
            title.setText(mapSetting.get("title"+(pos+1)));
        }
*/
    }







}
