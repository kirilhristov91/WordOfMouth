package com.wordofmouth.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.wordofmouth.R;

public class ActivityAbout extends BaseActivity {

    TextView aboutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_about);

        aboutText = (TextView) findViewById(R.id.aboutText);
        String about = "Word Of Mouth is a mobile application that allows users ";



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
