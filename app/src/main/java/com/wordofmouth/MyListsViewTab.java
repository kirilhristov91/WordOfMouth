package com.wordofmouth;

import android.content.Intent;
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

import java.util.ArrayList;

public class MyListsViewTab extends Fragment implements View.OnClickListener{

    TextView createList;
    //Button logoutButton;
    //UserLocalStore userLocalStore;
    ListView myListView;
    ArrayList<MyList> myLists;
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_lists_view_tab, container, false);

        mainActivity = (MainActivity) getActivity();
        createList = (TextView) v.findViewById(R.id.createListText);
        myListView = (ListView) v.findViewById(R.id.myListsListView);
        myLists = new ArrayList<MyList>();

        myLists = mainActivity.getMyLists();
        String[] listNames = new String[myLists.size()];
        for (int i = 0; i < myLists.size(); i++) {
            listNames[i] = myLists.get(i).get_name();
        }

        ArrayAdapter<String> MyListsAdapter =
                new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, listNames);
        myListView.setAdapter(MyListsAdapter);

        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String text = "";
                        int idClicked;
                        String list = String.valueOf(parent.getItemAtPosition(position));
                        idClicked = myLists.get(position).get_listId();
                        Intent myIntent = new Intent(mainActivity, ItemsOfAListView.class);
                        myIntent.putExtra("listId", idClicked);
                        myIntent.putExtra("name", list);
                        startActivity(myIntent);
                    }
                }
        );

        createList.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            /*case R.id.logoutButton:
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);

                startActivity(new Intent(this, Login.class));
                break;*/
            case R.id.createListText:
                startActivity(new Intent(mainActivity, AddListView.class));
                break;
        }
    }

}
