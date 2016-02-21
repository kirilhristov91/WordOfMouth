package com.wordofmouth.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetPasswordResetResponse;
import com.wordofmouth.Interfaces.GetUserCallback;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_profile);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dbHandler = DBHandler.getInstance(this);
        userLocalStore = UserLocalStore.getInstance(this);
        serverRequests = ServerRequests.getInstance(this);

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
            toSave = StringToBitMap(pictureTakenWithTheCamera);
            profilePictureinProfile.setImageBitmap(toSave);
        }

        else {
            //get current image from database if there
            User currentUser = userLocalStore.getUserLoggedIn();
            String pic = dbHandler.getProfilePicture(currentUser.getId());
            if (pic != null) {
                toSave = StringToBitMap(pic);
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
                Bitmap bm1=((BitmapDrawable) profilePictureinProfile.getDrawable()).getBitmap();
                Matrix matrix1 = new Matrix();
                matrix1.postRotate(angle);
                bm1 = Bitmap.createBitmap(bm1, 0, 0, bm1.getWidth(), bm1.getHeight(), matrix1, true);
                toSave = bm1;
                break;
            case R.id.rotateLeft:
                angle = angle-90;
                profilePictureinProfile.setRotation((float) angle);
                Bitmap bm2=((BitmapDrawable) profilePictureinProfile.getDrawable()).getBitmap();
                Matrix matrix2 = new Matrix();
                matrix2.postRotate(angle);
                bm2 = Bitmap.createBitmap(bm2, 0, 0, bm2.getWidth(), bm2.getHeight(), matrix2, true);
                toSave = bm2;
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
            dbHandler.setTemp(BitMapToString(image));
        }

        if (requestCode == REQUEST_BROWSE_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri targetUri = data.getData();
            System.out.println(targetUri.toString());
            Bitmap image;
            try {
                image = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(targetUri));

                int desiredWidth = image.getWidth();
                int desiredHeight = image.getHeight();
                while(desiredWidth/2 >= 200 || desiredHeight/2 >= 200){
                    desiredWidth = desiredWidth/2;
                    desiredHeight = desiredHeight/2;
                }

                image = Bitmap.createScaledBitmap(image,desiredWidth, desiredHeight, true);
                profilePictureinProfile.setImageBitmap(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void saveImageToDB(){
        if(!isNetworkAvailable()){
            showError("Network error! Check your internet connection and try again!");
        }

        else {
            User currentUser = userLocalStore.getUserLoggedIn();
            String imageToSave = BitMapToString(toSave);
            dbHandler.addProfilePicture(currentUser.getId(), imageToSave);
            dbHandler.deleteTemp();

            final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            progressDialog.show();
            serverRequests.UploadProfilePictureAsyncTask(currentUser.getUsername(), imageToSave, new GetUserCallback() {
                @Override
                public void done(User returnedUser) {
                    progressDialog.dismiss();
                    if (returnedUser != null) {
                        if (returnedUser.getUsername().equals("Timeout")) {
                            showError("Network error! Check your internet connection and try again!");
                        } else if (returnedUser.getUsername().equals("failure")) {
                            showError("Server Error! Failed to upload your picture!");
                        }
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
                        oldPassword = hashPassword(oldPassword);
                        newPassword = hashPassword(newPassword);
                        serverRequests.updatePasswordInBackground(userId, oldPassword, newPassword, new GetPasswordResetResponse() {
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

    private void showError(String message){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityProfile.this);
        allertBuilder.setMessage(message);
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    // method to transform image to string
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // shrink the file size of the image - nz kolko da e pomisli si
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    // method to transform string to image
    public Bitmap StringToBitMap(String encodedString){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, scaleOptions);

        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= 200
                && scaleOptions.outHeight / scale / 2 >= 200) {
            scale *= 2;
        }

        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, outOptions);
    }

    public String hashPassword(String password){
        String generatedPassword = null;
        try {
            // Hash the password in MD5 format
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


}
