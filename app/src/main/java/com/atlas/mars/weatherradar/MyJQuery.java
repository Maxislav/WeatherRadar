package com.atlas.mars.weatherradar;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by mars on 7/9/15.
 */
public class MyJQuery {
    ArrayList<View> allViews;
    public MyJQuery() {
        allViews = new ArrayList<View>();
    }
    public ArrayList<View> findViewByTagClass(ViewGroup root, Class type){
        allViews = new ArrayList<View>();
        return  getViewsByTag(root, type);

    };

    public ArrayList<View> getViewsByTag(ViewGroup root, Class type) {

        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = root.getChildAt(i);

            if (childView instanceof ViewGroup && 0 < ((ViewGroup) childView).getChildCount()) {
                getViewsByTag((ViewGroup) childView, type);
            }
            if (type.isInstance(childView)) {
                allViews.add(childView);
            }
        }
        return allViews;
    }

}
