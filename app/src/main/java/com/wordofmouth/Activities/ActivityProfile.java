package com.wordofmouth.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetResponse;
import com.wordofmouth.Interfaces.GetUser;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.SharedPreferences.UserLocalStore;

public class ActivityProfile extends BaseActivity implements View.OnClickListener, View.OnTouchListener{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_BROWSE_GALLERY = 2;
    ImageView profilePictureinProfile, rotateRight, rotateLeft;
    Button updatePicture, chooseFromGallery, savePictureChanges, changePassword;
    EditText oldPasswordField, newPasswordField, newPasswordAgainField;
    private String oldPassword, newPassword;
    private int angle = 0;
    Bitmap toSave =null;
    private DBHandler dbHandler;
    private UserLocalStore userLocalStore;
    ServerRequests serverRequests;
    RelativeLayout profileLayout;
    ScrollView profileScroll;
    Utilities utilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dbHandler = DBHandler.getInstance(this);
        userLocalStore = UserLocalStore.getInstance(this);
        serverRequests = ServerRequests.getInstance(this);
        utilities = Utilities.getInstance(this);

        profilePictureinProfile = (ImageView) findViewById(R.id.profilePicture);
        updatePicture = (Button) findViewById(R.id.updatePictureButton);
        chooseFromGallery = (Button) findViewById(R.id.chooseFromGallery);
        rotateRight = (ImageView) findViewById(R.id.rotateRight);
        rotateLeft = (ImageView) findViewById(R.id.rotateLeft);
        savePictureChanges = (Button) findViewById(R.id.saveProfilePictureChanges);
        oldPasswordField = (EditText) findViewById(R.id.oldPasswordField);
        newPasswordField = (EditText) findViewById(R.id.newPasswordField);
        newPasswordAgainField = (EditText) findViewById(R.id.newPasswordAgainField);
        changePassword = (Button) findViewById(R.id.changePassword);
        profileLayout = (RelativeLayout) findViewById(R.id.profileLayout);
        profileScroll = (ScrollView) findViewById(R.id.profileScroll);
        profileLayout.requestFocus();

        rotateRight.setOnClickListener(this);
        rotateLeft.setOnClickListener(this);
        updatePicture.setOnClickListener(this);
        chooseFromGallery.setOnClickListener(this);
        savePictureChanges.setOnClickListener(this);
        changePassword.setOnClickListener(this);
        oldPasswordField.setOnTouchListener(this);
        newPasswordField.setOnTouchListener(this);
        newPasswordAgainField.setOnTouchListener(this);
        profileLayout.setOnTouchListener(this);


        if(!hasCamera()){
            updatePicture.setEnabled(false);
        }
        // check if there is a picture in the db taken just now
        // this is necessary because the activity restarts after the camera closes
        String pictureTakenWithTheCamera;
        if((pictureTakenWithTheCamera = dbHandler.getTemp())!=null){
            toSave = utilities.StringToBitMap(pictureTakenWithTheCamera, 200, 200);
            profilePictureinProfile.setImageBitmap(toSave);
        }

