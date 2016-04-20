package com.atlas.mars.weatherradar.WeatherPager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import com.atlas.mars.weatherradar.LogTags;

/**
 * Created by mars on 7/8/15.
 */
public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    static int pages;

    public FragmentPagerAdapter(FragmentManager fm, int pages) {
        super(fm);
        this.pages = pages;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(LogTags.TagFragmentPagerAdapter, "getItem " + position);

        return PageFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return pages;
    }
}
