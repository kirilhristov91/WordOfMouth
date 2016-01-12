package com.wordofmouth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.content.pm.PackageManager;

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
    String mCurrentPhotoPath;
    int angle = 0;


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


        if(!hasCamera()){
            updatePicture.setEnabled(false);
        }

        rotateRight.setOnClickListener(this);
        rotateLeft.setOnClickListener(this);
        updatePicture.setOnClickListener(this);
        chooseFromGallery.setOnClickListener(this);
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

                break;
            case R.id.rotateLeft:
                angle = angle-90;
                profilePicture.setRotation((float) angle);
                break;

        }
    }


    //check if the user has a camera
    private boolean hasCamera(){
        PackageManager pm = mainActivity.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    public void browseGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_BROWSE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            profilePicture.setImageBitmap(photo);

            System.out.println(resultCode + " SLOJIH SNIMKATA BE");
        }

        if (requestCode == REQUEST_BROWSE_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri targetUri = data.getData();
            System.out.println(targetUri.toString());
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(mainActivity.getContentResolver().openInputStream(targetUri));
                profilePicture.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
