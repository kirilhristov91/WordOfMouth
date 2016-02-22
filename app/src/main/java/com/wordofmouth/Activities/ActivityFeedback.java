package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetFeedbackResponse;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;

public class ActivityFeedback extends BaseActivity implements View.OnClickListener, View.OnTouchListener{

    EditText feedbackField;
    Button feedbackButton;
    ServerRequests serverRequests;
    LinearLayout feedbackLayout;
    Utilities utilities;
    ScrollView feedbackScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_feedback);

        serverRequests = ServerRequests.getInstance(this);
        utilities = Utilities.getInstance(this);

        feedbackScroll = (ScrollView) findViewById(R.id.feedbackScroll);
        feedbackLayout = (LinearLayout) findViewById(R.id.feedbackLayout);
        feedbackField = (EditText) findViewById(R.id.feedbackField);
        feedbackButton = (Button) findViewById(R.id.feedbackButton);

        feedbackLayout.setOnTouchListener(this);
        feedbackField.setOnTouchListener(this);
        feedbackButton.setOnClickListener(this);
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
                    showError("Please type your feedback in the specified field!");
                }
                else{
                    if(!isNetworkAvailable()){
                        showError("Network error! Check your internet connection and try again!");
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
                                    showError("Network error! Check your internet connection and try again!");
                                }
                                else Toast.makeText(ActivityFeedback.this, "Your feedback has been sent!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId()) {
            case R.id.feedbackLayout:
                utilities.hideKeyboard(v);
                break;
            case R.id.feedbackField:
                feedbackScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        feedbackScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        feedbackField.requestFocus();
                    }
                }, 500);
                break;
        }
        return false;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showError(String message){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage(message);
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }
}
