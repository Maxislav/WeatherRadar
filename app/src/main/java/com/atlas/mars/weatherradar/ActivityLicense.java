package com.atlas.mars.weatherradar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;

import com.squareup.phrase.Phrase;

/**
 * Created by Администратор on 7/8/15.
 */
public class ActivityLicense extends AppCompatActivity implements View.OnClickListener {
    String web;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        web = getString(R.string.web);
        onDraw();

    }

    private void onDraw() {
        parsePasteWeb((WebView)findViewById(R.id.webStartOnMap), getString(R.string.license_text) );
    }
    private void parsePasteWeb(WebView browser,String put ){
        CharSequence formatted = Phrase.from(web).put("content", put).format();
        browser.loadData(formatted.toString(), "text/html; charset=UTF-8", null);
    }

    @Override
    public void onClick(View v) {

    }
}
