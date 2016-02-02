package com.wordofmouth.Activities;

import android.app.Activity;
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
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetUserCallback;
import com.wordofmouth.R;
import com.wordofmouth.ObjectClasses.User;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ActivityProfile extends BaseActivity implements View.OnClickListener{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_BROWSE_GALLERY = 2;
    ImageView profilePictureinProfile;
    ImageView rotateRight;
    ImageView rotateLeft;
    Button updatePicture;
    Button chooseFromGallery;
    Button saveChanges;
    private int angle = 0;
    Bitmap toSave =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_profile);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setTitle("Edit Profile");

        System.out.println("ActivityProfile after SetContentView");

        profilePictureinProfile = (ImageView) findViewById(R.id.profilePicture);
        System.out.println(" V PROFILA " + (profilePictureinProfile==null));
        updatePicture = (Button) findViewById(R.id.updatePictureButton);
        chooseFromGallery = (Button) findViewById(R.id.chooseFromGallery);
        rotateRight = (ImageView) findViewById(R.id.rotateRight);
        rotateLeft = (ImageView) findViewById(R.id.rotateLeft);
        saveChanges = (Button) findViewById(R.id.saveProfileChanges);

        System.out.println("ActivityProfile after bindings : " + (profilePictureinProfile == null));

        if(!hasCamera()){
            updatePicture.setEnabled(false);
        }
        // check if there is a picture in the db taken just now
        // this is necessary because the activity restarts after the camera closes
        String pictureTakenWithTheCamera;
        if((pictureTakenWithTheCamera = dbHandler.getTemp())!=null){
            System.out.println("VLIZAM TUKA DA MU EBA MAIKATA");
            Bitmap pic = StringToBitMap(pictureTakenWithTheCamera);
            toSave = pic;
            profilePictureinProfile.setImageBitmap(toSave);
        }

        else {
            //get current image from database if there
            User currentUser = userLocalStore.getUserLoggedIn();
            String pic = dbHandler.getProfilePicture(currentUser.getId());
            if (pic != null) {
                Bitmap bitmap = StringToBitMap(pic);
                toSave = bitmap;
                profilePictureinProfile.setImageBitmap(toSave);
            }
        }
        rotateRight.setOnClickListener(this);
        rotateLeft.setOnClickListener(this);
        updatePicture.setOnClickListener(this);
        chooseFromGallery.setOnClickListener(this);
        saveChanges.setOnClickListener(this);

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
            case R.id.saveProfileChanges:
                if (toSave != null) saveImageToDB();
                break;

        }
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
                while(desiredWidth/2 >= 100 || desiredHeight/2 >= 100){
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
            showConnectionError();
        }

        else {
            User currentUser = userLocalStore.getUserLoggedIn();
            String imageToSave = BitMapToString(toSave);
            currentUser.getId();
            dbHandler.addProfilePicture(currentUser.getId(), imageToSave);
            dbHandler.deleteTemp();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Processing");
            progressDialog.setMessage("Uploading Profile Picture to Server...");
            progressDialog.show();
            serverRequests.UploadProfilePictureAsyncTask(currentUser.getUsername(), imageToSave, new GetUserCallback() {
                @Override
                public void done(User returnedUser) {
                    progressDialog.dismiss();
                    if (returnedUser != null) {
                        if (returnedUser.getUsername().equals("Timeout")) {
                            showConnectionError();
                        } else if (returnedUser.getUsername().equals("failure")) {
                            showUploadError();
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

    private void showConnectionError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityProfile.this);
        allertBuilder.setMessage("Network error! Check your internet connection and try again!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showUploadError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityProfile.this);
        allertBuilder.setMessage("Server Error! Failed to upload your picture!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    // method to transform image to string
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // shrink the file size of the image - nz kolko da e pomisli si
        bitmap.compress(Bitmap.CompressFormat.JPEG,80 , stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    // method to transform string to image
    public Bitmap StringToBitMap(String encodedString){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, scaleOptions);

        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= 100
                && scaleOptions.outHeight / scale / 2 >= 100) {
            scale *= 2;
        }

        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,outOptions);
    }

}
