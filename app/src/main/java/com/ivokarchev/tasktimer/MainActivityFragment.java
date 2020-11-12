package com.ivokarchev.tasktimer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.InvalidParameterException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        CursorRecyclerViewAdapter.OnTaskClickListener {
    private static final String TAG = "MainActivityFragment";

    private static final int LOADER_ID = 0;
    private CursorRecyclerViewAdapter mAdapter;
    private Timing mCurrentTiming = null;

    public MainActivityFragment() {
        Log.d(TAG, "MainActivityFragment: starts");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: starts");
        super.onActivityCreated(savedInstanceState);
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        setTimingText(mCurrentTiming);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: starts");
        // Called even if we use the same fragment instance (retain fragment)
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        if (mAdapter == null) {
            // Creating MainActivityFragment for the 1st time, otherwise we are reusing an existing instance
            mAdapter = new CursorRecyclerViewAdapter(null, this);
        }
        RecyclerView recyclerView = view.findViewById(R.id.task_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        Log.d(TAG, "onCreateLoader: starts with id: " + id);
        String[] projection = {TasksContract.Columns._ID, TasksContract.Columns.TASKS_NAME,
                TasksContract.Columns.TASKS_DESCRIPTION, TasksContract.Columns.TASKS_SORTORDER};
        String sortOrder = TasksContract.Columns.TASKS_SORTORDER + ", " + TasksContract.Columns.TASKS_NAME;

        switch (id) {
            case LOADER_ID:
                //noinspection ConstantConditions
                return new CursorLoader(getContext(),
                        TasksContract.CONTENT_URI,
                        projection,
                        null,
                        null,
                        sortOrder);
            default:
                throw new InvalidParameterException(TAG + ".onCreateLoader called with invalid loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "entering onLoadFinished()");
        mAdapter.swipeCursor(cursor);
        int count = cursor.getCount();
        Log.d(TAG, "onLoadFinished: count is " + count);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: called");
        mAdapter.swipeCursor(null);
    }

    @Override
    public void onEditTaskClickListener(@NonNull Task task) {
        CursorRecyclerViewAdapter.OnTaskClickListener listener = (CursorRecyclerViewAdapter.OnTaskClickListener) getActivity();
        if (listener != null) {
            listener.onEditTaskClickListener(task);
        }
    }

    @Override
    public void onDeleteTaskClickListener(@NonNull Task task) {
        CursorRecyclerViewAdapter.OnTaskClickListener listener = (CursorRecyclerViewAdapter.OnTaskClickListener) getActivity();
        if (listener != null) listener.onDeleteTaskClickListener(task);
    }

    @Override
    public void onTaskLongClick(@NonNull Task task) {
        Log.d(TAG, "onTaskLongClick: called");
        if (mCurrentTiming != null) {
            if (task.getId() == mCurrentTiming.getTask().getId()) {
                // Current task was tapped second time, so stop timing
                saveTiming(mCurrentTiming);
                mCurrentTiming = null;
                setTimingText(null);
            } else {
                // a new task is being timed, so stop the old one first
                saveTiming(mCurrentTiming);
                mCurrentTiming = new Timing(task);
                setTimingText(mCurrentTiming);
            }
        } else {
            // No task being timed, so start timing the new task
            mCurrentTiming = new Timing(task);
            setTimingText(mCurrentTiming);
        }
    }

    private void saveTiming(@NonNull Timing currentTiming) {
        Log.d(TAG, "saveTiming: entering");

        // If we have an open timing, set the duration and save
        currentTiming.setDuration();
        //noinspection ConstantConditions
        ContentResolver contentResolver = getActivity().getContentResolver();

        ContentValues values = new ContentValues();
        values.put(TimingsContract.Columns.TIMINGS_TASK_ID, currentTiming.getTask().getId());
        values.put(TimingsContract.Columns.TIMINGS_DURATION, currentTiming.getDuration());
        values.put(TimingsContract.Columns.TIMINGS_START_TIME, currentTiming.getStartTime());

        // update table in database
        contentResolver.insert(TimingsContract.CONTENT_URI, values);
        Log.d(TAG, "saveTiming: exiting");
    }


    private void setTimingText(Timing timing) {
        //noinspection ConstantConditions
        TextView taskName = getActivity().findViewById(R.id.current_task);

        if (timing != null) {
            taskName.setText(getString(R.string.current_timing_text, timing.getTask().getName()));
        } else {
            taskName.setText(getString(R.string.no_task_message));
        }
    }
}