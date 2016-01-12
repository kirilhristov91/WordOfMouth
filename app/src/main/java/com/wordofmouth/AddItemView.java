package com.wordofmouth;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

public class AddItemView extends AppCompatActivity implements View.OnClickListener{

    EditText itemNameField;
    EditText itemDescriptionField;
    RatingBar ratingBar;
    Button addItemButton;
    DBHandler dbHandler;
    double ratingSelected;
    int listId;
    String listName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        listId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");
        System.out.println("V ADD ITEM VIEW SYM: " + listId + " " + listName);

        ratingSelected = 0.0;

        dbHandler= DBHandler.getInstance(this);
        itemNameField = (EditText) findViewById(R.id.itemNameField);
        itemDescriptionField = (EditText) findViewById(R.id.itemDescriptionField);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        addItemButton = (Button) findViewById(R.id.addItemButton);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                ratingSelected = (double) rating;
            }
        });

        addItemButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addItemButton:
                Item i = new Item(itemNameField.getText().toString(), ratingSelected, itemDescriptionField.getText().toString());
                dbHandler.addItem(i, listId);
                Intent intent = new Intent(this, ItemsOfAListView.class);
                intent.putExtra("listId", listId);
                intent.putExtra("name", listName);
                startActivity(intent);
        }
    }
}
