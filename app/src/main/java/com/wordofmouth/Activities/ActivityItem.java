package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetRateResponce;
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
        DBHandler dbHandler = DBHandler.getInstance(this);
        utilities = Utilities.getInstance(this);

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

        serverRequests = ServerRequests.getInstance(this);
        userLocalStore = UserLocalStore.getInstance(this);
        userId = userLocalStore.getUserLoggedIn().getId();
        rateButton.setOnClickListener(this);
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
                    serverRequests.rateInBackground(listId, itemId, userId, ratingSelected, new GetRateResponce() {
                        @Override
                        public void done(String response) {
                            progressDialog.dismiss();
                            if (response.equals("Timeout")) {
                                showError("Network error! Check your internet connection and try again!");
                            }
                            else if (response.equals("You have already rated that item\n")) {
                                showError("You have already rated that item!");
                            }
                            else  Toast.makeText(ActivityItem.this, "Your rating was uploaded to server", Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                break;
        }
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


