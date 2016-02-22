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
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.wordofmouth.Interfaces.GetList;
import com.wordofmouth.ObjectClasses.List;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

public class ActivityAddList extends BaseActivity implements View.OnClickListener, View.OnTouchListener{

    static final int REQUEST_BROWSE_GALLERY = 1;
    EditText listNameField, listDescriptionField;
    String image;
    Button createNewListButton;
    ImageView addImageToList, rotateRightList, rotateLeftList;
    Bitmap photo;
    RelativeLayout addListLayout;
    Utilities utilities;
    int angle = 0;
    ScrollView listScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        utilities = Utilities.getInstance(this);
        listNameField = (EditText) findViewById(R.id.listNameField);
        listDescriptionField = (EditText) findViewById(R.id.listDescriptionField);

        addImageToList = (ImageView) findViewById(R.id.addImageToList);
        rotateRightList = (ImageView) findViewById(R.id.rotateRightList);
        rotateLeftList = (ImageView) findViewById(R.id.rotateLeftList);
        createNewListButton = (Button) findViewById(R.id.createNewListButton);
        listScroll = (ScrollView) findViewById(R.id.listScroll);

        addImageToList.setOnClickListener(this);
        rotateRightList.setOnClickListener(this);
        rotateLeftList.setOnClickListener(this);
        createNewListButton.setOnClickListener(this);

        addListLayout = (RelativeLayout) findViewById(R.id.addListLayout);
        addListLayout.setOnTouchListener(this);
        addListLayout.requestFocus();
        listNameField.setOnTouchListener(this);
        listDescriptionField.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addImageToList:
                browseGallery();
                break;
            case R.id.rotateRightList:
                angle = angle+90;
                addImageToList.setRotation((float) angle);
                photo = utilities.rotate(((BitmapDrawable)addImageToList.getDrawable()).getBitmap(),angle);
                break;
            case R.id.rotateLeftList:
                angle = angle-90;
                addImageToList.setRotation((float) angle);
                photo = utilities.rotate(((BitmapDrawable)addImageToList.getDrawable()).getBitmap(),angle);
                break;

            case R.id.createNewListButton:
                if(listNameField.getText().toString().equals("")){
                    showError("Please enter a name for the list!");
                }

                else {
                    if (!isNetworkAvailable()) {
                        showError("Network error! Check your internet connection and try again!");
                    }
                    else {
                        image = "";
                        if (photo != null) {
                            image = utilities.BitMapToString(photo);
                        }

                        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
                        int currentUserId = userLocalStore.getUserLoggedIn().getId();
                        String currentUserUsername = userLocalStore.getUserLoggedIn().getUsername();
                        List list = new List(currentUserId, currentUserUsername, listNameField.getText().toString(), listDescriptionField.getText().toString(), image);

                        final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                        progressDialog.show();

                        ServerRequests serverRequests = ServerRequests.getInstance(this);
                        serverRequests.UploadListAsyncTask(list, new GetList() {
                            @Override
                            public void done(List returnedList) {
                                progressDialog.dismiss();
                                if (returnedList == null) {
                                    showError("You have already created a list with that name!");
                                } else {
                                    if (returnedList.get_username().equals("Timeout")) {
                                       showError("Network error! Check your internet connection and try again!");
                                    } else {
                                        DBHandler dbHandler = DBHandler.getInstance(ActivityAddList.this);
                                        returnedList.setHasNewContent(0);
                                        dbHandler.addList(returnedList);
                                        if(photo!=null) {
                                            photo.recycle();
                                        }

                                        closeActivity();
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
            case R.id.addListLayout:
                hideKeyboard(v);
                break;
            case R.id.listNameField:
                listScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        listNameField.requestFocus();
                    }
                }, 500);
                break;
            case R.id.listDescriptionField:
                listScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        listDescriptionField.requestFocus();
                    }
                }, 500);
                break;
        }
        return false;
    }

    public void closeActivity(){
        startActivity(new Intent(this, MainActivity.class));
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
            photo = utilities.getBitmapFromURI(targetUri, 100, 100);
            addImageToList.setImageBitmap(photo);
        }
    }
}