package com.atlas.mars.weatherradar.timepisker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.atlas.mars.weatherradar.R;

import java.lang.reflect.Field;

/**
 * Created by Администратор on 7/29/15.
 */
public class TimePickerColor  {
    Resources system;
    Context context;
    TimePicker time_picker;
    public TimePickerColor(Context context, TimePicker time_picker){

        this.context = context;
        this.time_picker = time_picker;
        set_timepicker_text_colour();
    }
    private void set_timepicker_text_colour(){
        system = Resources.getSystem();
        int hour_numberpicker_id = system.getIdentifier("hour", "id", "android");
        int minute_numberpicker_id = system.getIdentifier("minute", "id", "android");
        int ampm_numberpicker_id = system.getIdentifier("amPm", "id", "android");

        NumberPicker hour_numberpicker = (NumberPicker) time_picker.findViewById(hour_numberpicker_id);
        NumberPicker minute_numberpicker = (NumberPicker) time_picker.findViewById(minute_numberpicker_id);
        NumberPicker ampm_numberpicker = (NumberPicker) time_picker.findViewById(ampm_numberpicker_id);

        set_numberpicker_text_colour(hour_numberpicker);
        set_numberpicker_text_colour(minute_numberpicker);
        set_numberpicker_text_colour(ampm_numberpicker);
    }

    private void set_numberpicker_text_colour(NumberPicker number_picker){
        final int count = number_picker.getChildCount();
        final int color = getResources().getColor(R.color.white);

        for(int i = 0; i < count; i++){
            View child = number_picker.getChildAt(i);

            try{
                Field wheelpaint_field = number_picker.getClass().getDeclaredField("mSelectorWheelPaint");
                wheelpaint_field.setAccessible(true);

                ((Paint)wheelpaint_field.get(number_picker)).setColor(color);
                ((EditText)child).setTextColor(color);
                number_picker.invalidate();
            }
            catch(NoSuchFieldException e){
                //  Log.w("setNumberPickerTextColor", e);
            }
            catch(IllegalAccessException e){
                //Log.w("setNumberPickerTextColor", e);
            }
            catch(IllegalArgumentException e){
                //Log.w("setNumberPickerTextColor", e);
            }
        }
    }
    Resources getResources(){
        return context.getResources();
    }
}
