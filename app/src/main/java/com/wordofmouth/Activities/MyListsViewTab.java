package com.wordofmouth.Activities;

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
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.Other.DBGetData;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.StringToBitmapRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.util.ArrayList;

public class MyListsViewTab extends Fragment implements View.OnClickListener{

    TextView createList;
    ListView myListView;
    MainActivity mainActivity;
    ArrayList<MyList> myLists;
    UserLocalStore userLocalStore;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_lists_view_tab, container, false);

        mainActivity = (MainActivity) getActivity();
        createList = (TextView) v.findViewById(R.id.createListText);
        myListView = (ListView) v.findViewById(R.id.myListsListView);
        createList.setOnClickListener(this);

        userLocalStore = new UserLocalStore(mainActivity);
        String username = userLocalStore.getUserLoggedIn().getUsername();
        // get the user`s lists to display on fragment
        myLists = new ArrayList<MyList>();

        DBGetData dbGetData = new DBGetData(mainActivity);
        dbGetData.GetListsInBackground(username, new GetLists() {
            @Override
            public void done(ArrayList<MyList> lists) {
                System.out.println("KOLKO LISTA IMA V BAZATA " + lists.size());
                display(lists);
            }
        });
        return v;
    }

    public void display(ArrayList<MyList> lists){

        myLists = lists;
        System.out.println("size of mylists " + myLists.size());
        final String[] listNames = new String[myLists.size()];
        for (int i = 0; i < myLists.size(); i++) {
            listNames[i] = myLists.get(i).get_name();
            //System.out.println(myLists.get(i).get_listId());
        }

        StringToBitmapRequests stbr = new StringToBitmapRequests(mainActivity);
        stbr.ListsStringToBitmapInBackground(myLists, new GetBitmap() {
            @Override
            public void done(ArrayList<Bitmap> result) {
                /*final Runtime runtime = Runtime.getRuntime();
                final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
                final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
                System.out.println("Total heap size: " + maxHeapSizeInMB + " MB");
                System.out.println("Available heap size: " + usedMemInMB + " MB");*/

                ArrayAdapter<String> listAdapter =
                        new CustomListRowAdapter(mainActivity, listNames, myLists, result);
                myListView.setAdapter(listAdapter);
            }
        });

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
                        startActivity(myIntent);
                        mainActivity.finish();
                    }
                }
        );
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.createListText:
                startActivity(new Intent(mainActivity, ActivityAddList.class));
                break;
        }
    }

}
