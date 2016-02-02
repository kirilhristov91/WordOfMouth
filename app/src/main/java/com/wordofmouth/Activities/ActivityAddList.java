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
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import com.wordofmouth.Interfaces.GetListId;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ActivityAddList extends BaseActivity implements View.OnClickListener{

    EditText listNameField;
    EditText listDescriptionField;
    String image;
    Button createNewListButton;

    ImageView addImageToList;
    ImageView rotateRightList;
    ImageView rotateLeftList;
    int angle = 0;
    Bitmap photo;
    static final int REQUEST_BROWSE_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        listNameField = (EditText) findViewById(R.id.listNameField);
        listDescriptionField = (EditText) findViewById(R.id.listDescriptionField);

        addImageToList = (ImageView) findViewById(R.id.addImageToList);
        rotateRightList = (ImageView) findViewById(R.id.rotateRightList);
        rotateLeftList = (ImageView) findViewById(R.id.rotateLeftList);

        createNewListButton = (Button) findViewById(R.id.createNewListButton);

        addImageToList.setOnClickListener(this);
        rotateRightList.setOnClickListener(this);
        rotateLeftList.setOnClickListener(this);
        createNewListButton.setOnClickListener(this);
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
                Bitmap bm1=((BitmapDrawable)addImageToList.getDrawable()).getBitmap();
                Matrix matrix1 = new Matrix();
                matrix1.postRotate(angle);
                bm1 = Bitmap.createBitmap(bm1, 0, 0, bm1.getWidth(), bm1.getHeight(), matrix1, true);
                photo = bm1;
                break;
            case R.id.rotateLeftList:
                angle = angle-90;
                addImageToList.setRotation((float) angle);
                Bitmap bm2=((BitmapDrawable)addImageToList.getDrawable()).getBitmap();
                Matrix matrix2 = new Matrix();
                matrix2.postRotate(angle);
                bm2 = Bitmap.createBitmap(bm2, 0, 0, bm2.getWidth(), bm2.getHeight(), matrix2, true);
                photo = bm2;
                break;

            case R.id.createNewListButton:
                if(listNameField.getText().toString().equals("")){
                    showErrorEmptyField();
                }

                else {
                    if (!isNetworkAvailable()) {
                        showConnectionError();
                    }
                    else {
                        image = "";
                        if (photo != null) {
                            image = BitMapToString(photo);
                        }

                        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
                        int currentUserId = userLocalStore.getUserLoggedIn().getId();
                        String currentUserUsername = userLocalStore.getUserLoggedIn().getUsername();
                        MyList list = new MyList(currentUserId, currentUserUsername, listNameField.getText().toString(), listDescriptionField.getText().toString(), image);

                        final ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setCancelable(false);
                        progressDialog.setTitle("Processing");
                        progressDialog.setMessage("Uploading List to Server...");
                        progressDialog.show();

                        ServerRequests serverRequests = ServerRequests.getInstance(this);
                        serverRequests.UploadListAsyncTask(list, new GetListId() {
                            @Override
                            public void done(MyList returnedList) {
                                progressDialog.dismiss();
                                if (returnedList == null) {
                                    showAlreadyExistError();
                                } else {
                                    if (returnedList.get_username().equals("Timeout")) {
                                        showConnectionError();
                                    } else {
                                        DBHandler dbHandler = DBHandler.getInstance(ActivityAddList.this);
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
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
                while(desiredWidth/2 >= 100 || desiredHeight/2 >=100){
                    desiredWidth = desiredWidth/2;
                    desiredHeight = desiredHeight/2;
                }

                image = Bitmap.createScaledBitmap(image,desiredWidth, desiredHeight, true);

                photo = image;
                addImageToList.setImageBitmap(photo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    public void closeActivity(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showConnectionError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityAddList.this);
        allertBuilder.setMessage("Network error! Check your internet connection and try again!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showAlreadyExistError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("You have already created a list with that name!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showErrorEmptyField(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Please enter a name for the list!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

}