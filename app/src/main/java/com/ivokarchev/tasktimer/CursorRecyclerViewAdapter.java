package com.ivokarchev.tasktimer;

import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.TaskViewHolder> {
    private static final String TAG = "CursorRecyclerAdapt";

    interface OnTaskClickListener {
        void onEditTaskClickListener(@NonNull Task task);
        void onDeleteTaskClickListener(@NonNull Task task);
        void onTaskLongClick(@NonNull Task task);
    }


    private Cursor mCursor;
    private OnTaskClickListener mListener;

    public CursorRecyclerViewAdapter(Cursor cursor, OnTaskClickListener listener) {
        Log.d(TAG, "CursorRecyclerViewAdapter: Constructor called");
        mCursor = cursor;
        mListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.d(TAG, "onCreateViewHolder: new View requested");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_task_item, viewGroup, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder taskViewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: starts");

        if((mCursor == null) || (mCursor.getCount() == 0)){
            Log.d(TAG, "onBindViewHolder: providing instructions");
            taskViewHolder.mTaskName.setText(R.string.instructions);
            taskViewHolder.mTaskDesc.setText(R.string.instructions_body);

            taskViewHolder.editButton.setVisibility(View.GONE);
            taskViewHolder.deleteButton.setVisibility(View.GONE);
        } else {
            if(!mCursor.moveToPosition(i)){
                throw new IllegalStateException("Couldn't move cursor to position " + i);
            } else {
                final Task task = new Task(
                        mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASKS_NAME)),
                        mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASKS_DESCRIPTION)),
                        mCursor.getInt(mCursor.getColumnIndex(TasksContract.Columns.TASKS_SORTORDER)),
                        mCursor.getLong(mCursor.getColumnIndex(TasksContract.Columns._ID))
                );

                taskViewHolder.mTaskName.setText(task.getName());
                taskViewHolder.mTaskDesc.setText(task.getDescription());

                taskViewHolder.editButton.setVisibility(View.VISIBLE);
                taskViewHolder.deleteButton.setVisibility(View.VISIBLE);

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int id = v.getId();
                        switch(id){
                            case R.id.tli_edit:
                                if(mListener != null) {
                                    mListener.onEditTaskClickListener(task);
                                }
                                break;
                            case R.id.tli_delete:
                                if(mListener != null) {
                                    mListener.onDeleteTaskClickListener(task);
                                }
                                break;
                            default:
                                Log.d(TAG, "onClick: called with unhandled view ID");
                        }
                    }
                };

                taskViewHolder.editButton.setOnClickListener(listener);
                taskViewHolder.deleteButton.setOnClickListener(listener);
                taskViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Log.d(TAG, "onLongClick: starts");
                        if(mListener != null){
                            mListener.onTaskLongClick(task);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }



    @Override
    public int getItemCount() {
        return ((mCursor == null ) || (mCursor.getCount() == 0) ? 1 : mCursor.getCount());
    }

    Cursor swipeCursor(Cursor newCursor){
        Log.d(TAG, "swipeCursor: called");
        if(newCursor == mCursor){
            return null;
        }

        int numItems = getItemCount();

        final Cursor oldCursor = mCursor;
        mCursor = newCursor;

        if(newCursor != null){
            notifyDataSetChanged(); // notify the observers about the new cursor
        } else {
            notifyItemRangeRemoved(0, numItems); // notify the observers about the lack of data set
        }
        return oldCursor;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView mTaskName;
        private TextView mTaskDesc;
        private ImageButton editButton;
        private ImageButton deleteButton;
        View itemView;


        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mTaskName = itemView.findViewById(R.id.tli_name);
            this.mTaskDesc = itemView.findViewById(R.id.tli_description);
            this.editButton = itemView.findViewById(R.id.tli_edit);
            this.deleteButton = itemView.findViewById(R.id.tli_delete);
            this.itemView = itemView;
        }
    }
}
