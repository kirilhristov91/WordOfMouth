package com.wordofmouth.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Interfaces.GetUserCallback;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import com.wordofmouth.R;

public class ActivityProfile extends BaseActivity implements View.OnClickListener{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_BROWSE_GALLERY = 2;
    ImageView profilePicture;
    ImageView rotateRight;
    ImageView rotateLeft;
    Button updatePicture;
    Button chooseFromGallery;
    Button saveChanges;
    int angle = 0;
    Bitmap toSave;
    boolean fromGallery = false;
    DBHandler dbHandler;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_profile);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Eidt Profile");

        profilePicture = (ImageView) this.findViewById(R.id.profilePicture);
        updatePicture = (Button) this.findViewById(R.id.updatePictureButton);
        chooseFromGallery = (Button) this.findViewById(R.id.chooseFromGallery);
        rotateRight = (ImageView) this.findViewById(R.id.rotateRight);
        rotateLeft = (ImageView) this.findViewById(R.id.rotateLeft);
        saveChanges = (Button) this.findViewById(R.id.saveProfileChanges);

        if(!hasCamera()){
            updatePicture.setEnabled(false);
        }
        dbHandler = DBHandler.getInstance(this);
        userLocalStore = new UserLocalStore(this);
        // check if there is a picture in the db taken just now
        // this is necessary because the activity restarts after the camera closes
        String pictureTakenWithTheCamera;
        if((pictureTakenWithTheCamera = dbHandler.getTemp())!=null){
            System.out.println("VLIZAM TUKA DA MU EBA MAIKATA");
            Bitmap pic = StringToBitMap(pictureTakenWithTheCamera);
            profilePicture.setImageBitmap(pic);
            toSave = pic;
        }

        else {
            //get current image from database if there
            User currentUser = userLocalStore.getUserLoggedIn();
            String pic = dbHandler.getProfilePicture(currentUser.getId());
            if (pic != null) {
                Bitmap bitmap = StringToBitMap(pic);
                profilePicture.setImageBitmap(bitmap);
                toSave = bitmap;
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
                profilePicture.setRotation((float) angle);
                Bitmap bm1=((BitmapDrawable)profilePicture.getDrawable()).getBitmap();
                Matrix matrix1 = new Matrix();
                matrix1.postRotate(angle);
                bm1 = Bitmap.createBitmap(bm1, 0, 0, bm1.getWidth(), bm1.getHeight(), matrix1, true);
                toSave = bm1;
                break;
            case R.id.rotateLeft:
                angle = angle-90;
                profilePicture.setRotation((float) angle);
                Bitmap bm2=((BitmapDrawable)profilePicture.getDrawable()).getBitmap();
                Matrix matrix2 = new Matrix();
                matrix2.postRotate(angle);
                bm2 = Bitmap.createBitmap(bm2, 0, 0, bm2.getWidth(), bm2.getHeight(), matrix2, true);
                toSave = bm2;
                break;
            case R.id.saveProfileChanges:
                saveImageToDB();
                break;

        }
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
            dbHandler.setTemp(BitMapToString(image,100));
            fromGallery = false;
        }

        if (requestCode == REQUEST_BROWSE_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri targetUri = data.getData();
            System.out.println(targetUri.toString());
            Bitmap image;
            try {
                image = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(targetUri));
                while (image.getWidth() > 4096 || image.getHeight() > 4096) {
                    image = Bitmap.createScaledBitmap(image,image.getWidth()/2, image.getHeight()/2, true);
                }
                profilePicture.setImageBitmap(image);
                fromGallery = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveImageToDB(){
        User currentUser = userLocalStore.getUserLoggedIn();
        String imageToSave = BitMapToString(toSave, 100);
        currentUser.getId();
        dbHandler.addProfilePicture(currentUser.getId(), imageToSave);
        dbHandler.deleteTemp();

        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.UploadProfilePictureAsyncTask(currentUser.getUsername(), imageToSave, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if (returnedUser != null) {
                    System.out.println("ERROR UPLOADING IMAGE TO SERVER");
                } else
                    Toast.makeText(ActivityProfile.this, "Your profile picture was updated!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    // method to transform image to string
    public String BitMapToString(Bitmap bitmap, int compressFactor){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (fromGallery) compressFactor = 80;
        // shrink the file size of the image - nz kolko da e pomisli si
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressFactor, stream);
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