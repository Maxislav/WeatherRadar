package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by Администратор on 8/16/15.
 */
public class FragmentImageAction extends Fragment implements View.OnClickListener{
    View actionView;
    MainActivity mainActivity;
    Activity activity;
    DataBaseHelper db;
    HashMap <String, String> mapSetting;
    TextView textViewTitle;
    ImageButton buttonReload;
    int i;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        actionView = inflater.inflate(R.layout.fragment_image_action, null);
        mapSetting = DataBaseHelper.mapSetting;
        textViewTitle = (TextView)actionView.findViewById(R.id.textViewTitle);
        buttonReload = (ImageButton)actionView.findViewById(R.id.buttonReload);
        buttonReload.setOnClickListener(this);
        return actionView;
    }
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        mainActivity = (MainActivity)activity;

        // _onStart();
    }

    public void setTitle(int i){
        this.i = i;
        if(textViewTitle!=null){
            textViewTitle.setText(mapSetting.get("title"+(i+1)));
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        textViewTitle.setText(mapSetting.get("title"+(i+1)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonReload:
                mainActivity.onClick(v);
                break;
        }
    }
}