        else {
            //get current image from database if there
            User currentUser = userLocalStore.getUserLoggedIn();
            String pic = dbHandler.getProfilePicture(currentUser.getId());
            if (pic != null) {
                toSave = utilities.StringToBitMap(pic, 200, 200);
                profilePictureinProfile.setImageBitmap(toSave);
            }
        }
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.updatePictureButton:
                launchCamera();
                break;
            case R.id.chooseFromGallery:
                browseGallery();
                break;
            case R.id.rotateRight:
                angle = angle+90;
                profilePictureinProfile.setRotation((float) angle);
                toSave = utilities.rotate(((BitmapDrawable) profilePictureinProfile.getDrawable()).getBitmap(), angle);
                break;
            case R.id.rotateLeft:
                angle = angle-90;
                profilePictureinProfile.setRotation((float) angle);
                toSave = utilities.rotate(((BitmapDrawable) profilePictureinProfile.getDrawable()).getBitmap(), angle);
                break;
            case R.id.saveProfilePictureChanges:
                if (toSave != null) saveImageToDB();
                break;
            case R.id.changePassword:
                updatePassword();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId()) {
            case R.id.profileLayout:
                hideKeyboard(v);
                break;
            case R.id.oldPasswordField:
                profileScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        profileScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        oldPasswordField.requestFocus();
                    }
                }, 500);
                break;
            case R.id.newPasswordField:
                profileScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        profileScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        newPasswordField.requestFocus();
                    }
                }, 500);
                break;
            case R.id.newPasswordAgainField:
                profileScroll.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        profileScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        newPasswordAgainField.requestFocus();
                    }
                }, 500);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dbHandler.deleteTemp();
        finish();
    }

    //check if the user has a camera
    private boolean hasCamera(){
        PackageManager pm = this.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    // if take photo is chosen
    public void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    // if browse gallery is chosen
    public void browseGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_BROWSE_GALLERY);
    }

    // dealing with the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            dbHandler.setTemp(utilities.BitMapToString(image));
        }

        if (requestCode == REQUEST_BROWSE_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri targetUri = data.getData();
            System.out.println("TARGET URI: " + targetUri.toString());
            toSave = utilities.getBitmapFromURI(targetUri, 200, 200);
            profilePictureinProfile.setImageBitmap(toSave);
        }
    }

    public void saveImageToDB(){
        if(!isNetworkAvailable()){
            showError("Network error! Check your internet connection and try again!");
        }

        else {
            User currentUser = userLocalStore.getUserLoggedIn();
            String imageToSave = utilities.BitMapToString(toSave);
            dbHandler.addProfilePicture(currentUser.getId(), imageToSave);
            dbHandler.deleteTemp();

            final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            progressDialog.show();
            serverRequests.UploadProfilePictureInBackground(currentUser.getUsername(), imageToSave, new GetResponse() {
                @Override
                public void done(String response) {
                    progressDialog.dismiss();

                    if (response.equals("Timeout")) {
                        showError("Network error! Check your internet connection and try again!");
                    } else if (response.equals("failure")) {
                        showError("Server Error! Failed to upload your picture!");
                    } else {
                        Toast.makeText(ActivityProfile.this, "Your profile picture was updated!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ActivityProfile.this, MainActivity.class));
                        finish();
                    }
                }

            });
        }
    }

    public void updatePassword(){
        oldPassword = oldPasswordField.getText().toString();
        newPassword = newPasswordField.getText().toString();
        String newPasswordAgain = newPasswordAgainField.getText().toString();

        if(oldPassword.equals("") || newPassword.equals("") || newPasswordAgain.equals("")){
            showError("Empty field(s)!");
        }

        else if(!newPassword.equals(newPasswordAgain)){
            showError("New password mismatch!");
        }

        else{
            android.app.AlertDialog.Builder allertBuilder = new android.app.AlertDialog.Builder(this);
            allertBuilder.setMessage("Are you sure that you want to change your password?");
            allertBuilder.setCancelable(false);

            allertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (!isNetworkAvailable()) {
                        showError("Network error! Check your internet connection and try again!");
                    } else {
                        final ProgressDialog changePasswordPD = new ProgressDialog(ActivityProfile.this, R.style.MyTheme);
                        changePasswordPD.setCancelable(false);
                        changePasswordPD.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                        changePasswordPD.show();

                        int userId = userLocalStore.getUserLoggedIn().getId();
                        oldPassword = utilities.hashPassword(oldPassword);
                        newPassword = utilities.hashPassword(newPassword);
                        serverRequests.updatePasswordInBackground(userId, oldPassword, newPassword, new GetResponse() {
                            @Override
                            public void done(String response) {
                                changePasswordPD.dismiss();
                                if (response.equals("Timeout")) {
                                    showError("Network error! Check your internet connection and try again!");
                                } else if (response.equals("Incorrect password")) {
                                    showError("The old password you entered is incorrect");
                                } else if (response.equals("fail to update")) {
                                    showError("Server error!");
                                } else {
                                    Toast.makeText(ActivityProfile.this, "Your password has been updated!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });

            allertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Toast.makeText(ActivityPasswordReset.this, "DECLINED", Toast.LENGTH_SHORT).show();
                }
            });
            allertBuilder.create().show();
        }
    }
}
