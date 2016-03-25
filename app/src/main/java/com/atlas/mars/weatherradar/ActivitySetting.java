package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.atlas.mars.weatherradar.dialog.MyDialog;
import com.atlas.mars.weatherradar.dialog.OnEvents;
import com.atlas.mars.weatherradar.loader.Loader;
import com.atlas.mars.weatherradar.timepisker.TimePickerColor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Администратор on 7/8/15.
 */
public class ActivitySetting extends AppCompatActivity implements TimePicker.OnTimeChangedListener, View.OnClickListener, CheckBox.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, OnEvents {
    final String TAG = "ActivitySettingLog";
    DataBaseHelper db;
    HashMap<String, String> mapSetting;
    MyJQuery jQuery;
    ArrayList<View> arrayEditText;
    TimePicker timePickerFrom, timePickerTo;
    TextView textViewFrom, textViewTo;
    int fromHour = 8, fromMin = 0, toHour = 22, toMin = 0;
    CheckBox isAlarm, isMorningAlarm;
    EditText timeRepeat, edTextRadius;
    Button btnLoadSetting;
    FrameLayout globalLayout;
    static ObjectMapper mapper = new ObjectMapper();
    PackageInfo pInfo;
    Spinner citySpinner;
    int spinerSelection = 1;
    List<Model> cityCollection;
    CustomArrayAdapter adapter;
    MyDialog dialogLicence;
    SeekBar sb;
    TextView seekBarText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        fromHour = 8;
        fromMin = 0;
        toHour = 22;
        toMin = 0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        db = new DataBaseHelper(this);
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setTitle("V " + pInfo.versionName + "." + Integer.toString(BuildConfig.VERSION_CODE));

        mapSetting = DataBaseHelper.mapSetting;
        jQuery = new MyJQuery();
        globalLayout = (FrameLayout) findViewById(R.id.globalLayout);
        arrayEditText = jQuery.findViewByTagClass((ViewGroup) globalLayout.findViewById(R.id.urlMaps), EditText.class);
        timePickerFrom = (TimePicker) findViewById(R.id.timePickerFrom);
        timePickerTo = (TimePicker) findViewById(R.id.timePickerTo);
        timeRepeat = (EditText) findViewById(R.id.timeRepeat);
        edTextRadius = (EditText) findViewById(R.id.edTextRadius);
        btnLoadSetting = (Button) findViewById(R.id.btnLoadSetting);
        sb = (SeekBar) findViewById(R.id.seekBar);
        seekBarText = (TextView) findViewById(R.id.seekBarText);

        onSeekBarEvents();

        btnLoadSetting.setOnClickListener(this);

        isAlarm = (CheckBox) findViewById(R.id.isAlarm);
        isMorningAlarm = (CheckBox) findViewById(R.id.isMorningAlarm);
        timePickerFrom.setIs24HourView(true);
        timePickerTo.setIs24HourView(true);

        new TimePickerColor(this, timePickerFrom);
        new TimePickerColor(this, timePickerTo);

        timePickerFrom.setOnTimeChangedListener(this);
        timePickerTo.setOnTimeChangedListener(this);

        textViewFrom = (TextView) findViewById(R.id.textViewFrom);
        textViewTo = (TextView) findViewById(R.id.textViewTo);

        setTimeFromTo();
        inflateSetting();

        citySpinner = (Spinner) findViewById(R.id.citySpinner);
        //String[] planets = getResources().getStringArray(R.array.array_cities);

        Cities cities = new Cities(this);
        String[] planets = cities.getCities();
        // List<String> options = Arrays.asList(planets);


