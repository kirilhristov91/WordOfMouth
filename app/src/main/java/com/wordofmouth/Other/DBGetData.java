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

    private DBHandler dbHandler;

    public DBGetData() {
        this.dbHandler = DBHandler.getDBHandlerForAsyncTask();
    }

    public void GetItemsInBackground(int listId, GetItems getItems){
        new getItemsAsyncTask(listId, getItems, dbHandler).execute();
    }

    public void GetListsInBackground(String username, GetLists getLists){
        new getListsAsyncTask(username, getLists, dbHandler).execute();
    }

    private static class getListsAsyncTask extends AsyncTask<Void, Void, ArrayList<MyList>> {
        String username;
        GetLists getLists;
        ArrayList<MyList> result;
        DBHandler dbHandler;

        public getListsAsyncTask(String username, GetLists getLists, DBHandler dbHandler) {
            this.username = username;
            this.getLists = getLists;
            this.dbHandler = dbHandler;
            result = new ArrayList<>();
        }

        @Override
        protected ArrayList<MyList> doInBackground(Void... params) {

            result = dbHandler.getLists(username);
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<MyList> result) {
            getLists.done(result);
            super.onPostExecute(result);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    private static class getItemsAsyncTask extends AsyncTask<Void, Void, ArrayList<Item>> {
        int listId;
        GetItems getItems;
        ArrayList<Item> result;
        DBHandler dbHandler;


        public getItemsAsyncTask(int listId, GetItems getItems, DBHandler dbHandler) {
            this.listId = listId;
            this.getItems = getItems;
            this.dbHandler = dbHandler;
            result = new ArrayList<>();
        }

        @Override
        protected ArrayList<Item> doInBackground(Void... params) {
            result = dbHandler.getItems(listId);
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> result) {
            getItems.done(result);
            super.onPostExecute(result);
        }
    }
}