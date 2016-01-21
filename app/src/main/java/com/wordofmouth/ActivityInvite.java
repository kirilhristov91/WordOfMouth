package com.wordofmouth;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class ActivityInvite extends AppCompatActivity implements View.OnClickListener{

    int selectedListId;
    String listName;
    UserLocalStore userLocalStore;
    SearchView searchView;
    Button searchButton;
    ListView fetchedUserList;
    ArrayList<User> users;
    String[] usernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_invite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        selectedListId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");
        getSupportActionBar().setTitle("Invite people to: " + listName);

        searchView = (SearchView) findViewById(R.id.searchUsersField);
        searchButton = (Button) findViewById(R.id.searchUsersButton);
        searchButton.setOnClickListener(this);

        fetchedUserList = (ListView) findViewById(R.id.fetchedUsersList);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.searchUsersButton:
                //TODO prashtai i id-to za da ne vyrne syshtiq user (ko she se invite-va sebe si)
                ServerRequests serverRequests = new ServerRequests(this);
                System.out.println("GETQYERY TO STRING" + searchView.getQuery().toString());
                serverRequests.fetchUsersInBackground(searchView.getQuery().toString(), new GetUsers() {
                    @Override
                    public void done(ArrayList<User> returnedUsers) {
                        if(returnedUsers.size()>0) {
                            display(returnedUsers);
                        }
                        else Toast.makeText(ActivityInvite.this, "No users were found matching this criteria", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }


    public void display(ArrayList<User> returnedUsers){
        users = returnedUsers;
        usernames = new String[users.size()];
        for(int i =0; i< users.size();i++){
            usernames[i] = users.get(i).getUsername();
        }

        ArrayAdapter<String> userAdapter = new CustomUserRowAdapter(ActivityInvite.this, usernames, users);
        fetchedUserList.setAdapter(userAdapter);
        fetchedUserList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        /*String itemName = String.valueOf(parent.getItemAtPosition(position));
                        int itemIdClicked = items.get(position).get_itemId();
                        Intent myIntent = new Intent(ActivityItemsOfAList.this, ActivityItem.class);
                        myIntent.putExtra("listId", selectedListId);
                        myIntent.putExtra("listName", listName);
                        myIntent.putExtra("itemId", itemIdClicked);
                        myIntent.putExtra("itemName", itemName);
                        startActivity(myIntent);
                        finish();*/
                    }
                }
        );
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActivityItemsOfAList.class);
        intent.putExtra("listId", selectedListId);
        intent.putExtra("name", listName);
        startActivity(intent);
        finish();
    }
}
