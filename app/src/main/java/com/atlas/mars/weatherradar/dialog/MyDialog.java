package com.atlas.mars.weatherradar.dialog;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.atlas.mars.weatherradar.R;

/**
 * Created by mars on 10/23/15.
 */
public class MyDialog implements View.OnClickListener{


    Integer resId = 0;
    Activity activity;
    View viewDialog;
    DisplayMetrics displayMetrics;
    public float dpHeight, dpWidth, density;
    LayoutInflater inflater;
    PopupWindow pw;
    LinearLayout contentDialog;

    public MyDialog(Activity activity,  Integer resId){
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resId = resId;
    }



    private  void inflate(){
        displayMetrics = activity.getResources().getDisplayMetrics();
        density = displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / density;
        dpWidth = displayMetrics.widthPixels / density;
        viewDialog = inflater.inflate(R.layout.dialog_licence, null);
        pw = new PopupWindow(viewDialog, FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        pw.setOutsideTouchable(false);
        pw.setAnimationStyle(R.style.Animation);

        LinearLayout block =(LinearLayout) viewDialog.findViewById(R.id.block);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(310*density),FrameLayout.LayoutParams.WRAP_CONTENT);
        block.setLayoutParams(params);
        initOkCancelSend();
        inflateContent();

    }

    public void inflateContent(){
        contentDialog = (LinearLayout)viewDialog.findViewById(R.id.contentDialog);
        FrameLayout row = (FrameLayout)inflater.inflate(resId, null);
        contentDialog.addView(row);
    }

    public void show(View view){
        if(pw==null){
            inflate();
        }
        pw.showAtLocation(view, Gravity.CENTER, 0, 0);

    }

    public void hide(){
        pw.dismiss();
    }

    private void initOkCancelSend() {
        FrameLayout btnOk = (FrameLayout) viewDialog.findViewById(R.id.btn_ok);
        FrameLayout btnCancel = (FrameLayout) viewDialog.findViewById(R.id.btn_cancel);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

    }




    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_ok:
                break;
            case R.id.btn_cancel:
                break;
        }
        pw.dismiss();
    }
}