        cityCollection = getCityCollection(planets);
        adapter = new CustomArrayAdapter(this, R.layout.spinner_item, cityCollection);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        if (mapSetting.get(db.MY_LOCATION) != null) {
            spinerSelection = Integer.parseInt(mapSetting.get(db.MY_LOCATION));
        }
        citySpinner.setSelection(spinerSelection);
        citySpinner.setOnItemSelectedListener(this);

    }

    List<Model> getCityCollection(String[] planets) {
        int i = 0;
        //String[] planets = getResources().getStringArray(R.array.array_cities);
        List<String> options = Arrays.asList(planets);
        List<Model> listOptions = new ArrayList<>();
        for (String option : options) {
            Model model = new Model(option, i == spinerSelection, i);
            listOptions.add(model);
            i++;
        }
        return listOptions;
    }


    public class Model {
        String name;
        boolean select = false;
        int position;

        Model(String name, boolean select, int position) {
            this.name = name;
            this.select = select;
            this.position = position;
        }

        public String getName() {
            return this.name;
        }

        public void setSelect(boolean select) {
            this.select = select;
        }

        public int getPosition() {
            return position;
        }

        public boolean isSelect() {
            return select;
        }
    }

    void setTimeFromTo() {

        if (mapSetting.get(DataBaseHelper.TIME_FROM_HOUR) != null) {
            fromHour = Integer.parseInt(mapSetting.get(DataBaseHelper.TIME_FROM_HOUR));
        }
        if (mapSetting.get(DataBaseHelper.TIME_FROM_MINUTE) != null) {
            fromMin = Integer.parseInt(mapSetting.get(DataBaseHelper.TIME_FROM_MINUTE));
        }
        if (mapSetting.get(DataBaseHelper.TIME_TO_HOUR) != null) {
            toHour = Integer.parseInt(mapSetting.get(DataBaseHelper.TIME_TO_HOUR));
        }
        if (mapSetting.get(DataBaseHelper.TIME_TO_MINUTE) != null) {
            toMin = Integer.parseInt(mapSetting.get(DataBaseHelper.TIME_TO_MINUTE));
        }

        timePickerFrom.setCurrentHour(fromHour);
        timePickerFrom.setCurrentMinute(fromMin);
        timePickerTo.setCurrentHour(toHour);
        timePickerTo.setCurrentMinute(toMin);
        textViewFrom.setText("" + fromHour + ":" + getMinutes(fromMin));
        textViewTo.setText("" + toHour + ":" + getMinutes(toMin));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save_and_close:
                saveAndClose();
                return true;
            case R.id.action_close:
                finish();
        }
        return false;
    }

    private void inflateSetting() {


        if (mapSetting.get(DataBaseHelper.MORNING_ALARM) != null && mapSetting.get(DataBaseHelper.MORNING_ALARM).equals("1")) {
            isMorningAlarm.setChecked(true);
        }

        if (mapSetting.get(DataBaseHelper.TIME_REPEAT) != null) {
            timeRepeat.setText(mapSetting.get(DataBaseHelper.TIME_REPEAT));
        } else {
            timeRepeat.setText("2");
        }
        if (mapSetting.get(DataBaseHelper.IS_ALARM) != null && mapSetting.get(DataBaseHelper.IS_ALARM).equals("1")) {
            isAlarm.setChecked(true);
        }

        if (mapSetting.get(DataBaseHelper.RADIUS_ALARM) != null && !mapSetting.get(DataBaseHelper.RADIUS_ALARM).isEmpty()) {
            edTextRadius.setText(mapSetting.get(DataBaseHelper.RADIUS_ALARM));
        }

        if (mapSetting.get(DataBaseHelper.SEED_BARR_VALUE) != null) {
            double z = Double.parseDouble(mapSetting.get(DataBaseHelper.SEED_BARR_VALUE));
            z = z * 100 / 2;
            int position = (int) z;
            sb.setProgress(position);
        }


        inflateUrlSetting(mapSetting);


        isMorningAlarm.setOnCheckedChangeListener(this);

    }

    void inflateUrlSetting(HashMap<String, String> mapSetting) {
        if (mapSetting.get(DataBaseHelper.TITLE1) != null) {
            ((EditText) arrayEditText.get(0)).setText(mapSetting.get(DataBaseHelper.TITLE1));
        }

        if (mapSetting.get(DataBaseHelper.TITLE2) != null) {
            ((EditText) arrayEditText.get(2)).setText(mapSetting.get(DataBaseHelper.TITLE2));
        }

        if (mapSetting.get(DataBaseHelper.TITLE3) != null) {
            ((EditText) arrayEditText.get(4)).setText(mapSetting.get(DataBaseHelper.TITLE3));
        }

        if (mapSetting.get(DataBaseHelper.URL1) != null) {
            ((EditText) arrayEditText.get(1)).setText(mapSetting.get(DataBaseHelper.URL1));
        }
        if (mapSetting.get(DataBaseHelper.URL1) != null) {
            ((EditText) arrayEditText.get(3)).setText(mapSetting.get(DataBaseHelper.URL2));
        }
        if (mapSetting.get(DataBaseHelper.URL3) != null) {
            ((EditText) arrayEditText.get(5)).setText(mapSetting.get(DataBaseHelper.URL3));
        }
    }

    private void saveAndClose() {
        saveSetting();
        Intent answerIntent = new Intent();
        answerIntent.putExtra(DataBaseHelper.IS_ALARM, isAlarm.isChecked());
        answerIntent.putExtra(DataBaseHelper.MORNING_ALARM, isMorningAlarm.isChecked());
        setResult(RESULT_OK, answerIntent);
        finish();
    }

    private void saveSetting() {
        String title1 = ((EditText) arrayEditText.get(0)).getText().toString();
        String title2 = ((EditText) arrayEditText.get(2)).getText().toString();
        String title3 = ((EditText) arrayEditText.get(4)).getText().toString();

        String url1 = ((EditText) arrayEditText.get(1)).getText().toString();
        String url2 = ((EditText) arrayEditText.get(3)).getText().toString();
        String url3 = ((EditText) arrayEditText.get(5)).getText().toString();

        mapSetting.put(DataBaseHelper.TITLE1, title1);
        mapSetting.put(DataBaseHelper.TITLE2, title2);
        mapSetting.put(DataBaseHelper.TITLE3, title3);


        mapSetting.put(DataBaseHelper.URL1, url1);
        mapSetting.put(DataBaseHelper.URL2, url2);
        mapSetting.put(DataBaseHelper.URL3, url3);

        mapSetting.put(DataBaseHelper.TIME_FROM_HOUR, "" + fromHour);
        mapSetting.put(DataBaseHelper.TIME_FROM_MINUTE, "" + fromMin);
        mapSetting.put(DataBaseHelper.TIME_TO_HOUR, "" + toHour);

        mapSetting.put(DataBaseHelper.TIME_TO_MINUTE, "" + toMin);
        mapSetting.put(DataBaseHelper.TIME_REPEAT, timeRepeat.getText().toString());

        mapSetting.put(DataBaseHelper.SEED_BARR_VALUE, seekBarText.getText().toString());

        if (isAlarm.isChecked()) {
            mapSetting.put(DataBaseHelper.IS_ALARM, "1");
        } else {
            mapSetting.put(DataBaseHelper.IS_ALARM, "0");
        }

        if (isMorningAlarm.isChecked()) {
            mapSetting.put(DataBaseHelper.MORNING_ALARM, "1");
        } else {
            mapSetting.put(DataBaseHelper.MORNING_ALARM, "0");
        }

        if (!edTextRadius.getText().toString().isEmpty()) {
            mapSetting.put(DataBaseHelper.RADIUS_ALARM, edTextRadius.getText().toString());
        } else {
            mapSetting.put(DataBaseHelper.RADIUS_ALARM, "40");
        }
        mapSetting.put(db.MY_LOCATION, citySpinner.getSelectedItemPosition() + "");
        db.saveSetting();
    }

    boolean isInitTimeFrom = false, isInitTimeTo = false;

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

        switch (view.getId()) {
            case R.id.timePickerFrom:

                if (isInitTimeFrom) {
                    textViewFrom.setText("" + hourOfDay + ":" + getMinutes(minute));
                    fromMin = minute;
                    fromHour = hourOfDay;
                }
                isInitTimeFrom = true;

                break;
            case R.id.timePickerTo:

                if (isInitTimeTo) {
                    textViewTo.setText("" + hourOfDay + ":" + getMinutes(minute));
                    toMin = minute;
                    toHour = hourOfDay;
                }
                isInitTimeTo = true;

                break;
        }

        Log.d(TAG, "" + hourOfDay + ":" + minute);
    }

    String getMinutes(int minute) {
        if (minute < 10) {
            return "0" + minute;
        }
        return "" + minute;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLoadSetting:
                LoadTask loadTask = new LoadTask(this);
                loadTask.execute();
                break;
        }
    }

    void callbackLoadTask(HashMap<String, String> map) {
        if (map == null) return;
        inflateUrlSetting(map);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, buttonView.getId() + "");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (mapSetting.get(db.LICENCE) == null || !mapSetting.get(db.LICENCE).equals("1") && position == 0) {

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    dialogLicence = new MyDialog(ActivitySetting.this, R.layout.license);
                    View view = new View(ActivitySetting.this);
                    dialogLicence.show(view);
                }
            }, 100L);
        } else {
            setSpinerSelection(cityCollection, position);
        }
    }

    @Override
    public void dialogOnOk() {
        // setSpinerSelection(cityCollection, 0);
        mapSetting.put(db.LICENCE, "1");
        db.saveSetting();
        citySpinner.setSelection(0);
    }

    @Override
    public void dialogOnCancel() {
        citySpinner.setSelection(spinerSelection);
        //setSpinerSelection(cityCollection, spinerSelection);
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    void setSpinerSelection(List<Model> list, int position) {
        for (Model model : list) {
            if (model.getPosition() == position) {
                model.setSelect(true);
            } else {
                model.setSelect(false);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public void onSeekBarEvents() {
        sb.setOnSeekBarChangeListener(new onSeekBarListener());
    }

    private class onSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Log the progress

            double z = (double) progress;
            z = (z * 2 / 100);
            seekBarText.setText(z + "");
            Log.d(TAG, "Progress is: " + z);
            //set textView's text
            // yourTextView.setText(""+progress);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    }


    private class LoadTask extends AsyncTask<String, Void, HashMap<String, String>> {
        HttpURLConnection urlConnection;
        Activity activity;
        Loader loader;

        LoadTask(Activity activity) {
            super();
            this.activity = activity;
            loader = new Loader(activity, globalLayout);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loader.show();

        }


        @Override
        protected HashMap<String, String> doInBackground(String... params) {
            HashMap<String, String> map = new HashMap<>();
            URL url = null;
            InputStream in = null;
            StringBuilder sb = new StringBuilder();
            try {
                url = new URL("http://178.62.44.54/php/settingborispolradar.php");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                Scanner inStream = new Scanner(urlConnection.getInputStream());
                while (inStream.hasNextLine()) {
                    sb.append(inStream.nextLine());

                }
            } catch (IOException e) {
                Log.e(TAG, e.toString(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }


            if (0 < sb.length()) {
                String json = sb.toString();
                try {
                    ObjectNode root = (ObjectNode) mapper.readTree(json);
                    map.put("title1", root.get("title1").asText());
                    map.put("title2", root.get("title2").asText());
                    map.put("title3", root.get("title3").asText());
                    map.put("url1", root.get("url1").asText());
                    map.put("url2", root.get("url2").asText());
                    map.put("url3", root.get("url3").asText());

                } catch (IOException e) {
                    Log.e(TAG, e.toString(), e);
                    e.printStackTrace();
                }

            }
            return map;
        }

        @Override
        protected void onPostExecute(HashMap result) {
            //Log.d(TAG, result);
            // onCallback(result);
            loader.hide();
            callbackLoadTask(result);
        }
    }
}
