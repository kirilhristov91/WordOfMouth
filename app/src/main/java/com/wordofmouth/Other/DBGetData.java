package com.wordofmouth.Other;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.wordofmouth.Interfaces.GetItems;
import com.wordofmouth.Interfaces.GetLists;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.MyList;

import java.util.ArrayList;

public class DBGetData {

    private static DBHandler dbHandler;
    private static ProgressDialog progressDialog;

    public DBGetData(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Fetching data from database");
        progressDialog.setMessage("Please wait...");
        this.dbHandler = DBHandler.getInstance(context);
    }

    public void GetItemsInBackground(int listId, GetItems getItems){
        progressDialog.show();
        new getItemsAsyncTask(listId, getItems).execute();
    }

    public void GetListsInBackground(String username, GetLists getLists){
        progressDialog.show();
        new getListsAsyncTask(username, getLists).execute();
    }

    private static class getListsAsyncTask extends AsyncTask<Void, Void, ArrayList<MyList>> {
        String username;
        GetLists getLists;
        ArrayList<MyList> result;

        public getListsAsyncTask(String username, GetLists getLists) {
            this.username = username;
            this.getLists = getLists;
            result = new ArrayList<>();
        }

        @Override
        protected ArrayList<MyList> doInBackground(Void... params) {

            result = dbHandler.getLists(username);
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<MyList> result) {
            progressDialog.dismiss();
            getLists.done(result);
            super.onPostExecute(result);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    private static class getItemsAsyncTask extends AsyncTask<Void, Void, ArrayList<Item>> {
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