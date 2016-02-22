package com.wordofmouth.Activities;

import android.os.Bundle;
import android.widget.TextView;

import com.wordofmouth.R;

public class ActivityAbout extends BaseActivity {

    TextView aboutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        aboutText = (TextView) findViewById(R.id.aboutText);
        String about = "Word Of Mouth is a mobile application that allows users to share their interests with each other."
                + " Here users manage 'shared' lists of interests with people they know and trust."
                + " Users can create a list with a specific topic like 'Favourite movies'), add items to it and invite people "
                + "- who they know would like wathcing movies - to manage the list. Each joined user will then be able to add new"
                + " items to the list, rate the existing items and invite more people to join. It is easy, so try it and enjoy!";
        aboutText.setText(about);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
