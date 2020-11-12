package com.ivokarchev.tasktimer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Date;
import java.util.GregorianCalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "DatePickerFragment";

    static final String DATE_PICKER_ID = "ID";
    static final String DATE_PICKER_TITLE = "TITLE";
    static final String DATE_PICKER_DATE = "DATE";

    private int mDialogId = 0;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Activities using this dialog must implement its callbacks
        if (!(context instanceof DatePickerDialog.OnDateSetListener)) {
            throw new ClassCastException(context.toString() + " must implement DatePickerDialog.OnDateSetListener interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current date initially
        final GregorianCalendar calendar = new GregorianCalendar();
        String title = null;
        Bundle arguments = getArguments();
        if (arguments != null) {
            mDialogId = arguments.getInt(DATE_PICKER_ID);
            title = arguments.getString(DATE_PICKER_TITLE);

            // If we passed a date, use it, otherwise leave calendar set to the current date.
            Date givenDate = (Date) arguments.getSerializable(DATE_PICKER_DATE);
            if (givenDate != null) {
                calendar.setTime(givenDate);
                Log.d(TAG, "onCreateDialog: retrieved date = " + givenDate.toString());
            }
        }
        int year = calendar.get(GregorianCalendar.YEAR);
        int month = calendar.get(GregorianCalendar.MONTH);
        int day = calendar.get(GregorianCalendar.DAY_OF_MONTH);

        UnbuggyDatePickerDialog dpd = new UnbuggyDatePickerDialog(getContext(), this, year, month, day);
        if (title != null) {
            dpd.setTitle(title);
        }
        return dpd;
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d(TAG, "onDateSet: called");
        DatePickerDialog.OnDateSetListener listener = (DatePickerDialog.OnDateSetListener) getActivity();
        if (listener != null) {
            // Notify caller of the user-selected values
            view.setTag(mDialogId); // pass the id back in the tag, to save the caller storing their own copy
            listener.onDateSet(view, year, month, dayOfMonth);
        }
        Log.d(TAG, "onDateSet: exiting");
    }

}
