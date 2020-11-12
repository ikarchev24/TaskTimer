package com.ivokarchev.tasktimer;

public class Duration {

    private long mDurationTaskId;
    private String mDurationTaskName;
    private String mDurationTaskDesc;
    private long mDurationStartTime;
    private String mDurationStartDate;
    private long mTimingsDuration;

    public Duration(long durationTaskId, String durationTaskName, String durationTaskDesc, long durationStartTime, String durationStartDate, long timingsDuration) {
        mDurationTaskId = durationTaskId;
        mDurationTaskName = durationTaskName;
        mDurationTaskDesc = durationTaskDesc;
        mDurationStartTime = durationStartTime;
        mDurationStartDate = durationStartDate;
        mTimingsDuration = timingsDuration;
    }

    public long getDurationTaskId() {
        return mDurationTaskId;
    }

    public String getDurationTaskName() {
        return mDurationTaskName;
    }

    public String getDurationTaskDesc() {
        return mDurationTaskDesc;
    }

    public long getDurationStartTime() {
        return mDurationStartTime;
    }

    public String getDurationStartDate() {
        return mDurationStartDate;
    }

    public long getTimingsDuration() {
        return mTimingsDuration;
    }

    @Override
    public String toString() {
        return "Duration{" +
                "mDurationTaskId=" + mDurationTaskId +
                ", mDurationTaskName='" + mDurationTaskName + '\'' +
                ", mDurationTaskDesc='" + mDurationTaskDesc + '\'' +
                ", mDurationStartTime=" + mDurationStartTime +
                ", mDurationStartDate='" + mDurationStartDate + '\'' +
                ", mTimingsDuration=" + mTimingsDuration +
                '}';
    }
}
