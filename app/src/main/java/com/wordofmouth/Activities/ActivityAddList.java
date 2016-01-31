package com.wordofmouth.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Interfaces.GetListId;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ActivityAddList extends BaseActivity implements View.OnClickListener{

    EditText listNameField;
    EditText listDescriptionField;
    UserLocalStore userLocalStore;
    String image;
    Button createNewListButton;
    DBHandler dbHandler;

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

        dbHandler= DBHandler.getInstance(this);
        userLocalStore = new UserLocalStore(this);
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
                    image ="";
                    if (photo != null) {
                        image = BitMapToString(photo, 30);
                    }

                    int currentUserId = userLocalStore.getUserLoggedIn().getId();
                    String currentUserUsername = userLocalStore.getUserLoggedIn().getUsername();
                    MyList list = new MyList(currentUserId, currentUserUsername, listNameField.getText().toString(), listDescriptionField.getText().toString(), image);
                    ServerRequests serverRequests = new ServerRequests(this);
                    serverRequests.UploadListAsyncTask(list, new GetListId() {
                        @Override
                        public void done(MyList returnedList) {
                            if(returnedList == null){
                                showAlreadyExistError();
                            }

                            else{
                                if(returnedList.get_username().equals("Timeout")){
                                    showConnectionError();
                                }
                                else {
                                    dbHandler.addList(returnedList);
                                    closeActivity();
                                }
                            }
                        }
                    });
                }
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

                while (image.getWidth() > 4096 || image.getHeight() > 4096) {
                    image = Bitmap.createScaledBitmap(image,image.getWidth()/2, image.getHeight()/2, true);
                }
                photo = image;
                addImageToList.setImageBitmap(image);
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