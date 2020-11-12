package com.ivokarchev.tasktimer;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class AddEditActivity extends AppCompatActivity implements AppDialog.DialogEvents {
    private static final String TAG = "AddEditActivity";
    private AddEditActivityFragment mAddEditActivityFragment;
    public static final int DIALOG_ID_CANCEL_EDIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: saved instance state is null");
            mAddEditActivityFragment = new AddEditActivityFragment();
            Bundle arguments = getIntent().getExtras();
            mAddEditActivityFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.task_details_container, mAddEditActivityFragment)
                    .commit();
        }

        if(mAddEditActivityFragment == null && savedInstanceState != null){
            mAddEditActivityFragment = (AddEditActivityFragment) savedInstanceState.getSerializable(AddEditActivityFragment.class.getSimpleName());
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: home button pressed");
                if(mAddEditActivityFragment != null && mAddEditActivityFragment.canClose()) {
                    return super.onOptionsItemSelected(item);
                } else {
                    showConfirmationDialog();
                    return true;
                }
            default:
                Log.d(TAG, "onOptionsItemSelected: called");
                return super.onOptionsItemSelected(item);
        }
    }

    private void showConfirmationDialog(){
        AppDialog dialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_CANCEL_EDIT);
        args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancel_edit_dialog_message));
        args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancel_edit_dialog_positive_caption);
        args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancel_edit_dialog_negative_caption);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveDialogResult: called");
    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onNegativeDialogResult: called");
        finish();
    }

    @Override
    public void onDialogCancelled(int dialogId) {
        Log.d(TAG, "onDialogCancelled: called");
    }

    @Override
    public void onBackPressed() {
        if (mAddEditActivityFragment == null || mAddEditActivityFragment.canClose()) {
            super.onBackPressed();
        } else {
           showConfirmationDialog();
        }
    }
}
