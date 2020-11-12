package com.ivokarchev.tasktimer;

import android.app.DatePickerDialog;
import android.content.Context;

/*
   Replacing tryNotifyDateSet() with nothing -
   this is a workaround for Android 4.x bug https://android-review.googlesource.com/#/c/61270/A
 */

public class UnbuggyDatePickerDialog extends DatePickerDialog {
    UnbuggyDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    @Override
    protected void onStop() {
        // do nothing - do NOT call super method.
    }

}
