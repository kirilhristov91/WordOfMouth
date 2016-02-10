package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
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
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

public class ActivityItem extends BaseActivity implements View.OnClickListener{

    int listId;
    String listName;
    int itemId;
    int userId;
    double ratingSelected;
    String itemName;
    ImageView itemPicture;
    TextView itemNameTitle;
    TextView usernameText;
    TextView description;
    RatingBar itemRating;
    RatingBar rateItYourselfRatingBar;
    Button rateButton;
    Item item;
    ServerRequests serverRequests;
    UserLocalStore userLocalStore;
    int tabToreturn;
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

        rateItYourselfRatingBar = (RatingBar) findViewById(R.id.rateItYourselfRatingBar);
        rateItYourselfRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingSelected = (double) rating;
            }
        });

        Intent intent = getIntent();
        listId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("listName");
        itemId = intent.getIntExtra("itemId", 0);
        itemName = intent.getStringExtra("itemName");
        tabToreturn = intent.getIntExtra("tab", 0);

        getSupportActionBar().setTitle(itemName);
        DBHandler dbHandler = DBHandler.getInstance(this);
        item = dbHandler.getItem(itemId);

        Bitmap bitmap = StringToBitMap(item.get_itemImage());

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

    public Bitmap StringToBitMap(String encodedString){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, scaleOptions);

        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= 150
                && scaleOptions.outHeight / scale / 2 >= 150) {
            scale *= 2;
        }

        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,outOptions);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showConnectionError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Network error! Check your internet connection and try again!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showAlreadyRated(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("You have already rated that item!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.rateButton:
                if (!isNetworkAvailable()) {
                    showConnectionError();
                }
                else {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Processing");
                    progressDialog.setMessage("Uploading your rating to server...");
                    progressDialog.show();
                    serverRequests.rateInBackground(listId, itemId, userId, ratingSelected, new GetRateResponce() {
                        @Override
                        public void done(String response) {
                            progressDialog.dismiss();
                            if (response.equals("Timeout")) {
                                showConnectionError();
                            }
                            else if (response.equals("You have already rated that item\n")){
                                showAlreadyRated();
                            }
                            else  Toast.makeText(ActivityItem.this, "Your rating was uploaded to server", Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                break;
        }
    }
}


