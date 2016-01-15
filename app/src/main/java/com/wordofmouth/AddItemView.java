package com.wordofmouth;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class AddItemView extends AppCompatActivity implements View.OnClickListener{

    EditText itemNameField;
    EditText itemDescriptionField;
    RatingBar ratingBar;
    Button addItemButton;
    DBHandler dbHandler;
    double ratingSelected;
    int listId;
    String listName;
    ImageView addItemPhoto;
    String photo="";
    static final int REQUEST_BROWSE_GALLERY = 1;
    UserLocalStore userLocalStore;

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
        userLocalStore = new UserLocalStore(this);
        addItemPhoto = (ImageView) findViewById(R.id.addImageToItem);
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

        addItemPhoto.setOnClickListener(this);
        addItemButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addImageToItem:
                browseGallery();
                break;
            case R.id.addItemButton:
                System.out.println(userLocalStore.userLocalDatabase.getAll().toString());
                Item i = new Item(itemNameField.getText().toString(), ratingSelected, itemDescriptionField.getText().toString(), photo, userLocalStore.getUserLoggedIn().getUsername());
                dbHandler.addItem(i, listId);
                Intent intent = new Intent(this, ItemsOfAListView.class);
                intent.putExtra("listId", listId);
                intent.putExtra("name", listName);
                startActivity(intent);
                finish();
                break;
        }
    }

    public void browseGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_BROWSE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BROWSE_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri targetUri = data.getData();
            Bitmap image;
            try {
                image = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(targetUri));
                photo = BitMapToString(image,25);
                addItemPhoto.setImageBitmap(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String BitMapToString(Bitmap bitmap, int compressFactor){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // shrink the file size of the image - nz kolko da e pomisli si
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressFactor, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

}
