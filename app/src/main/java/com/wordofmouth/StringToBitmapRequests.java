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
        progressDialog.setTitle("Processing");
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
            bitmaps = new ArrayList<Bitmap>();
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(Void... params) {
            for(int i=0;i< items.size();i++){
                String asd = items.get(i).get_itemImage();
                System.out.println("VLIZAM TUKA BE V DO IN BACKGROUND");
                byte[] bytes = Base64.decode(items.get(i).get_itemImage(), Base64.DEFAULT);
                bitmaps.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            System.out.println("VLIZAM TUKA BE V DO IN ONPOSTEXECUTE");
            progressDialog.dismiss();
            getBitmap.done(result);
            super.onPostExecute(result);
        }
    }
}
