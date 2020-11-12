package com.ivokarchev.tasktimer;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DurationsReportActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        DatePickerDialog.OnDateSetListener,
        AppDialog.DialogEvents,
        View.OnClickListener {
    private static final String TAG = "DurationsActivity";

    private static final int LOADER_ID = 1;

    // Dialog Constants
    public static final int DIALOG_FILTER = 1;
    public static final int DIALOG_DELETE = 2;

    // applyFilter() constants
    private static final String SELECTION_PARAM = "SELECTION";
    private static final String SELECTION_ARGS_PARAM = "SELECTION_ARGS";
    private static final String SORT_ORDER_PARAM = "SORT_ORDER";

    public static final String DELETION_DATE = "DELETION_DATE";

    public static final String CURRENT_DATE = "CURRENT_DATE";
    public static final String DISPLAY_WEEK = "DISPLAY_WEEK";

    //retain state e.g when changing sort order / custom selection
    private Bundle mArgs = new Bundle();
    private boolean mDisplayWeek = true;

    private DurationsRVAdapter mAdapter;

    private final GregorianCalendar mCalendar = new GregorianCalendar();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_durations);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RecyclerView rv = findViewById(R.id.td_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set the listener for the buttons to sort the report
        TextView taskName = findViewById(R.id.tv_name_heading);
        taskName.setOnClickListener(this);

        TextView taskDesc = findViewById(R.id.tv_text_description);
        if (taskDesc != null) {
            taskDesc.setOnClickListener(this);
        }

        TextView taskDate = findViewById(R.id.tv_start_heading);
        taskDate.setOnClickListener(this);

        TextView taskDuration = findViewById(R.id.tv_duration_heading);
        taskDuration.setOnClickListener(this);


        Log.d(TAG, "onCreate: timeInMillis " + mCalendar.getTimeInMillis());

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: current callendar date");
            long timeInMillis = savedInstanceState.getLong(CURRENT_DATE, 0);
            Log.d(TAG, "onCreate: timeInMillis " + timeInMillis);
            if (timeInMillis != 0) {
                // Make sure the time part is cleared, because we filter the database by seconds
                // and we don't want delete a record from the current date as we notify users we only delete records prior to the current date
                mCalendar.setTimeInMillis(timeInMillis);
                mCalendar.set(Calendar.HOUR_OF_DAY, 0);
                mCalendar.set(Calendar.MINUTE, 0);
                mCalendar.set(Calendar.SECOND, 0);
            }
            mDisplayWeek = savedInstanceState.getBoolean(DISPLAY_WEEK, true);
        }

        applyFilter();

        // Init LoaderManager
        LoaderManager.getInstance(this).
                initLoader(LOADER_ID, mArgs, this);

        // Init duration recyclerview adapter, an empty adapter until data is loaded
        if (mAdapter == null) {
            Log.d(TAG, "onCreate: mAdapter INITIATED");
            mAdapter = new DurationsRVAdapter(this, null);
        }
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: called");

        int id = v.getId();
        switch (id) {
            case R.id.tv_name_heading:
                mArgs.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_NAME);
                break;
            case R.id.tv_text_description:
                mArgs.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_DESCRIPTION);
                break;
            case R.id.tv_start_heading:
                mArgs.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_START_DATE);
                break;
            case R.id.tv_duration_heading:
                mArgs.putString(SORT_ORDER_PARAM, DurationsContract.Columns.DURATIONS_DURATION);
                break;
        }
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, mArgs, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.rm_filter_period:
                mDisplayWeek = !mDisplayWeek;
                applyFilter();
                invalidateOptionsMenu(); // force call to onPrepareOptionsMenu to redraw the menu ( change our icon  from mDisplayWeek toggler )
                LoaderManager.getInstance(this).restartLoader(LOADER_ID, mArgs, this);
                return true;
            case R.id.rm_filter_date:
                showDatePickerDialog(getString(R.string.dpd_dialog_filter), DIALOG_FILTER); /// The actual filtering is done in onDataSet();
                return true;
            case R.id.rm_delete:
                showDatePickerDialog(getString(R.string.dpd_dialog_delete), DIALOG_DELETE); // The actual deleting is done in onDateSet();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.rm_filter_period);
        if (item != null) {
            // switch icon and title to represent 7 days o 1 day as appropriate to the future function of the menu item
            if (mDisplayWeek) {
                item.setIcon(R.drawable.ic_filter_1_black_24dp);
                item.setTitle(R.string.rm_title_filter_day);
            } else {
                item.setIcon(R.drawable.ic_filter_7_black_24dp);
                item.setTitle(R.string.rm_title_filter_week);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(CURRENT_DATE, mCalendar.getTimeInMillis());
        outState.putBoolean(DISPLAY_WEEK, mDisplayWeek);
        super.onSaveInstanceState(outState);
    }

    private void applyFilter() {
        Log.d(TAG, "applyFilter: called");
        if (mDisplayWeek) {
            // show records for the entire week
            Date currentCalendarDate = mCalendar.getTime(); // store the time, so we can put it back
            Log.d(TAG, "applyFilter: currentCalendarDate is: " + currentCalendarDate);
            // we have a date, so find out which day of week it is
            int dayOfWeek = mCalendar.get(GregorianCalendar.DAY_OF_WEEK);
            int weekStart = mCalendar.getFirstDayOfWeek();
            Log.d(TAG, "applyFilter: dayOfWeek is: " + dayOfWeek);
            Log.d(TAG, "applyFilter: first day of calendar week is: " + weekStart);
            Log.d(TAG, "applyFilter: date is: " + mCalendar.getTime());

            // calculate week start and end dates
            mCalendar.set(GregorianCalendar.DAY_OF_WEEK, weekStart);
            String startDate = String.format(Locale.US, "%04d-%02d-%02d",
                    mCalendar.get(GregorianCalendar.YEAR),
                    mCalendar.get(GregorianCalendar.MONTH) + 1,
                    mCalendar.get(GregorianCalendar.DAY_OF_MONTH));

            mCalendar.add(GregorianCalendar.DATE, 6); // move forward 6 days to get the last day of the week

            String endDate = String.format(Locale.US, "%04d-%02d-%02d",
                    mCalendar.get(GregorianCalendar.YEAR),
                    mCalendar.get(GregorianCalendar.MONTH) + 1,
                    mCalendar.get(GregorianCalendar.DAY_OF_MONTH));
            String[] selectionArgs = new String[]{startDate, endDate};
            // put the calendar back to where it was before we started jumping back and forth
            mCalendar.setTime(currentCalendarDate);
            Log.d(TAG, "applyFilter: In applyFilter(7), Start date is: " + startDate + ", End date is: " + endDate);
            mArgs.putString(SELECTION_PARAM, "StartDate Between ? AND ?");
            mArgs.putStringArray(SELECTION_ARGS_PARAM, selectionArgs);
        } else {
            // re-query for the current day
            String startDate = String.format(Locale.US, "%04d-%02d-%02d",
                    mCalendar.get(GregorianCalendar.YEAR),
                    mCalendar.get(GregorianCalendar.MONTH) + 1,
                    mCalendar.get(GregorianCalendar.DAY_OF_MONTH));
            String[] selectionArgs = new String[]{startDate};
            Log.d(TAG, "applyFilter: In applyFilter(1), Start date is + " + startDate);
            mArgs.putString(SELECTION_PARAM, "StartDate = ?");
            mArgs.putStringArray(SELECTION_ARGS_PARAM, selectionArgs);

        }
    }

    private void showDatePickerDialog(String title, int dialogId) {
        Log.d(TAG, "showDatePickerDialog: called");
        DialogFragment dialogFragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt(DatePickerFragment.DATE_PICKER_ID, dialogId);
        args.putString(DatePickerFragment.DATE_PICKER_TITLE, title);
        args.putSerializable(DatePickerFragment.DATE_PICKER_DATE, mCalendar.getTime());

        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
        Log.d(TAG, "showDatePickerDialog: exiting");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d(TAG, "onDateSet: called");
        // Check the id, so we know what to do with the result
        mCalendar.set(year, month, dayOfMonth, 0, 0, 0);
        int dialogId = (int) view.getTag();
        switch (dialogId) {
            case DIALOG_FILTER:
                applyFilter();
                LoaderManager.getInstance(this).restartLoader(LOADER_ID, mArgs, this);
                break;
            case DIALOG_DELETE:
                applyFilter();
                LoaderManager.getInstance(this).restartLoader(LOADER_ID, mArgs, this);
                String fromDate = DateFormat.getDateFormat(this)
                        .format(mCalendar.getTimeInMillis());
                AppDialog dialog = new AppDialog();
                Bundle args = new Bundle();
                args.putInt(AppDialog.DIALOG_ID, 1); // we only have 1 dialog in this activity
                args.putString(AppDialog.DIALOG_MESSAGE, String.format(getString(R.string.dur_report_dialog_message), fromDate));
                args.putLong(DELETION_DATE, mCalendar.getTimeInMillis());
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), null);
                break;
            default:
                throw new IllegalArgumentException("Invalid mode when receiving DatePickerDialog result");
        }
    }

    private int deleteRecords(long timeInMillis) {
        Log.d(TAG, "deleteRecords: starts");

        long longDate = timeInMillis / 1000; // We need time in seconds, not ms
        String[] selectionArgs = new String[]{Long.toString(longDate)};
        String selection = TimingsContract.Columns.TIMINGS_START_TIME + " < ?";
        Log.d(TAG, "deleting records prior to " + longDate);
        return getContentResolver().delete(TimingsContract.CONTENT_URI, selection, selectionArgs);
    }

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveDialogResult: starts");
        // clear all records from Timings table prior to the date selected.
        long deleteDate = args.getLong(DELETION_DATE);
        int deletedRecords = deleteRecords(deleteDate);
        Log.d(TAG, "deleted records: " + deletedRecords);
        if (deletedRecords > 0) {
            // re-query, in case we've deleted records that currently being shown
            LoaderManager.getInstance(this).restartLoader(LOADER_ID, mArgs, this);
        }

    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {

    }

    @Override
    public void onDialogCancelled(int dialogId) {

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                BaseColumns._ID,
                DurationsContract.Columns.DURATIONS_NAME,
                DurationsContract.Columns.DURATIONS_DESCRIPTION,
                DurationsContract.Columns.DURATIONS_START_DATE,
                DurationsContract.Columns.DURATIONS_START_TIME,
                DurationsContract.Columns.DURATIONS_DURATION
        };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        if (args != null) {
            selection = args.getString(SELECTION_PARAM);
            selectionArgs = args.getStringArray(SELECTION_ARGS_PARAM);
            sortOrder = args.getString(SORT_ORDER_PARAM);
        }

        //noinspection SwitchStatementWithTooFewBranches
        switch (id) {
            case LOADER_ID:
                return new CursorLoader(
                        this, DurationsContract.CONTENT_URI, projection, selection, selectionArgs, sortOrder
                );
            default:
                throw new InvalidParameterException("Loader with id: " + id + " does not match any switch case statement");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: called");
        mAdapter.swipeCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        mAdapter.swipeCursor(null);
    }
}
