package com.wordofmouth.Other;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import com.wordofmouth.Interfaces.GetBitmap;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.MyList;

import java.util.ArrayList;

public class StringToBitmapRequests {
    private static ProgressDialog progressDialog;

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

    public void ListsStringToBitmapInBackground(ArrayList<MyList> lists, GetBitmap getBitmap){
        progressDialog.show();
        new ListStringToBitmapAsyncTask(lists, getBitmap).execute();
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    private static class ListStringToBitmapAsyncTask extends AsyncTask<Void, Void, ArrayList<Bitmap>> {
        ArrayList<MyList> lists;
        GetBitmap getBitmap;
        ArrayList<Bitmap> bitmaps;

        public ListStringToBitmapAsyncTask(ArrayList<MyList> lists, GetBitmap getBitmap) {
            this.lists = lists;
            this.getBitmap = getBitmap;
            bitmaps = new ArrayList<>();
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(Void... params) {
            //final BitmapFactory.Options options = new BitmapFactory.Options();
            for(int i=0;i< lists.size();i++){
                if(!lists.get(i).getImage().equals("")) {
                    byte[] bytes = Base64.decode(lists.get(i).getImage(), Base64.DEFAULT);

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

    ////////////////////////////////////////////////////////////////////////////////////////
    private static class StringToBitmapAsyncTask extends AsyncTask<Void, Void, ArrayList<Bitmap>> {
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