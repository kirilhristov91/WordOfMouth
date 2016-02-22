package com.wordofmouth.Other;

import android.content.Context;
import android.os.AsyncTask;

import com.wordofmouth.Interfaces.GetItems;
import com.wordofmouth.Interfaces.GetLists;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.List;

import java.util.ArrayList;

public class DBGetData {

    private static DBHandler dbHandler;

    private static DBGetData INSTANCE = null;

    public static synchronized DBGetData getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new DBGetData(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private DBGetData(Context context) {
        this.dbHandler = DBHandler.getInstance(context);
    }

    public void GetItemsInBackground(int listId, GetItems getItems){
        new getItemsAsyncTask(listId, getItems, dbHandler).execute();
    }

    public void GetListsInBackground(String username, int flag, GetLists getLists){
        new getListsAsyncTask(username, flag, getLists, dbHandler).execute();
    }

    private static class getListsAsyncTask extends AsyncTask<Void, Void, ArrayList<List>> {
        String username;
        GetLists getLists;
        ArrayList<List> result;
        int flag;
        DBHandler dbHandler;

        public getListsAsyncTask(String username, int flag, GetLists getLists, DBHandler dbHandler) {
            this.username = username;
            this.getLists = getLists;
            this.dbHandler = dbHandler;
            this.flag = flag;
            result = new ArrayList<>();
        }

        @Override
        protected ArrayList<List> doInBackground(Void... params) {

           /* if(flag == 0) {
                result = dbHandler.getLists(username);
            }
            else {
                result = dbHandler.getSharedLists(username);
            }*/

            //return result;
            return dbHandler.getLists(username, flag);
        }

        @Override
        protected void onPostExecute(ArrayList<List> result) {
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