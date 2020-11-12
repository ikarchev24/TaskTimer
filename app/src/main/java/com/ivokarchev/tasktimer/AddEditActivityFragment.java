package com.ivokarchev.tasktimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddEditActivityFragment extends Fragment {
    private static final String TAG = "AddEditActivityFragment";

    public enum FragmentEditMode {EDIT, ADD}

    private OnTaskSaveListener mListener;

    interface OnTaskSaveListener {
        void onSaveClicked(FragmentEditMode mode);
    }

    private FragmentEditMode mMode;
    private EditText mTaskName;
    private EditText mTaskDesc;
    private EditText mTaskSortOrder;

    public AddEditActivityFragment() {
        Log.d(TAG, "AddEditActivityFragment: constructor called");
        // Required empty public constructor
    }

    boolean canClose() {

        Bundle args = getArguments();

        if (args != null) {
            Task task = (Task) args.getSerializable(Task.class.getSimpleName());
            if (task != null) {
                // When editing a task...
                if (!(task.getName().equals(mTaskName.getText().toString()))) {
                    return false;
                }
                if (!(task.getDescription()).equals(mTaskDesc.getText().toString())) {
                    return false;
                }
                if (mTaskSortOrder.getText().toString().isEmpty()) {
                    return true;
                } else {
                    return task.getSortOrder() == Integer.parseInt((mTaskSortOrder.getText().toString()));
                }
            } else {
                // When adding a new task
                return mTaskName.getText().toString().trim().length() == 0;
            }
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: starts");
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity != null) {
            if (!(activity instanceof OnTaskSaveListener)) {
                throw new ClassCastException("Activity must implement OnTaskSaveListener");
            } else {
                mListener = (OnTaskSaveListener) activity;
            }
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: starts");
        super.onDetach();
        mListener = null;
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: starts");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);
        mTaskName = view.findViewById(R.id.addedit_name);
        mTaskDesc = view.findViewById(R.id.addedit_desc);
        mTaskSortOrder = view.findViewById(R.id.addedit_sortorder);
        Button taskSaveBtn = view.findViewById(R.id.addedit_save);

        // Get arguments from the activity
        Bundle arguments = getArguments();

        final Task task;
        if (arguments != null) {
            Log.d(TAG, "onCreateView: retrieving task details.");
            task = (Task) arguments.getSerializable(Task.class.getSimpleName());
            if (task != null) {
                Log.d(TAG, "onCreateView: Task details found, editing...");
                mTaskName.setText(task.getName());
                mTaskDesc.setText(task.getDescription());
                mTaskSortOrder.setText(Integer.toString(task.getSortOrder()));
                mMode = FragmentEditMode.EDIT;
            } else {
                // No task, creating new one and not editing an existing one
                mMode = FragmentEditMode.ADD;
            }
        } else {
            task = null;
            Log.d(TAG, "onCreateView: No arguments, adding new record");
            mMode = FragmentEditMode.ADD;
        }

        Activity activity = getActivity();
        final ContentResolver contentResolver = activity != null ? activity.getContentResolver() : null;
        taskSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the DB if at least one field has changed
                int sortOrder;
                if (mTaskSortOrder.length() > 0) {
                    sortOrder = Integer.parseInt(mTaskSortOrder.getText().toString());
                } else {
                    sortOrder = 0;
                }
                ContentValues contentValues = new ContentValues();
                switch (mMode) {
                    case EDIT:
                        if (task != null) {
                            if (!mTaskName.getText().toString().equals(task.getName())) {
                                contentValues.put(TasksContract.Columns.TASKS_NAME, mTaskName.getText().toString());
                            }
                            if (!mTaskDesc.getText().toString().equals(task.getDescription())) {
                                contentValues.put(TasksContract.Columns.TASKS_DESCRIPTION, mTaskDesc.getText().toString());
                            }
                            if (!(mTaskSortOrder.getText().toString().isEmpty()) && sortOrder != task.getSortOrder()) {
                                contentValues.put(TasksContract.Columns.TASKS_SORTORDER, sortOrder);
                            }
                            if (contentValues.size() != 0) {
                                Log.d(TAG, "onTaskSave: updating task...");
                                if (contentResolver != null) {
                                    contentResolver.update(TasksContract.buildTaskUri(task.getId()), contentValues, null, null);
                                }
                            }
                            break;
                        }
                    case ADD:
                        if (mTaskName.getText().toString().trim().length() > 0) {
                            Log.d(TAG, "onTaskSave: adding new task");
                            contentValues.put(TasksContract.Columns.TASKS_NAME, mTaskName.getText().toString());
                            contentValues.put(TasksContract.Columns.TASKS_DESCRIPTION, mTaskDesc.getText().toString());
                            contentValues.put(TasksContract.Columns.TASKS_SORTORDER, sortOrder);
                            if (contentResolver != null) {
                                contentResolver.insert(TasksContract.CONTENT_URI, contentValues);
                            }
                        }
                        break;
                }
                if (mListener != null) {
                    mListener.onSaveClicked(mMode);
                }
                Log.d(TAG, "onSave: Done editing");
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
