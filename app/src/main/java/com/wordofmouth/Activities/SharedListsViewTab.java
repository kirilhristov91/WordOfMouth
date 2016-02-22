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

public class SharedListsViewTab extends Fragment {

    MainActivity mainActivity;
    ListView sharedListView;
    ArrayList<List> sharedLists;
    ArrayList <Shared> usernames;
    DBGetData dbGetData;
    String username;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.shared_lists_view_tab,container,false);

        mainActivity = (MainActivity) getActivity();
        sharedListView = (ListView) v.findViewById(R.id.sharedListsListView);
        username = UserLocalStore.getInstance(mainActivity).getUserLoggedIn().getUsername();
        sharedLists = new ArrayList<List>();
        dbGetData = DBGetData.getInstance(mainActivity);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // get the user`s lists to display on fragment
        final ProgressDialog progressDialogFetching = new ProgressDialog(mainActivity,R.style.MyTheme);
        progressDialogFetching.setCancelable(false);
        progressDialogFetching.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        progressDialogFetching.show();

        dbGetData.GetListsInBackground(username, 1, new GetLists() {
            @Override
            public void done(ArrayList<List> lists) {
                progressDialogFetching.dismiss();
                display(lists);
            }
        });
    }

    public void display(ArrayList<List> lists){

        sharedLists = lists;
        final String[] listNames = new String[sharedLists.size()];
        for (int i = 0; i < sharedLists.size(); i++) {
            listNames[i] = sharedLists.get(i).get_name();
        }

        DBHandler dbHandler = DBHandler.getInstance(mainActivity);
        usernames = dbHandler.getUsernames();

        StringToBitmapRequests stbr = StringToBitmapRequests.getInstance(mainActivity);
        final ProgressDialog progressDialogShared = new ProgressDialog(mainActivity,R.style.MyTheme);
        progressDialogShared.setCancelable(false);
        progressDialogShared.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        progressDialogShared.show();
        stbr.ListsStringToBitmapInBackground(sharedLists, new GetBitmap() {
            @Override
            public void done(ArrayList<Bitmap> resultShared) {
                progressDialogShared.dismiss();
                ArrayAdapter<String> listAdapterShared =
                        new CustomListRowAdapter(mainActivity, listNames, sharedLists, resultShared, usernames);
                sharedListView.setAdapter(listAdapterShared);
            }
        });

        sharedListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int idClicked;
                        String list = String.valueOf(parent.getItemAtPosition(position));
                        idClicked = sharedLists.get(position).get_listId();
                        Intent myIntent = new Intent(mainActivity, ActivityItemsOfAList.class);
                        myIntent.putExtra("listId", idClicked);
                        myIntent.putExtra("name", list);
                        myIntent.putExtra("tab", 1);
                        startActivity(myIntent);
                        mainActivity.finish();
                    }
                }
        );
    }

}
