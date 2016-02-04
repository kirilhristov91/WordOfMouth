package com.wordofmouth.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.wordofmouth.Interfaces.GetItemId;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ActivityAddItem extends BaseActivity implements View.OnClickListener{

    EditText itemNameField;
    EditText itemDescriptionField;
    RatingBar ratingBar;
    Button addItemButton;
    ImageView addItemPhoto;
    ImageView rotateRightItem;
    ImageView rotateLeftItem;
    double ratingSelected;
    int listId;
    String listName;
    int angle = 0;
    Bitmap photo;
    static final int REQUEST_BROWSE_GALLERY = 1;

    /*private UserLocalStore userLocalStore;
    private DBHandler dbHandler;
    private ServerRequests serverRequests;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_view);

        Intent intent = getIntent();
        listId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");

        ratingSelected = 0.0;
        photo = null;

        addItemPhoto = (ImageView) findViewById(R.id.addImageToItem);
        itemNameField = (EditText) findViewById(R.id.itemNameField);
        itemDescriptionField = (EditText) findViewById(R.id.itemDescriptionField);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        addItemButton = (Button) findViewById(R.id.addItemButton);
        rotateLeftItem = (ImageView) findViewById(R.id.rotateLeftItem);
        rotateRightItem = (ImageView) findViewById(R.id.rotateRightItem);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                ratingSelected = (double) rating;
            }
        });

        rotateRightItem.setOnClickListener(this);
        rotateLeftItem.setOnClickListener(this);
        addItemPhoto.setOnClickListener(this);
        addItemButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addImageToItem:
                browseGallery();
                break;
            case R.id.rotateRightItem:
                System.out.println("ROTATE RIGHT");
                angle = angle+90;
                addItemPhoto.setRotation((float) angle);
                Bitmap bm1=((BitmapDrawable)addItemPhoto.getDrawable()).getBitmap();
                Matrix matrix1 = new Matrix();
                matrix1.postRotate(angle);
                bm1 = Bitmap.createBitmap(bm1, 0, 0, bm1.getWidth(), bm1.getHeight(), matrix1, true);
                photo = bm1;
                break;
            case R.id.rotateLeftItem:
                System.out.println("ROTATE LEFT");
                angle = angle-90;
                addItemPhoto.setRotation((float) angle);
                Bitmap bm2=((BitmapDrawable)addItemPhoto.getDrawable()).getBitmap();
                Matrix matrix2 = new Matrix();
                matrix2.postRotate(angle);
                bm2 = Bitmap.createBitmap(bm2, 0, 0, bm2.getWidth(), bm2.getHeight(), matrix2, true);
                photo = bm2;
                break;

            case R.id.addItemButton:
                if(itemNameField.getText().toString().equals("")){
                    showErrorEmptyField();
                }
                else {
                    //System.out.println(userLocalStore.getUserLocalDatabase().getAll().toString());
                    if (!isNetworkAvailable()) {
                        showConnectionError();
                    }
                    else {
                        String imageToSave = "";
                        if (photo != null) {
                            imageToSave = BitMapToString(photo);
                        }
                        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
                        Item i = new Item(listId, userLocalStore.getUserLoggedIn().getId(), userLocalStore.getUserLoggedIn().getUsername(),
                                itemNameField.getText().toString(), ratingSelected, itemDescriptionField.getText().toString(), imageToSave);

                        final ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setCancelable(false);
                        progressDialog.setTitle("Processing");
                        progressDialog.setMessage("Uploading Item to Server...");
                        progressDialog.show();

                        ServerRequests serverRequests = ServerRequests.getInstance(this);
                        serverRequests.UploadItemAsyncTask(i, new GetItemId() {
                            @Override
                            public void done(Item item) {
                                progressDialog.dismiss();
                                if (item == null) {
                                    showAlreadyExistError();
                                } else {
                                    if (item.get_creatorUsername().equals("Timeout")) {
                                        showConnectionError();
                                    } else {
                                        DBHandler dbHandler = DBHandler.getInstance(ActivityAddItem.this);
                                        dbHandler.addItem(item);
                                        Intent intent = new Intent(ActivityAddItem.this, ActivityItemsOfAList.class);
                                        intent.putExtra("listId", listId);
                                        intent.putExtra("name", listName);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void showAlreadyExistError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("An item with that name for that list already exists!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showErrorEmptyField(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Please enter a name for the item!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActivityItemsOfAList.class);
        intent.putExtra("listId", listId);
        intent.putExtra("name", listName);
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
            Bitmap image;
            try {
                image = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(targetUri));

                int desiredWidth = image.getWidth();
                int desiredHeight = image.getHeight();
                while(desiredWidth/2 >= 150 || desiredHeight/2 >=150){
                    desiredWidth = desiredWidth/2;
                    desiredHeight = desiredHeight/2;
                }

                image = Bitmap.createScaledBitmap(image,desiredWidth, desiredHeight, true);

                photo = image;
                addItemPhoto.setImageBitmap(photo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void showConnectionError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityAddItem.this);
        allertBuilder.setMessage("Network error! Check your internet connection and try again!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

}
