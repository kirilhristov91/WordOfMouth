package com.wordofmouth;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
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

public class ActivityAddItem extends AppCompatActivity implements View.OnClickListener{

    EditText itemNameField;
    EditText itemDescriptionField;
    RatingBar ratingBar;
    Button addItemButton;
    ImageView addItemPhoto;
    ImageView rotateRightItem;
    ImageView rotateLeftItem;

    DBHandler dbHandler;
    UserLocalStore userLocalStore;
    double ratingSelected;
    int listId;
    String listName;
    int angle = 0;
    Bitmap photo;
    static final int REQUEST_BROWSE_GALLERY = 1;

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
        photo = null;

        dbHandler= DBHandler.getInstance(this);
        userLocalStore = new UserLocalStore(this);
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
                System.out.println(userLocalStore.userLocalDatabase.getAll().toString());
                String imageToSave="";
                if(photo!=null) {
                   imageToSave = BitMapToString(photo,30);
                }
                Item i = new Item(itemNameField.getText().toString(), ratingSelected, itemDescriptionField.getText().toString(), imageToSave, userLocalStore.getUserLoggedIn().getUsername());
                dbHandler.addItem(i, listId);
                Intent intent = new Intent(this, ActivityItemsOfAList.class);
                intent.putExtra("listId", listId);
                intent.putExtra("name", listName);
                startActivity(intent);
                finish();
                break;
        }
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

                while (image.getWidth() > 4096 || image.getHeight() > 4096) {
                    image = Bitmap.createScaledBitmap(image,image.getWidth()/2, image.getHeight()/2, true);
                }
                photo = image;
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
