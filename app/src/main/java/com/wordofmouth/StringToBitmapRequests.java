package com.wordofmouth;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import java.util.ArrayList;

public class StringToBitmapRequests {
    ProgressDialog progressDialog;

    public StringToBitmapRequests(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Fetching images");
        progressDialog.setMessage("Please wait...");
    }

    public void stringToBitmapInBackground(ArrayList<Item> items, GetBitmap getBitmap){
        progressDialog.show();
        new StringToBitmapAsyncTask(items, getBitmap).execute();
    }


    public class StringToBitmapAsyncTask extends AsyncTask<Void, Void, ArrayList<Bitmap>> {
        ArrayList<Item> items;
        GetBitmap getBitmap;
        ArrayList<Bitmap> bitmaps;

        public StringToBitmapAsyncTask(ArrayList<Item> items, GetBitmap getBitmap) {
            this.items = items;
            this.getBitmap = getBitmap;
            bitmaps = new ArrayList<>();
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(Void... params) {
            //final BitmapFactory.Options options = new BitmapFactory.Options();
            for(int i=0;i< items.size();i++){
                if(!items.get(i).get_itemImage().equals("")) {
                    byte[] bytes = Base64.decode(items.get(i).get_itemImage(), Base64.DEFAULT);

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

                    bitmaps.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, outOptions));
                }
                else bitmaps.add(null);
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> result) {
            progressDialog.dismiss();
            getBitmap.done(result);
            super.onPostExecute(result);
        }
    }
}
