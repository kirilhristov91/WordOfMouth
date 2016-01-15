package com.wordofmouth;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class DBGetItems {

    DBHandler dbHandler;
    ProgressDialog progressDialog;

    public DBGetItems(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Fetching data from database");
        progressDialog.setMessage("Please wait...");
        this.dbHandler = DBHandler.getInstance(context);
    }

    public void GetItemsInBackground(int listId, GetItems getItems){
        new getItemsAsyncTask(listId, getItems).execute();
    }

    public class getItemsAsyncTask extends AsyncTask<Void, Void, ArrayList<Item>> {
        int listId;
        GetItems getItems;
        ArrayList<Item> result;

        public getItemsAsyncTask(int listId, GetItems getItems) {
            this.listId = listId;
            this.getItems = getItems;
            result = new ArrayList<>();
        }

        @Override
        protected ArrayList<Item> doInBackground(Void... params) {
            result = dbHandler.getItems(listId);
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> result) {
            progressDialog.dismiss();
            getItems.done(result);
            super.onPostExecute(result);
        }
    }
}
