package com.ivokarchev.tasktimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ivokarchev.tasktimer.debug.TestData;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity implements CursorRecyclerViewAdapter.OnTaskClickListener,
        AddEditActivityFragment.OnTaskSaveListener,
        AppDialog.DialogEvents {
    private static final String TAG = "MainActivity";

    // whether or not the activity is in 2-pane mode
    // i.e running in landscape on a tablet
    private boolean mTwoPane = false;
    private static final int DIALOG_ID_DELETE = 1;
    private static final int DIALOG_ID_CANCEL_EDIT = 2;
    private AlertDialog mDialog = null;    // module scope because we need to dismiss it in onStop, e.g when orientation changes to avoid memory leaks.
    private View mAddEditLayout;
    private View mMainFragment;
    private Task mCurrentTaskEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTwoPane = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        Log.d(TAG, "onCreate: twoPane is " + mTwoPane);

        FragmentManager fragmentManager = getSupportFragmentManager();
        // If the AddEditActivity fragment exists, we are editing...
        boolean editing = fragmentManager.findFragmentById(R.id.task_details_container) != null;
        Log.d(TAG, "onCreate: editing is " + editing);

        // We need references to the containers, so we can show or hide them as necessarry
        // No need to cast them, as we're only calling a method that's avaiable for all views
        mAddEditLayout = findViewById(R.id.task_details_container);
        mMainFragment = findViewById(R.id.fragment);

        if (mTwoPane) {
            mMainFragment.setVisibility(View.VISIBLE);
            mAddEditLayout.setVisibility(View.VISIBLE);
        } else if (editing) {
            // hide the left hand fragment, to make room for editing
            mMainFragment.setVisibility(View.GONE);
        } else {
            // Show left hand fragment
            mMainFragment.setVisibility(View.VISIBLE);
            // Hide the editing fragment
            mAddEditLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionsMenu: starts");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(BuildConfig.DEBUG){
            MenuItem generate = menu.findItem(R.id.menumain_generate);
            generate.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                AddEditActivityFragment fragment = (AddEditActivityFragment) getSupportFragmentManager().findFragmentById(R.id.task_details_container);
                if ((fragment == null) || fragment.canClose()) {
                    hideSoftKeyboard();
                    if (mTwoPane) {
                        mAddEditLayout.setVisibility(View.INVISIBLE);
                    } else {
                        mAddEditLayout.setVisibility(View.GONE);
                    }
                    mMainFragment.setVisibility(View.VISIBLE);
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                } else {
                    if (mAddEditLayout.getVisibility() == View.VISIBLE) {
                        AppDialog dialog = new AppDialog();
                        Bundle args = new Bundle();
                        args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_CANCEL_EDIT);
                        args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancel_edit_dialog_message));
                        args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancel_edit_dialog_positive_caption);
                        args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancel_edit_dialog_negative_caption);
                        dialog.setArguments(args);
                        dialog.show(getSupportFragmentManager(), null);
                    }
                }
                break;
            case R.id.menumain_addTask:
                taskEditRequest(null);
                break;
            case R.id.menumain_showDurations:
                Intent intent = new Intent(this, DurationsReportActivity.class);
                startActivity(intent);
                break;
            case R.id.menumain_settings:
                break;
            case R.id.menumain_showAbout:
                showAboutDialog();
                break;
            case R.id.menumain_generate:
                TestData.generateTestData(getContentResolver());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    public void showAboutDialog() {
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
        final TextView tvLink = messageView.findViewById(R.id.about_weblink);
        if (tvLink != null) {
            tvLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://" + tvLink.getText().toString();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "No browser application found", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(messageView);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });
        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(true);
        TextView tvVersion = messageView.findViewById(R.id.about_version);

        tvVersion.setText("v" + com.ivokarchev.tasktimer.BuildConfig.VERSION_NAME);
        mDialog.show();
    }

    @Override
    public void onEditTaskClickListener(@NonNull Task task) {
        taskEditRequest(task);
    }

    @Override
    public void onDeleteTaskClickListener(@NonNull Task task) {
        Log.d(TAG, "onDeleteTaskClickListener: starts");
        if ((mCurrentTaskEdited != null && mCurrentTaskEdited.getId() == task.getId() && (mTwoPane && mAddEditLayout.getVisibility() == View.VISIBLE))) {
            Toast.makeText(this, "You are trying to delete task that is currently being edited...", Toast.LENGTH_SHORT).show();
        } else {
            AppDialog dialog = new AppDialog();
            Bundle args = new Bundle();
            args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_DELETE);
            args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.delete_dialog_message) + task.getName());
            args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption);
            args.putSerializable(Task.class.getSimpleName(), task);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onTaskLongClick(@NonNull Task task) {
        // required to satisfy the interface
    }

    private void taskEditRequest(Task task) {
        Log.d(TAG, "taskEditRequest: starts");
        Log.d(TAG, "taskEditRequest: in 2-pane mode (tablet)");
        mCurrentTaskEdited = task;
        AddEditActivityFragment addEditActivityFragment = new AddEditActivityFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(Task.class.getSimpleName(), task);
        addEditActivityFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.task_details_container, addEditActivityFragment)
                .commit();
        mAddEditLayout.setVisibility(View.VISIBLE);
        if (!mTwoPane) {
            Log.d(TAG, "taskEditRequest: in 1-pane mode (phone)");
            // Hide the left hand fragment and show the right hand frame
            mMainFragment.setVisibility(View.GONE);
            mAddEditLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveClicked(AddEditActivityFragment.FragmentEditMode mode) {
        AddEditActivityFragment fragment = (AddEditActivityFragment) getSupportFragmentManager().findFragmentById(R.id.task_details_container);
        if (fragment != null && fragment.canClose()) {
            // canClose() return true if there are no edit changes  or new task has no name
            if (mode == AddEditActivityFragment.FragmentEditMode.EDIT) {
                hideSoftKeyboard();
                Toast.makeText(this, "No changes have been found. Please, edit the task before you attempt to save it", Toast.LENGTH_SHORT).show();

            } else {
                hideSoftKeyboard();
                Toast.makeText(this, "Task name must not be empty!", Toast.LENGTH_LONG).show();
            }
        } else {
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }

        View addEditLayout = findViewById(R.id.task_details_container);
        View mainFragment = findViewById(R.id.fragment);
        if (fragment != null) {
            if (!mTwoPane && !fragment.canClose()) {
                // We've just removed the editing fragment, so hide the frame
                addEditLayout.setVisibility(View.GONE);
                // and make sure the MainActivityFragment is visible
                mainFragment.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveDialogResult: called");

        Task task = (Task) args.getSerializable(Task.class.getSimpleName());
        if (com.ivokarchev.tasktimer.BuildConfig.DEBUG && task != null && task.getId() == 0)
            throw new AssertionError("Task ID is equal to 0");
        switch (dialogId) {
            case DIALOG_ID_DELETE:
                if (task != null) {
                    getContentResolver().delete(TasksContract.buildTaskUri(task.getId()), null, null);
                }
                break;
            case DIALOG_ID_CANCEL_EDIT:
                break;
        }
    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onNegativeDialogResult: called");
        switch (dialogId) {
            case DIALOG_ID_DELETE:
                // no action required
                break;
            case DIALOG_ID_CANCEL_EDIT:
                hideSoftKeyboard();
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.task_details_container);
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
                if (mTwoPane) {
                    mAddEditLayout.setVisibility(View.INVISIBLE);
                } else {
                    mAddEditLayout.setVisibility(View.GONE);
                }
                mMainFragment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDialogCancelled(int dialogId) {
        Log.d(TAG, "onDialogCancelled: called");
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddEditActivityFragment fragment = (AddEditActivityFragment) fragmentManager.findFragmentById(R.id.task_details_container);
        if ((fragment == null) || fragment.canClose()) {
            if (!mTwoPane) {
                if (mAddEditLayout.getVisibility() == View.VISIBLE) {
                    mAddEditLayout.setVisibility(View.GONE);
                    mMainFragment.setVisibility(View.VISIBLE);
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                } else {
                    super.onBackPressed();
                }
            } else {
                if (mAddEditLayout.getVisibility() == View.VISIBLE) {
                    mAddEditLayout.setVisibility(View.INVISIBLE);
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            // show dialogue to get confirmation to quit editing
            if (mAddEditLayout.getVisibility() == View.VISIBLE) {
                AppDialog dialog = new AppDialog();
                Bundle args = new Bundle();
                args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_CANCEL_EDIT);
                args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancel_edit_dialog_message));
                args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancel_edit_dialog_positive_caption);
                args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancel_edit_dialog_negative_caption);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), null);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: called");
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onStop();
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}



