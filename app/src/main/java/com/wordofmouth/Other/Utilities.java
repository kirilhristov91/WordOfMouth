package com.wordofmouth.Other;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utilities {

    private static Utilities INSTANCE;
    private Context context;

    public static synchronized Utilities getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new Utilities(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private Utilities(Context context) {
        this.context = context;
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    public Bitmap StringToBitMap(String encodedString, int desiredWidth, int desiredHeight){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, scaleOptions);

        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= desiredWidth
                && scaleOptions.outHeight / scale / 2 >= desiredHeight) {
            scale *= 2;
        }

        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, outOptions);
    }

    public Bitmap getBitmapFromURI(Uri targetUri, int width, int height){
        Bitmap image = null;
        try {
            image = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(targetUri));

            int desiredWidth = image.getWidth();
            int desiredHeight = image.getHeight();
            while(desiredWidth/2 >= width || desiredHeight/2 >= height){
                desiredWidth = desiredWidth/2;
                desiredHeight = desiredHeight/2;
            }

            image = Bitmap.createScaledBitmap(image,desiredWidth, desiredHeight, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return image;
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

    public Bitmap rotate(Bitmap b, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }
}
