package com.wordofmouth.Activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.wordofmouth.Interfaces.GetResponse;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.util.ArrayList;

public class ActivityItem extends BaseActivity implements View.OnClickListener{

    String listName, itemName;
    int itemId, userId, listId, tabToreturn;
    double ratingSelected;
    ImageView itemPicture;
    TextView itemNameTitle, usernameText, description, ratedBy;
    RatingBar itemRating, rateItYourselfRatingBar;
    Button rateButton;
    Item item;
    ServerRequests serverRequests;
    UserLocalStore userLocalStore;
    Utilities utilities;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        itemNameTitle = (TextView) findViewById(R.id.itemNameTitle);
        itemPicture = (ImageView) findViewById(R.id.itemPicture);
        usernameText = (TextView) findViewById(R.id.creatorUsername);
        description = (TextView) findViewById(R.id.description);
        itemRating = (RatingBar) findViewById(R.id.itemRatingBar);
        rateButton = (Button) findViewById(R.id.rateButton);
        ratedBy = (TextView) findViewById(R.id.ratedBy);

        Intent intent = getIntent();
        listId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("listName");
        itemId = intent.getIntExtra("itemId", 0);
        itemName = intent.getStringExtra("itemName");
        tabToreturn = intent.getIntExtra("tab", 0);

        getSupportActionBar().setTitle(itemName);

        rateItYourselfRatingBar = (RatingBar) findViewById(R.id.rateItYourselfRatingBar);
        rateItYourselfRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingSelected = (double) rating;
            }
        });
        dbHandler = DBHandler.getInstance(this);
        utilities = Utilities.getInstance(this);


        serverRequests = ServerRequests.getInstance(this);
        userLocalStore = UserLocalStore.getInstance(this);
        userId = userLocalStore.getUserLoggedIn().getId();
        rateButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<Integer> seens = dbHandler.getSeens(listId);
        boolean flag = false;
        for(Integer seen: seens){
            if(seen == 0) flag = true;
        }

        if(!flag){
            dbHandler.updateHasNewContent(listId, 0);
        }

        item = dbHandler.getItem(itemId);

        Bitmap bitmap = utilities.StringToBitMap(item.get_itemImage(), 150, 150);

        itemNameTitle.setText(item.get_name());
        if(bitmap!=null) {
            itemPicture.setImageBitmap(bitmap);
        }
        else {
            itemPicture.setImageResource(R.drawable.logowom);
        }

        usernameText.setText(item.get_creatorUsername());
        description.setText(item.get_description());
        itemRating.setRating((float) item.get_rating());

        if(item.getRatingCounter()>1){
            ratedBy.setText("Rated by " + item.getRatingCounter() + " users");
        }
        else{
            ratedBy.setText("Rated by 1 user");
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActivityItemsOfAList.class);
        intent.putExtra("listId", listId);
        intent.putExtra("name", listName);
        intent.putExtra("tab", tabToreturn);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.rateButton:
                if (!isNetworkAvailable()) {
                    showError("Network error! Check your internet connection and try again!");
                }
                else {
                    final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                    progressDialog.show();
                    serverRequests.rateInBackground(listId, itemId, userId, ratingSelected, new GetResponse() {
                        @Override
                        public void done(String response) {
                            progressDialog.dismiss();
                            if (response.equals("Timeout")) {
                                showError("Network error! Check your internet connection and try again!");
                            }
                            else if (response.equals("You have already rated that item")) {
                                showError("You have already rated that item!");
                            }
                            else  Toast.makeText(ActivityItem.this, "Your rating was uploaded to server", Toast.LENGTH_SHORT).show();
                            //onResume();
                        }

                    });
                }
                break;
        }
    }
}


