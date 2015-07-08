package com.atlas.mars.weatherradar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * Created by mars on 7/8/15.
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    static final int PAGE_COUNT = 3;
    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(LogTags.TAG, "getItem " + position);

        return PageFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
