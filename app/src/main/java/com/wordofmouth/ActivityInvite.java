package com.wordofmouth;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import java.util.ArrayList;

public class ActivityInvite extends AppCompatActivity implements View.OnClickListener{

    int selectedListId;
    String listName;
    UserLocalStore userLocalStore;
    SearchView searchView;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_invite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        selectedListId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");
        getSupportActionBar().setTitle("Invite people to " + listName);

        searchView = (SearchView) findViewById(R.id.searchUsersField);
        searchButton = (Button) findViewById(R.id.searchUsersButton);
        searchButton.setOnClickListener(this);

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
                        if(returnedUsers!=null) {
                            for (int i = 0; i < returnedUsers.size(); i++) {
                                System.out.println("V INVITE " + returnedUsers.get(i).getId() + " " + returnedUsers.get(i).getName() + " " + returnedUsers.get(i).getUsername());
                            }
                        }
                    }
                });
        }
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
