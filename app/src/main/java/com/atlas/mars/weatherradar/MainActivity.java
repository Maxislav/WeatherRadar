package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
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

import com.atlas.mars.weatherradar.alarm.MorningBroadCast;
import com.atlas.mars.weatherradar.alarm.RegenBorispolBroadCast;
import com.atlas.mars.weatherradar.alarm.SampleBootReceiver;
import com.atlas.mars.weatherradar.dialog.MyDialog;
import com.atlas.mars.weatherradar.dialog.OnEvents;
import com.atlas.mars.weatherradar.fragments.BoridpolRadar;
import com.atlas.mars.weatherradar.fragments.InfraRed;
import com.atlas.mars.weatherradar.fragments.MyFragment;
import com.atlas.mars.weatherradar.fragments.Visible;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends FragmentActivity implements Communicator, ViewPager.OnPageChangeListener, View.OnClickListener, PopupMenu.OnMenuItemClickListener, ToastShow, OnEvents {
    public final static String LOCATION = "LOCATION";
    final String TAG = "MainActivityLogs";
    private int posinion;

    public int scrollSliderSize;

    ViewPager pager;
    PagerAdapter pagerAdapter;
    BoridpolRadar boridpolRadar;
    InfraRed infraRed;
    Visible visible;
    FragmentManager fragmentManager;

    HashMap<Integer, MyFragment> mapFragments;

    DataBaseHelper db;

    NotificationManager nm;
    AlarmManager am, alarmManagerMorning, alarmRegenBorispol;
    Intent intent1, morningIntent, borispolRegenIntent;
    Intent intent2;
    PendingIntent pIntent1;
    PendingIntent pIntent2;
    PendingIntent pIntent3;
    long startAlarm;
    static MyReceiver myReceiver;
    static RegenBorispolBroadCast regenBorispolBroadCast;
    HashMap<String, String> mapSetting;

    // ImageButton buttonReload;
    ImageButton buttonMenu;
    TextView title;
    LinearLayout forecastLinearLayout;

    HashMap<Integer, Object> fragmetMap;
    ScrollView scrollView;
    FrameLayout frLayoutCurrent;
    CurrentWeather currentWeather;
    final static String OLOO = BuildConfig.BorispolParseRain;
    FragmentTransaction fragmentTransaction;
    Fragment fragmentWeather, fragmentImageAction;
    boolean isFromNotification = false;

    Forecast forecast;

    MyDialog dialogLicence;

    boolean isActivityLeave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Density(this);
        db = new DataBaseHelper(this);
        startAlarm = db.getStartTime();
        mapSetting = DataBaseHelper.mapSetting;

        _onStart();


    }

    private void _onStart() {
        //todo закоментировать
        //boolean isWork = db.isWorkTime();


        // db.deleteValue(DataBaseHelper.TIME_NOTIFY);

        //  buttonReload = (ImageButton)findViewById(R.id.buttonReload);
        buttonMenu = (ImageButton) findViewById(R.id.buttonMenu);
        title = (TextView) findViewById(R.id.title);
        forecastLinearLayout = (LinearLayout) findViewById(R.id.forecastLinearLayout);

//        frLayoutCurrent = (FrameLayout)findViewById(R.id.frLayoutCurrent);
//        forecast =  new Forecast(this, forecastLinearLayout);

        mapFragments = new HashMap<>();

        buttonMenu.setOnClickListener(this);
        // buttonReload.setOnClickListener(this);
        fragmetMap = new HashMap<>();
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);
        pager.setOnPageChangeListener(this);

        LinearLayout.LayoutParams parms;

        if(isLandscapeMode()){
            parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (Density.widthPixels / 1.34));
        }else{
            parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (Density.widthPixels));
        }

        pager.setLayoutParams(parms);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        setSisze();


        if (mapSetting.get(DataBaseHelper.IS_ALARM) != null && mapSetting.get(DataBaseHelper.IS_ALARM).equals("1")) {
            alarmOn();
        }
        if (mapSetting.get(DataBaseHelper.MORNING_ALARM) != null && mapSetting.get(DataBaseHelper.MORNING_ALARM).equals("1")) {
            morningAlarm();
        }

        Log.d(TAG, BuildConfig.BorispolParseRain);


        /**
         * Текущая погода
         */

        //frLayoutCurrent = (FrameLayout)findViewById(R.id.frLayoutCurrent);






        //todo закоментировать
        // new MyRestTest();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        isActivityLeave = true;
        outState.putBoolean("isActivityLeave", isActivityLeave);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isActivityLeave = savedInstanceState.getBoolean("isActivityLeave");
        Log.d("isActivityLeave", savedInstanceState.getBoolean("isActivityLeave")+"");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (forecast == null) {
            //frLayoutCurrent = (FrameLayout) findViewById(R.id.frLayoutCurrent);
            forecast = new Forecast(this, forecastLinearLayout);
        }else if (updateForecastIsNeeded()) {
            if (forecast != null) {
                forecast.onRegen();
            } else {
               // frLayoutCurrent = (FrameLayout) findViewById(R.id.frLayoutCurrent);
                forecast = new Forecast(this, forecastLinearLayout);
            }
        }

        //  startService(new Intent(this, MyService.class));
        onCreateMyReceiver();
        //setMyTitle(pager.getCurrentItem());
        Bundle extras = getIntent().getExtras();

        if (pager.getCurrentItem() == 0) {
            alarmRegenBorispol = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            borispolRegenIntent = createIntent("borispolAction", "regetExtras", RegenBorispolBroadCast.class);
            pIntent3 = PendingIntent.getBroadcast(this, 0, borispolRegenIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmRegenBorispol.cancel(pIntent3);
            alarmRegenBorispol.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 1000, 2 * 60 * 1000, pIntent3);
        }

        Log.d(TAG, "Current page: " + pager.getCurrentItem());

        fragmentWeather = new CurrentWeather();
        fragmentImageAction = new FragmentImageAction();
        fragmentManager = getFragmentManager();

        if(!isActivityLeave){
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.frLayoutCurrent, fragmentWeather);
            fragmentTransaction.commit();
        };
    }


    @Override
    protected void onPause() {
        unregisterReceiver(myReceiver);

        if(pIntent3!=null)
            alarmRegenBorispol.cancel(pIntent3);
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Смена лайаута текущей погоды
     *
     * @param i
     */
    public void changeFragmentBar(int i) {

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);



        switch (i) {
            case 0:
                    fragmentTransaction.replace(R.id.frLayoutCurrent, fragmentWeather);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                break;
            case 1:
                    fragmentTransaction.replace(R.id.frLayoutCurrent, fragmentImageAction);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                ((FragmentImageAction) fragmentImageAction).setTitle(posinion);
                break;
        }

    }

    private ListFragment getListFragment() {
        ListFragment listFragment = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.frLayoutCurrent);
        if (listFragment == null) {
            listFragment = new ListFragment();
        }
        return listFragment ;
    }


    private void setSisze() {
        final MainActivity mainActivity = this;
       /* ViewTreeObserver observer = ((LinearLayout)buttonReload.getParent()).getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                buttonReload.setLayoutParams(new  LinearLayout.LayoutParams (buttonReload.getHeight(),buttonReload.getHeight() ));
                buttonMenu.setLayoutParams(new  LinearLayout.LayoutParams ((int)(buttonReload.getHeight()/1.5),buttonMenu.getHeight() ));
            }
        });*/

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
                scrollSliderSize = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
                new ScrollObserv(mainActivity, scrollView, scrollSliderSize);
                //todo установка позиции скрода при старте
                //  scrollView.scrollTo(0, scrollView.getChildAt(0).getHeight() - scrollView.getHeight());
            }
        });
    }

    void morningAlarm() {

        //todo закоментировать тестовый вызов утреннего срвиса
         //startService(new Intent(this, MorningService.class));

        long time = db.getMorningWakeUp();
        alarmManagerMorning = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        morningIntent = createIntent("morningAction", "extraMorning", MorningBroadCast.class);
        startService(morningIntent);
        pIntent2 = PendingIntent.getBroadcast(this, 0, morningIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManagerMorning.cancel(pIntent2);

        alarmManagerMorning.set(AlarmManager.RTC_WAKEUP, time, pIntent2);
        //todo для отладки и старта будильника сейчас
       // alarmManagerMorning.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1*1000, pIntent2);
    }

    void morningAlarmCancel() {
        if (pIntent2 != null && alarmManagerMorning != null) {
            pIntent2 = PendingIntent.getBroadcast(this, 0, morningIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManagerMorning.cancel(pIntent1);
        }
    }


    void alarmOn() {
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intent1 = createIntent("action 1", "extra 1", SampleBootReceiver.class);
        pIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pIntent1);

        //todo разобраться
        //am.set(AlarmManager.RTC_WAKEUP, startAlarm, pIntent1);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1 * 1000, pIntent1);
    }

    void alarmCancel() {
        if (pIntent1 != null && am != null) {
            pIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
            am.cancel(pIntent1);
        }

    }


    Intent createIntent(String action, String extra, Class c) {
        //Intent intent = new Intent(this, SampleBootReceiver.class);
        Intent intent = new Intent(this, c);
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
        Intent intent;
        if (id == R.id.action_settings) {
            intent = new Intent(this, ActivitySetting.class);
            startActivityForResult(intent, 0);
            return true;
        }
        if (id == R.id.action_reload) {
            reloadAll();
            return true;
        }
        if (id == R.id.action_license) {
            MyDialog myDialog = new MyDialog(this, R.layout.license);
            View view = new View(this);
            myDialog.show(view);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadAll() {

        for (Map.Entry entry : mapFragments.entrySet()) {
            ((BoridpolRadar) entry.getValue()).reloadImg();

            /*System.out.println("Key: " + entry.getKey() + " Value: "
                    + entry.getValue());*/
        }
       /* boridpolRadar.reloadImg();
        infraRed.reloadImg();
        visible.reloadImg();*/
    }

    @Override
    public void initView(View v, int position) {
        mapFragments.put(position, new BoridpolRadar(v, this, position));
        if (position == 0) {
            mapFragments.get(0).firstLoad();
        }
       /* switch (position){
            case 0:
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
        }*/
    }


    @Override
    public void show(String txt) {
        Toast toast = Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void show(String txt, int gravity) {
        Toast toast = Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, 0, 100);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //Todo нажато сохранение
                reloadAll();
                Bundle extras = intent.getExtras();
                if (extras.getBoolean(DataBaseHelper.IS_ALARM)) {
                    alarmOn();
                } else {
                    alarmCancel();
                }
                if (extras.getBoolean(DataBaseHelper.MORNING_ALARM)) {
                    morningAlarm();
                } else {
                    morningAlarmCancel();
                }
            }
        }
        if (requestCode == 1) {
            Log.d(TAG, requestCode + "");
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setMyTitle(position);
        Log.d(TAG, "position: " + position);
        mapFragments.get(position).firstLoad();

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    public void Ololo() {
        //mapFragments.get(posinion).reloadImg();
    }

    @Override
    public void onClick(View v) {
        final View _v = v;
        final MainActivity mainActivity = this;
        switch (v.getId()) {
            case R.id.buttonReload:

               /* MyFragment myFragment = (MyFragment)fragmetMap.get(posinion);// (MyFragment)pager.getChildAt(posinion);
                myFragment.reloadImg();*/
                mapFragments.get(posinion).reloadImg();
                //   reloadImg();
                break;
            case R.id.buttonMenu:
                PopupMenu popupMenu = new PopupMenu(this, v);
                popupMenu.inflate(R.menu.menu_main);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(this);
                break;
        }
    }

    @Override
    public void dialogOnOk() {
        mapSetting.put(db.LICENCE, "1");
        db.saveSetting();
        _onStart();
       // frLayoutCurrent = (FrameLayout) findViewById(R.id.frLayoutCurrent);
        forecast = new Forecast(this, forecastLinearLayout);
    }

    @Override
    public void dialogOnCancel() {
        mapSetting.put(db.LICENCE, "0");
        morningAlarmCancel();
        alarmCancel();
        db.saveSetting();
        finish();
    }

    @Override
    public Activity getActivity() {
        return this;
    }


    /**
     * событие из сервиса
     */
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            String distance = arg1.getExtras().getString("distance");
            boolean regen = arg1.getExtras().getBoolean("regenBorispol");
            if (regen) {
                Log.d(TAG, "Regen " + regen);
                mapFragments.get(pager.getCurrentItem()).reloadImg();
            }
        }
    }

    public void setCityName(String name) {
        title.setText(name);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {

            if (extras.containsKey("dist")) {
                mapFragments.get(0).reloadImg();
                show(extras.getInt("dist") + " km");
                isFromNotification = true;
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(1);
            }

            if (extras.containsKey("time")) {
                if (forecast != null) {
                    forecast.onRegen();
                } else {
                    forecast = new Forecast(this, forecastLinearLayout);
                }
                show(Cities.getStringResourceByName("probability_rain", this) + " " + extras.getString("time") + Cities.getStringResourceByName("hh", this));
                isFromNotification = true;


            }
        }
    }


    boolean updateForecastIsNeeded() {
        boolean a = true;
        String stringDateForecast = db.mapSetting.get(db.TIMESTAMP_FORECAST);
        Date dateForecast;
        if (stringDateForecast != null) {
            dateForecast = db.stringToDate(db.mapSetting.get(db.TIMESTAMP_FORECAST));
            if (dateForecast.getTime() + (60 * 60 * 1000) < System.currentTimeMillis()) {
                a = true;
            } else {
                a = false;
            }
        } else {
            a = true;
        }
        return a;
    }


    private void onCreateMyReceiver() {
       /* if(myReceiver!=null){
            unregisterReceiver(myReceiver);
        }*/
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LOCATION);
        registerReceiver(myReceiver, intentFilter);

    }

    private void setMyTitle(int pos) {
        posinion = pos;
        String titleText = "";

        if(fragmentImageAction!=null &&   fragmentImageAction.isAdded()){
            Log.d(TAG, " isAdded " );
            ((FragmentImageAction) fragmentImageAction).setTitle(posinion);
        };
       // ((FragmentImageAction) fragmentImageAction).setTitle(posinion);

        Log.d(TAG, "Position " + pos);

      /*  if(mapSetting.get("title"+(pos+1))==null){
            title.setText(titleText);
        }else{
            title.setText(mapSetting.get("title"+(pos+1)));
        }
*/
    }

    private boolean isLandscapeMode(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            return false;
        }else {
            return true;
        }
    }


}
