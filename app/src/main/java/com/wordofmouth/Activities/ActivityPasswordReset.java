package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetResponse;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;

public class ActivityPasswordReset extends BaseActivity implements View.OnClickListener{

    EditText emailField;
    Button resetButton;
    ServerRequests serverRequests;
    String email="";
    LinearLayout resetPasswordLayout;
    Utilities utilities;

    @Override
    public boolean usesToolbar() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        serverRequests = ServerRequests.getInstance(this);
        utilities = Utilities.getInstance(this);
        emailField = (EditText) findViewById(R.id.emailforReset);
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
                    // ask the user to confirm he/she wants to reset his/her password
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
                    // if confirmed add connect to the server to reset the password
                    final ProgressDialog progressDialogDownloadList = new ProgressDialog(ActivityPasswordReset.this, R.style.MyTheme);
                    progressDialogDownloadList.setCancelable(false);
                    progressDialogDownloadList.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                    progressDialogDownloadList.show();
                    serverRequests.resetPasswordInBackground(email, new GetResponse() {
                        @Override
                        public void done(String response) {
                            progressDialogDownloadList.dismiss();
                            if (response.equals("Timeout")) {
                                showError("Network error! Check your internet connection and try again!");
                            } else if (response.equals("No user with such email")) {
                                showError("There is no user with such email!");
                            } else {
                                // notify the user that his/her password has been reset
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
}
