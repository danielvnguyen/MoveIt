package com.example.moveit.model;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeSetListener =  (TimePickerDialog.OnTimeSetListener) getActivity();
        return new TimePickerDialog(getActivity(), timeSetListener, hour, minute, false);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {}
}