package com.ivokarchev.tasktimer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DurationsRVAdapter extends RecyclerView.Adapter<DurationsRVAdapter.ViewHolder> {
    private static final String TAG = "DurationsRVAdapter";
    private Cursor mCursor;
    private final DateFormat mDateFormat;

    public DurationsRVAdapter(Context context, Cursor cursor) {
        mCursor = cursor;
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
    }

    @NonNull
    @Override
    public DurationsRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_durations_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DurationsRVAdapter.ViewHolder holder, int position) {
        if (mCursor == null || mCursor.getCount() == 0) {
            holder.name.setText(R.string.empty_text_duration);
            if(holder.description != null) {
                // when in landscape mode
                holder.description.setVisibility(View.GONE);
            }
            holder.startDate.setVisibility(View.GONE);
            holder.duration.setVisibility(View.GONE);
        } else {
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("Couldn't move to position: " + position);
            } else {
                String name = mCursor.getString(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_NAME));
                String description = mCursor.getString(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_DESCRIPTION));
                long totalDuration = mCursor.getLong(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_DURATION));
                long   startTime = mCursor.getLong(mCursor.getColumnIndex(DurationsContract.Columns.DURATIONS_START_TIME));

                holder.name.setText(name);
                if (holder.description != null) {
                    // when in landscape mode
                    holder.description.setText(description);
                }

                String userDate = mDateFormat.format(startTime * 1000); // The db stores seconds, we need milliseconds
                holder.startDate.setText(userDate);
                holder.duration.setText(formatDuration(totalDuration));

                if(holder.description != null) {
                    // when in landscape mode
                    holder.description.setVisibility(View.VISIBLE);
                }
                holder.startDate.setVisibility(View.VISIBLE);
                holder.duration.setVisibility(View.VISIBLE);

            }
        }
    }

    @Override
    public int getItemCount() {
        return (((mCursor == null)) || (mCursor.getCount() == 0) ? 1 : mCursor.getCount());
    }

    private String formatDuration(long duration){
        // duration is in seconds, convert to hours:min:seconds
        // (allowing for > 24h - so we can't use a time data type)
        long hours = duration / 3600;
        Log.d(TAG, "formatDuration: hours: " + hours);
        long remainder = duration - (hours * 3600);
        Log.d(TAG, "formatDuration: remainder: " + remainder);
        long minutes = remainder / 60;
        Log.d(TAG, "formatDuration: minutes: " + minutes);
        long seconds = remainder - (minutes * 60);
        Log.d(TAG, "formatDuration: seconds: " + seconds);
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    Cursor swipeCursor(Cursor newCursor) {
        Log.d(TAG, "swipeCursor: called");
        if (newCursor == mCursor) {
            return null;
        }

        int numItems = getItemCount();

        final Cursor oldCursor = mCursor;
        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged(); // notify the observers about the new cursor
        } else {
            notifyItemRangeRemoved(0, numItems); // notify the observers about the lack of data set and USE THE OLD COUNT
        }
        return oldCursor;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView description;
        private TextView startDate;
        private TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.td_name);
            description = itemView.findViewById(R.id.td_description);
            startDate = itemView.findViewById(R.id.td_start);
            duration = itemView.findViewById(R.id.td_duration);
        }
    }
}
