package com.ivokarchev.tasktimer;

import android.util.Log;

import java.io.Serializable;
import java.util.Date;

/**
 * Simple timing object.
 * Sets its start time when created, and calculates how long since creation,
 * when setDuration is called.
 */

class Timing implements Serializable {
    private static final long serialVersionUID = 20200403L;
    private static final String TAG = "Timing";

    private long m_Id;
    private long mStartTime;
    private long mDuration;
    private Task mTask;

    Timing(Task task) {
        mTask = task;
        // Initialize the start time to now and the duration to 0 for new objects/
        Date currentTime = new Date();
        mStartTime = currentTime.getTime() / 1000; // We are only tracking whole seconds, not milliseconds
        mDuration = 0;
    }

    long getId() {
        return m_Id;
    }

    void setId(long id) {
        m_Id = id;
    }

    long getStartTime() {
        return mStartTime;
    }

    void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    long getDuration() {
        return mDuration;
    }

    void setDuration() {
        // Calculate the duration from mStartTime to dateTime.
        Date currentTime = new Date();
        mDuration = (currentTime.getTime() / 1000) - mStartTime; // working in seconds, not milliseconds
        Log.d(TAG, "setDuration: taskId: " + mTask.getId() + " - Start Time: " + mStartTime / 1000 + " | Duration: " + mDuration);
    }

    Task getTask() {
        return mTask;
    }

    void setTask(Task task) {
        mTask = task;
    }
}
