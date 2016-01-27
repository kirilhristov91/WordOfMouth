package com.wordofmouth.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetUsers;
import com.wordofmouth.Interfaces.SendInviteResponse;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.SharedPreferences.UserLocalStore;

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

        userLocalStore = new UserLocalStore(this);

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
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

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
                        // list id, sharedWithId

                        int sharedWithId = users.get(position).getId();
                        int currentUserId = userLocalStore.getUserLoggedIn().getId();
                        ServerRequests serverRequests = new ServerRequests(ActivityInvite.this);
                        serverRequests.inviteInBackground(selectedListId, currentUserId, sharedWithId, new SendInviteResponse() {
                            @Override
                            public void done(String response) {
                                System.out.println("Otgovoryt na invite e : " + response);
                                if(response.equals("That person has already been invited to that list!\n")){
                                    showError();
                                }
                                else{
                                    Intent myIntent = new Intent(ActivityInvite.this, ActivityItemsOfAList.class);
                                    myIntent.putExtra("listId", selectedListId);
                                    myIntent.putExtra("name", listName);
                                    startActivity(myIntent);
                                    finish();
                                }
                            }
                        });
                    }
                }
        );
    }

    private void showError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("That person has already been invited to list '" + listName +"'!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
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
