package com.wordofmouth.Other;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.wordofmouth.Interfaces.GetBitmap;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.List;

import java.util.ArrayList;

public class StringToBitmapRequests {

    // a class to transfrom String represented lists or items images into Bitmap objects in the background

    private static StringToBitmapRequests INSTANCE;
    private static Utilities utilities;

    public static synchronized StringToBitmapRequests getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new StringToBitmapRequests(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private StringToBitmapRequests(Context context){
        utilities = Utilities.getInstance(context);
    }

    public void stringToBitmapInBackground(ArrayList<Item> items, GetBitmap getBitmap){
        new StringToBitmapAsyncTask(items, getBitmap).execute();
    }

    public void ListsStringToBitmapInBackground(ArrayList<List> lists, GetBitmap getBitmap){
        new ListStringToBitmapAsyncTask(lists, getBitmap).execute();
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    private static class ListStringToBitmapAsyncTask extends AsyncTask<Void, Void, ArrayList<Bitmap>> {
        ArrayList<List> lists;
        GetBitmap getBitmap;
        ArrayList<Bitmap> bitmaps;

        public ListStringToBitmapAsyncTask(ArrayList<List> lists, GetBitmap getBitmap) {
            this.lists = lists;
            this.getBitmap = getBitmap;
            bitmaps = new ArrayList<>();
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(Void... params) {
            //final BitmapFactory.Options options = new BitmapFactory.Options();
            for(int i=0;i< lists.size();i++){
                if(!lists.get(i).getImage().equals("")) {
                    bitmaps.add(utilities.StringToBitMap(lists.get(i).getImage(), 100, 100));
                }
                else bitmaps.add(null);
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> result) {
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
                    bitmaps.add(utilities.StringToBitMap(items.get(i).get_itemImage(), 100, 100));
                }
                else bitmaps.add(null);
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> result) {
            getBitmap.done(result);
            super.onPostExecute(result);
        }
    }
}