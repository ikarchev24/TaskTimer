package com.ivokarchev.tasktimer;

import java.io.Serializable;

class Task implements Serializable {
    private static final long serialVersionUID = 20202802L;

    private long m_Id;
    private final String mName;
    private final String mDescription;
    private final int mSortOrder;

    public Task(String name, String description, int sortOrder, long id) {
        m_Id = id;
        mName = name;
        mDescription = description;
        mSortOrder = sortOrder;
    }

    void setId(long id) {
        this.m_Id = id;
    }

    long getId() {
        return m_Id;
    }

    String getName() {
        return mName;
    }

    String getDescription() {
        return mDescription;
    }

    int getSortOrder() {
        return mSortOrder;
    }

    @Override
    public String toString() {
        return "Task{" +
                "m_Id=" + m_Id +
                ", mName='" + mName + '\'' +
                ", mDescrption='" + mDescription + '\'' +
                ", mSortOrder=" + mSortOrder +
                '}';
    }
}
