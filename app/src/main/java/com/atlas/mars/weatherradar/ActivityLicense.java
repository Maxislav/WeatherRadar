package com.atlas.mars.weatherradar;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.squareup.phrase.Phrase;

/**
 * Created by Администратор on 7/8/15.
 */
public class ActivityLicense   {
    String web;
    FrameLayout row;
    Activity activity;
    public ActivityLicense( Activity activity, FrameLayout row) {
        this.activity = activity;
        this.row = row;
        web = activity.getString(R.string.web);
        onDraw();
    }

    private void onDraw() {

        parsePasteWeb((WebView)(row.findViewById(R.id.webStartOnMap)),
                activity.getString(R.string.license_text),
                activity.getString(R.string.licence_link1),
                activity.getString(R.string.licence_link2) );
    }
    private void parsePasteWeb(WebView browser, String put, String link1, String link2 ){
        Log.d("TAG", link1);
        CharSequence formatted = Phrase.from(web).put("content", put).put("link", link1).put("link_", link2).format();
        browser.loadData(formatted.toString(), "text/html; charset=UTF-8", null);
    }




}
