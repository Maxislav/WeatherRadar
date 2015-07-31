package com.atlas.mars.weatherradar;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by mars on 6/24/15.
 */
//@ReportsCrashes( formUri = "http://www.bugsense.com/api/acra?api_key=863af310",  mode = ReportingInteractionMode.TOAST,
//@ReportsCrashes( formUri = "http://192.168.126.73:8000/AtlasRevolution/acra/rest.php?key=000",  mode = ReportingInteractionMode.TOAST,
@ReportsCrashes( formUri = "http://178.62.44.54/dev/acra/rest.php?key=863af310&app=WeatherCheater",  mode = ReportingInteractionMode.TOAST,
//@ReportsCrashes( formUri = "http://192.168.126.73:88?key=000",  mode = ReportingInteractionMode.TOAST,
        forceCloseDialogAfterToast = false, // optional, default false
        resToastText = R.string.app_error,
        httpMethod = HttpSender.Method.POST
)
public class WeatherCheater extends Application {
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        super.onCreate();
        ACRA.init(this);

    }
}
