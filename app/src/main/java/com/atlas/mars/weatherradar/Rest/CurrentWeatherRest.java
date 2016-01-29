package com.atlas.mars.weatherradar.Rest;

import com.atlas.mars.weatherradar.BuildConfig;

import java.util.HashMap;

/**
 * Created by mars on 1/29/16.
 */
public class CurrentWeatherRest {
    OnAccept onAccept;
    String cityId;

    public CurrentWeatherRest(OnAccept onAccept, String cityId) {
        this.onAccept = onAccept;
        this.cityId = cityId;
    }

    private interface Constant{
        public String URL = BuildConfig.CURRENT_WEATHER_URL;
    }

    public interface OnAccept{
        void accept(HashMap<String, ? > map);
    }
}
