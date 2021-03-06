package com.atlas.mars.weatherradar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mars on 1/14/16.
 */
public class CustomArrayAdapter extends ArrayAdapter<ActivitySetting.Model> {

    private List<ActivitySetting.Model> objects;
    private Context context;

    public CustomArrayAdapter(Context context, int resourceId, List<ActivitySetting.Model> objects) {
        super(context, resourceId, objects);
        this.objects = objects;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Log.d("djjs", position + "");
        return getCustomView(position, convertView, parent, false);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent, boolean close) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_item, parent, false);
        TextView label = (TextView) row.findViewById(R.id.spItem);
        label.setText(objects.get(position).getName());

        if(close){
            label.setTextColor(context.getResources().getColor(R.color.color_edit_text));
        }else if (objects.get(position).isSelect()) {//Special style for dropdown header
            label.setTextColor(context.getResources().getColor(R.color.color_edit_text));
        }

        return row;
    }

    public List<ActivitySetting.Model> getListCollection(){
        return objects;
    }


}
