package com.wordofmouth.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wordofmouth.Interfaces.GetBitmap;
import com.wordofmouth.Interfaces.GetLists;
import com.wordofmouth.ObjectClasses.List;
import com.wordofmouth.ObjectClasses.Shared;
import com.wordofmouth.Other.DBGetData;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.StringToBitmapRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.util.ArrayList;

public class MyListsViewTab extends Fragment implements View.OnClickListener{

    TextView createList, noMyListsYet;
    ListView myListView;
    MainActivity mainActivity;
    ArrayList<List> myLists;
    String username;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_lists_tab, container, false);

        mainActivity = (MainActivity) getActivity();
        createList = (TextView) v.findViewById(R.id.createListText);
        myListView = (ListView) v.findViewById(R.id.myListsListView);
        createList.setOnClickListener(this);
        noMyListsYet = (TextView) v.findViewById(R.id.noMyListsYet);
        username = UserLocalStore.getInstance(mainActivity).getUserLoggedIn().getUsername();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // get the user`s lists in background to display on fragment

        DBGetData dbGetData = DBGetData.getInstance(mainActivity);
        // show progress dialog in the meantime
        final ProgressDialog progressDialogFetching = new ProgressDialog(mainActivity,R.style.MyTheme);
        progressDialogFetching.setCancelable(false);
        progressDialogFetching.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        progressDialogFetching.show();

        dbGetData.GetListsInBackground(username, 0, new GetLists() {
            @Override
            public void done(ArrayList<List> lists) {
                progressDialogFetching.dismiss();
                display(lists);
            }
        });
    }

    // display the obtained lists
    public void display(ArrayList<List> lists){

        myLists = lists;

        if(myLists.size()>0) {
            // remove the no lists text if lists created by the user exist
            noMyListsYet.setVisibility(View.INVISIBLE);

            final String[] listNames = new String[myLists.size()];
            for (int i = 0; i < myLists.size(); i++) {
                listNames[i] = myLists.get(i).get_name();
            }

            // get the usernames of the people the list is shared with
            DBHandler dbHandler = DBHandler.getInstance(mainActivity);
            final ArrayList<Shared> usernames = dbHandler.getUsernames();

            // show progress dialog during image(s) manupulation
            StringToBitmapRequests stbr = StringToBitmapRequests.getInstance(mainActivity);
            final ProgressDialog progressDialog = new ProgressDialog(mainActivity, R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            progressDialog.show();

            // transform the images into Bitmap objects in background
            stbr.ListsStringToBitmapInBackground(myLists, new GetBitmap() {
                @Override
                public void done(ArrayList<Bitmap> result) {
                    progressDialog.dismiss();
                    ArrayAdapter<String> listAdapter =
                            new CustomListRowAdapter(mainActivity, listNames, myLists, result, usernames);
                    myListView.setAdapter(listAdapter);
                }
            });

            // set the onItemClick listener and link to the Items of a List page
            myListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            int idClicked;
                            String list = String.valueOf(parent.getItemAtPosition(position));
                            idClicked = myLists.get(position).get_listId();
                            Intent myIntent = new Intent(mainActivity, ActivityItemsOfAList.class);
                            myIntent.putExtra("listId", idClicked);
                            myIntent.putExtra("name", list);
                            myIntent.putExtra("tab", 0);
                            startActivity(myIntent);
                            mainActivity.finish();
                        }
                    }
            );
        }
    }


    // set the link to the Create List page
    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.createListText:
                startActivity(new Intent(mainActivity, ActivityAddList.class));
                break;
        }
    }

}
