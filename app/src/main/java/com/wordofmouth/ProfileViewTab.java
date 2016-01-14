package com.wordofmouth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ProfileViewTab extends Fragment implements View.OnClickListener{

    MainActivity mainActivity;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_BROWSE_GALLERY = 2;
    ImageView profilePicture;
    ImageView rotateRight;
    ImageView rotateLeft;
    Button updatePicture;
    Button chooseFromGallery;
    Button saveChanges;
    int angle = 0;

    DBHandler dbHandler;
    Bitmap toSave;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_view_tab,container,false);

        mainActivity = (MainActivity) getActivity();
        profilePicture = (ImageView) v.findViewById(R.id.profilePicture);
        updatePicture = (Button) v.findViewById(R.id.updatePictureButton);
        chooseFromGallery = (Button) v.findViewById(R.id.chooseFromGallery);
        rotateRight = (ImageView) v.findViewById(R.id.rotateRight);
        rotateLeft = (ImageView) v.findViewById(R.id.rotateLeft);
        saveChanges = (Button) v.findViewById(R.id.saveProfileChanges);

        dbHandler = DBHandler.getInstance(mainActivity);

        if(!hasCamera()){
            updatePicture.setEnabled(false);
        }

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
            User currentUser = mainActivity.userLocalStore.getUserLoggedIn();
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
        return v;
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
        PackageManager pm = mainActivity.getPackageManager();
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
                image = BitmapFactory.decodeStream(mainActivity.getContentResolver().openInputStream(targetUri));
                //image = fixOrientation(image);
                toSave = image;
                profilePicture.setImageBitmap(image);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveImageToDB(){
        User currentUser = mainActivity.userLocalStore.getUserLoggedIn();
        String imageToSave = BitMapToString(toSave);
        System.out.println("KOLKO E GOLQM STRINGA " + imageToSave.length());
        currentUser.getId();
        dbHandler.addProfilePicture(currentUser.getId(), imageToSave);
        dbHandler.deleteTemp();
        Toast.makeText(mainActivity, "Your profile picture was updated!", Toast.LENGTH_SHORT).show();

    }

    // method to transform image to string
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // shrink the file size of the image - nz kolko da e pomisli si
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    // method to transform string to image
    public Bitmap StringToBitMap(String encodedString){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


/*
    File createImageFile() throws IOException {

        String timeStamp = SimpleDateFormat.getDateInstance().toString();
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName,".jpg", storageDirectory);
        mImageFileLocation = image.getAbsolutePath();

        return image;
    }

    void setReducedImageSize() {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        Bitmap photoReducedSizeBitmp = BitmapFactory.decodeFile(mImageFileLocation, bmOptions);
        profilePicture.setImageBitmap(photoReducedSizeBitmp);
    }*/
}
