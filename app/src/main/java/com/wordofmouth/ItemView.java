package com.wordofmouth;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ItemView extends AppCompatActivity {

    int listId;
    String listName;
    int itemId;
    String itemName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_item_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        listId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("listName");
        itemId = intent.getIntExtra("itemId", 0);
        itemName = intent.getStringExtra("itemName");

        getSupportActionBar().setTitle(itemName);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ItemsOfAListView.class);
        intent.putExtra("listId", listId);
        intent.putExtra("name", listName);
        startActivity(intent);
        finish();
    }

}


