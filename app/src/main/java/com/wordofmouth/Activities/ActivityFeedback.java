package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetFeedbackResponse;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;

public class ActivityFeedback extends BaseActivity implements View.OnClickListener{

    EditText feedbackField;
    Button feedbackButton;
    ServerRequests serverRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_feedback);

        feedbackField = (EditText) findViewById(R.id.feedbackField);
        feedbackButton = (Button) findViewById(R.id.feedbackButton);
        serverRequests = ServerRequests.getInstance(this);

        feedbackButton.setOnClickListener(this);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showEmptyError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Please type your feedback in the specified field!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showConnectionError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Network error! Check your internet connection and try again!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.feedbackButton:
                String feedback = feedbackField.getText().toString();
                if(feedback.equals("")){
                    showEmptyError();
                }
                else{
                    if(!isNetworkAvailable()){
                        showConnectionError();
                    }
                    else{
                        final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                        progressDialog.show();
                        serverRequests.sendFeedbackInBackground(feedback, new GetFeedbackResponse() {
                            @Override
                            public void done(String response) {
                                progressDialog.dismiss();
                                if(response.equals("Timeout")){
                                    showConnectionError();
                                }
                                else Toast.makeText(ActivityFeedback.this, "Your feedback has been sent!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                break;
        }
    }
}
