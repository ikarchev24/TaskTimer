package com.ivokarchev.tasktimer.debug;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.ivokarchev.tasktimer.TasksContract;
import com.ivokarchev.tasktimer.TimingsContract;

import java.util.GregorianCalendar;

public class TestData {
    public static void generateTestData(ContentResolver contentResolver) {
        final int SECS_IN_DAY = 86400;
        final int MAX_DURATION = SECS_IN_DAY / 6;
        final int LOWER_BOUND = 30;
        final int UPPER_BOUND = 60;

        // get a list of task ID's from database.
        String[] projection = {TasksContract.Columns._ID};

        Uri uri = TasksContract.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if((cursor != null) && cursor.moveToNext()){
            do {
                long taskId = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns._ID));
                // generate between 100 and 500 random timings for this task
                int loopCount = LOWER_BOUND + getRandomInt(UPPER_BOUND - LOWER_BOUND);


                for(int i = 0; i < loopCount; i++){
                    long randomDate = randomDateTime();

                    // generate a random duration between 0 and 4 hours
                    long duration = getRandomInt(MAX_DURATION);

                    // create a new TestTiming object
                    TestTiming testTiming = new TestTiming(taskId, randomDate, duration);
                    saveCurrentTiming(contentResolver, testTiming);
                }
                // save the TestTiming record to the DB
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private static int getRandomInt(int max) {
        return (int) Math.round(Math.random() * max);
    }

    private static long randomDateTime() {
        // Set the range of years - change as necessary
        final int startYear = 2020;
        final int endYear = 2021;
        int sec = getRandomInt(59);
        int min = getRandomInt(59);
        int hour = getRandomInt(23);
        int month = getRandomInt(11);
        int year = startYear + getRandomInt(endYear - startYear);
        final GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, 1);
        int day = 1 + getRandomInt(gregorianCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) - 1);

        gregorianCalendar.set(year, month, day, hour, min, sec);
        return gregorianCalendar.getTimeInMillis();
    }

     private static void saveCurrentTiming(ContentResolver contentResolver, TestTiming currentTiming){
        // save the timing record
         ContentValues contentValues = new ContentValues();
         contentValues.put(TimingsContract.Columns.TIMINGS_TASK_ID, currentTiming.taskId);
         contentValues.put(TimingsContract.Columns.TIMINGS_START_TIME, currentTiming.startTime);
         contentValues.put(TimingsContract.Columns.TIMINGS_DURATION, currentTiming.duration);

         // update database
         contentResolver.insert(TimingsContract.CONTENT_URI, contentValues);
     }

}
