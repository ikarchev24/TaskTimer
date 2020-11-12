package com.ivokarchev.tasktimer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

/**
 * Provider for the TaskTimer app. This is the only class that knows about {@link AppDatabase}
 */

public class AppProvider extends ContentProvider {
    private static final String TAG = "AppProvider";

    private AppDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final String CONTENT_AUTHORITY = "com.ivokarchev.tasktimer.provider";
    public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final int TASKS = 100;
    private static final int TASKS_ID = 101;

    private static final int TIMINGS = 200;
    private static final int TIMINGS_ID = 201;


    private static final int TASK_TIMINGS = 300;
    private static final int TASK_TIMINGS_ID = 301;


    private static final int TASK_DURATIONS = 400;
    private static final int TASK_DURATIONS_ID = 401;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //  eg. content://com.ivokarchev.tasktimer.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS);
        // e.g. content://com.ivokarchev.tasktimer.provider/Tasks/8
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME + "/#", TASKS_ID);

        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS);
        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME + "/#", TIMINGS_ID);
//
        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATIONS);
        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME + "/#", TASK_DURATIONS_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query: called with URI " + uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "query: match is " + match);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (match) {
            case TASKS:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                break;

            case TASKS_ID:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                long taskId = TasksContract.getTaskId(uri);
                queryBuilder.appendWhere(TasksContract.Columns._ID + " = " + taskId);
                break;

            case TIMINGS:
                queryBuilder.setTables(TimingsContract.TABLE_NAME);
                break;

            case TIMINGS_ID:
                queryBuilder.setTables(TimingsContract.TABLE_NAME);
                long timingId = TimingsContract.getTimingId(uri);
                queryBuilder.appendWhere(TimingsContract.Columns._ID + " = " + timingId);
                break;

            case TASK_DURATIONS:
                queryBuilder.setTables(DurationsContract.TABLE_NAME);
                break;

            case TASK_DURATIONS_ID:
                queryBuilder.setTables(DurationsContract.TABLE_NAME);
                long durationId = DurationsContract.getDurationId(uri);
                queryBuilder.appendWhere(DurationsContract.Columns._ID + " = " + durationId);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.d(TAG, "insert called with " + uri);
        int match = sUriMatcher.match(uri);
        final SQLiteDatabase db;
        long recordId;
        Uri returnUri;

        Log.d(TAG, "match is " + match);
        switch (match) {
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                recordId = db.insert(TasksContract.TABLE_NAME, null, values);
                if (recordId > 0) {
                    Log.d(TAG, "insert: successfully inserted record into TASKS with id " + recordId);
                    //noinspection ConstantConditions
                    getContext().getContentResolver().notifyChange(uri, null);
                    returnUri = TasksContract.buildTaskUri(recordId);
                } else {
                    throw new SQLException("Unable to insert a record into DB");
                }
                break;
            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                recordId = db.insert(TimingsContract.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    Log.d(TAG, "insert: successfully inserted record into TIMINGS with id " + recordId);
                    returnUri = TimingsContract.buildTimingUri(recordId);
                } else {
                    throw new SQLException("Unable to insert a record into DB");
                }
                break;
            default:
                throw new IllegalStateException("Uri did not match " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete: called with uri = " + uri);
        int match = sUriMatcher.match(uri);

        final SQLiteDatabase db;
        int count;
        String selectionCriteria;

        switch (match) {
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs);
                break;

            case TASKS_ID:
                long taskId = TasksContract.getTaskId(uri);
                db = mOpenHelper.getWritableDatabase();
                selectionCriteria = TasksContract.Columns._ID + " = " + taskId;
                if ((selection != null) && (selection.length() > 0)) {
                    selectionCriteria += " AND " + selection;
                }
                count = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs);
                break;

            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                count = db.delete(TimingsContract.TABLE_NAME, selection, selectionArgs);
                break;

            case TIMINGS_ID:
                long timingsId = TimingsContract.getTimingId(uri);
                db = mOpenHelper.getWritableDatabase();
                selectionCriteria = TimingsContract.Columns._ID + " = " + timingsId;
                if ((selection != null) && (selection.length() > 0)) {
                    selectionCriteria += " AND " + selection;
                }
                count = db.delete(TimingsContract.TABLE_NAME, selectionCriteria, selectionArgs);
                break;


            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (count > 0) {
            // something was deleted
            Log.d(TAG, "delete: Setting notifyChange with " + uri);
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            Log.d(TAG, "delete: nothing deleted");
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update: called with uri =  " + uri);
        int match = sUriMatcher.match(uri);
        final SQLiteDatabase db;
        int count;
        String selectionCriteria;

        switch (match) {
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs);
                break;

            case TASKS_ID:
                long taskId = TasksContract.getTaskId(uri);
                db = mOpenHelper.getWritableDatabase();
                selectionCriteria = TasksContract.Columns._ID + " = " + taskId;
                if ((selection != null) && (selection.length() > 0)) {
                    selectionCriteria += " AND " + selection;
                }
                count = db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs);
                break;

            case TIMINGS:
                db = mOpenHelper.getWritableDatabase();
                count = db.update(TimingsContract.TABLE_NAME, values, selection, selectionArgs);
                break;

            case TIMINGS_ID:
                long timingsId = TimingsContract.getTimingId(uri);
                db = mOpenHelper.getWritableDatabase();
                selectionCriteria = TimingsContract.Columns._ID + " = " + timingsId;
                if ((selection != null) && (selection.length() > 0)) {
                    selectionCriteria += " AND " + selection;
                }
                count = db.update(TimingsContract.TABLE_NAME, values, selectionCriteria, selectionArgs);
                break;


            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (count > 0) {
            // something was deleted
            Log.d(TAG, "update: Setting notifyChange with " + uri);
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            Log.d(TAG, "update: nothing deleted");
        }

        return count;
    }
}
