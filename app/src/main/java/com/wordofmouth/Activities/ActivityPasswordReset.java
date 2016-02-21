package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetListId;
import com.wordofmouth.Interfaces.GetPasswordResetResponse;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.ObjectClasses.Notification;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

public class ActivityPasswordReset extends AppCompatActivity implements View.OnClickListener{

    EditText emailField;
    Button resetButton;
    ServerRequests serverRequests;
    String email="";
    LinearLayout resetPasswordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_password_reset);

        serverRequests = ServerRequests.getInstance(this);
        emailField = (EditText) findViewById(R.id.emailPasswordReset);
        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(this);
        resetPasswordLayout = (LinearLayout) findViewById(R.id.resetPasswordLayout);
        resetPasswordLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.resetButton:
                hideKeyboard(v);
                email = emailField.getText().toString();
                if(emailField.getText().toString().equals("")){
                    showError("Please provide an email!");
                }
                else{
                    showConfirmationDialog();
                }
                break;
        }
    }

    private void showConfirmationDialog(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Are you sure that you want to reset your password?");
        allertBuilder.setCancelable(false);

        allertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!isNetworkAvailable()) {
                    showError("Network error! Check your internet connection and try again!");
                } else {
                    final ProgressDialog progressDialogDownloadList = new ProgressDialog(ActivityPasswordReset.this, R.style.MyTheme);
                    progressDialogDownloadList.setCancelable(false);
                    progressDialogDownloadList.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                    progressDialogDownloadList.show();
                    serverRequests.resetPasswordInBackground(email, new GetPasswordResetResponse() {
                        @Override
                        public void done(String response) {
                            progressDialogDownloadList.dismiss();
                            if (response.equals("Timeout")) {
                                showError("Network error! Check your internet connection and try again!");
                            }
                            else if(response.equals("No user with such email")){
                                showError("There is no user with such email!");
                            }

                            else{
                                Toast.makeText(ActivityPasswordReset.this, "Check your email for your new password!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            }
        });

        allertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(ActivityPasswordReset.this, "DECLINED", Toast.LENGTH_SHORT).show();
            }
        });
        allertBuilder.create().show();
    }

    public void showError(String message){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage(message);
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
