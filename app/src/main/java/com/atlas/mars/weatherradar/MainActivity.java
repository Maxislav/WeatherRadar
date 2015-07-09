package com.atlas.mars.weatherradar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.atlas.mars.weatherradar.fragments.BoridpolRadar;
import com.atlas.mars.weatherradar.fragments.InfraRed;
import com.atlas.mars.weatherradar.fragments.Visible;


public class MainActivity extends FragmentActivity implements Communicator{
    ViewPager pager;
    PagerAdapter pagerAdapter;
    BoridpolRadar boridpolRadar;
    InfraRed infraRed;
    Visible visible;
    DataBaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Density(this);
        db = new DataBaseHelper(this);

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //Todo нажато сохранение
                reloadAll();
            }
        }
    }
}
