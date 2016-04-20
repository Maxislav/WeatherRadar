package com.atlas.mars.weatherradar.WeatherPager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.atlas.mars.weatherradar.Communicator;
import com.atlas.mars.weatherradar.R;

;

/**
 * Created by mars on 7/8/15.
 */
public class PageFragment extends Fragment {
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    int pageNumber;

    static PageFragment newInstance(int page) {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        //Log.d(TAG, "savedPageNumber");
        return pageFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        Communicator communicator;
        communicator = (Communicator)getActivity();
        view = inflater.inflate(R.layout.fragment_0, null);
        /*switch (pageNumber) {
            case 0:
                view = inflater.inflate(R.layout.fragment_0, null);

                //communicator.initViewHome(view, inflater);
                break;
            case 1:
                view = inflater.inflate(R.layout.fragment_0, null);
               // communicator.initView(view, pageNumber);
                //communicator.initViewHome(view, inflater);
                break;
            case 2:
                view = inflater.inflate(R.layout.fragment_0, null);
                //communicator.initViewHome(view, inflater);
                break;

        }*/
        communicator.initView(view, pageNumber);
        return  view;
    }
}
