package com.wordofmouth.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.R;

public class ActivityItem extends BaseActivity {

    int listId;
    String listName;
    int itemId;
    String itemName;
    ImageView itemPicture;
    TextView itemNameTitle;
    TextView usernameText;
    TextView description;
    RatingBar itemRating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        itemNameTitle = (TextView) findViewById(R.id.itemNameTitle);
        itemPicture = (ImageView) findViewById(R.id.itemPicture);
        usernameText = (TextView) findViewById(R.id.creatorUsername);
        description = (TextView) findViewById(R.id.description);
        itemRating = (RatingBar) findViewById(R.id.itemRatingBar);

        Intent intent = getIntent();
        listId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("listName");
        itemId = intent.getIntExtra("itemId", 0);
        itemName = intent.getStringExtra("itemName");

        getSupportActionBar().setTitle(itemName);
        DBHandler dbHandler = DBHandler.getInstance(this);
        Item item = dbHandler.getItem(itemId);

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
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActivityItemsOfAList.class);
        intent.putExtra("listId", listId);
        intent.putExtra("name", listName);
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

}


