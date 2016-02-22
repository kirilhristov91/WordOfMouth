package com.wordofmouth.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.wordofmouth.Interfaces.GetItem;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

public class ActivityAddItem extends BaseActivity implements View.OnClickListener, View.OnTouchListener{

    static final int REQUEST_BROWSE_GALLERY = 1;
    EditText itemNameField, itemDescriptionField;
    RatingBar ratingBar;
    Button addItemButton;
    ImageView addItemPhoto, rotateRightItem, rotateLeftItem;
    String listName;
    double ratingSelected;
    int angle = 0 , listId, tabToreturn;
    Bitmap photo;
    RelativeLayout addItemLayout;
    Utilities utilities;
    ScrollView itemScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_view);

        Intent intent = getIntent();
        listId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");
        tabToreturn = intent.getIntExtra("tab", 0);

        ratingSelected = 0.0;
        photo = null;
        utilities = Utilities.getInstance(this);

        addItemPhoto = (ImageView) findViewById(R.id.addImageToItem);
        itemNameField = (EditText) findViewById(R.id.itemNameField);
        itemDescriptionField = (EditText) findViewById(R.id.itemDescriptionField);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        addItemButton = (Button) findViewById(R.id.addItemButton);
        rotateLeftItem = (ImageView) findViewById(R.id.rotateLeftItem);
        rotateRightItem = (ImageView) findViewById(R.id.rotateRightItem);

        itemScroll = (ScrollView) findViewById(R.id.itemScroll);
        addItemLayout = (RelativeLayout) findViewById(R.id.addItemLayout);
        addItemLayout.setOnTouchListener(this);
        addItemLayout.requestFocus();
        itemNameField.setOnTouchListener(this);
        itemDescriptionField.setOnTouchListener(this);

        rotateRightItem.setOnClickListener(this);
        rotateLeftItem.setOnClickListener(this);
        addItemPhoto.setOnClickListener(this);
        addItemButton.setOnClickListener(this);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                ratingSelected = (double) rating;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addImageToItem:
                browseGallery();
                break;
            case R.id.rotateRightItem:
                angle = angle+90;
                addItemPhoto.setRotation((float) angle);
                photo = utilities.rotate(((BitmapDrawable) addItemPhoto.getDrawable()).getBitmap(),angle);
                break;
            case R.id.rotateLeftItem:
                angle = angle-90;
                addItemPhoto.setRotation((float) angle);
                photo = utilities.rotate(((BitmapDrawable) addItemPhoto.getDrawable()).getBitmap(),angle);
                break;

            case R.id.addItemButton:
                if(itemNameField.getText().toString().equals("")){
                    showError("Please enter a name for the item!");
                }
                else {
                    if (!isNetworkAvailable()) {
                        showError("Network error! Check your internet connection and try again!");
                    }
                    else {
                        String imageToSave = "";
                        if (photo != null) {
                            imageToSave = utilities.BitMapToString(photo);
                        }
                        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
                        Item i = new Item(listId, userLocalStore.getUserLoggedIn().getId(), userLocalStore.getUserLoggedIn().getUsername(),
                                itemNameField.getText().toString(), ratingSelected, 1, itemDescriptionField.getText().toString(), imageToSave);

                        final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                        progressDialog.show();

                        ServerRequests serverRequests = ServerRequests.getInstance(this);
                        serverRequests.UploadItemInBackground(i, new GetItem() {
                            @Override
                            public void done(Item item) {
                                progressDialog.dismiss();
                                if (item == null) {
                                    showError("An item with that name for that list already exists!");
                                } else {
                                    if (item.get_creatorUsername().equals("Timeout")) {
                                        showError("Network error! Check your internet connection and try again!");
                                    } else {
                                        DBHandler dbHandler = DBHandler.getInstance(ActivityAddItem.this);
                                        item.setSeen(1);
                                        dbHandler.addItem(item);
                                        Intent intent = new Intent(ActivityAddItem.this, ActivityItemsOfAList.class);
                                        intent.putExtra("listId", listId);
                                        intent.putExtra("name", listName);
                                        intent.putExtra("tab", tabToreturn);
                                        if(photo!=null) {
                                            photo.recycle();
                                        }
                                        startActivity(intent);
                                        finish();
                                    }
                                }
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
            case R.id.addItemLayout:
                hideKeyboard(v);
                break;
            case R.id.itemNameField:
                itemScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        itemScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        itemNameField.requestFocus();
                    }
                }, 500);
                break;
            case R.id.itemDescriptionField:
                itemScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        itemScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        itemDescriptionField.requestFocus();
                    }
                }, 500);
                break;
        }
        return false;
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

    public void browseGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_BROWSE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BROWSE_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri targetUri = data.getData();
            photo = utilities.getBitmapFromURI(targetUri, 150, 150);
            addItemPhoto.setImageBitmap(photo);

        }
    }
}
