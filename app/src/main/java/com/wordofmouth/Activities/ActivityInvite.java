package com.wordofmouth.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

public class ActivityInvite extends BaseActivity implements View.OnClickListener{

    int selectedListId;
    String listName;
    SearchView searchView;
    Button searchButton;
    ListView fetchedUserList;
    ArrayList<User> users;
    String[] usernames;
    private UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_activity_invite);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);*/
        userLocalStore = UserLocalStore.getInstance(this);
        Intent intent = getIntent();
        selectedListId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");
        getSupportActionBar().setTitle("Invite to: " + listName);

        searchView = (SearchView) findViewById(R.id.searchUsersField);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.onActionViewExpanded();
            }
        });

        searchButton = (Button) findViewById(R.id.searchUsersButton);
        searchButton.setOnClickListener(this);

        fetchedUserList = (ListView) findViewById(R.id.fetchedUsersList);

    }

    // TODO set an alertDialog to ask if the user is sure he wants to invite the selected user
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchUsersButton:
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                if (!isNetworkAvailable()) {
                    showConnectionError();
                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Processing");
                    progressDialog.setMessage("Fetching users matching the name or username you entered...");
                    progressDialog.show();

                    ServerRequests serverRequests = ServerRequests.getInstance(this);
                    serverRequests.fetchUsersInBackground(searchView.getQuery().toString(), userLocalStore.getUserLoggedIn().getId(), new GetUsers() {
                        @Override
                        public void done(ArrayList<User> returnedUsers) {
                            progressDialog.dismiss();
                            if (returnedUsers.size() > 0) {
                                if (returnedUsers.get(0).getUsername().equals("Timeout")) {
                                    showConnectionError();
                                } else {
                                    display(returnedUsers);
                                }
                            } else
                                Toast.makeText(ActivityInvite.this, "No users were found matching this criteria", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void display(ArrayList<User> returnedUsers){
        users = returnedUsers;
        usernames = new String[users.size()];
        for(int i =0; i< users.size();i++){
            usernames[i] = users.get(i).getUsername();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        ArrayAdapter<String> userAdapter = new CustomUserRowAdapter(ActivityInvite.this, usernames, users);
        fetchedUserList.setAdapter(userAdapter);
        fetchedUserList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // list id, sharedWithId

                        if (!isNetworkAvailable()) {
                            showConnectionError();
                        } else {
                            int sharedWithId = users.get(position).getId();
                            int currentUserId = userLocalStore.getUserLoggedIn().getId();

                            final ProgressDialog progressDialog2 = new ProgressDialog(ActivityInvite.this);
                            progressDialog2.setCancelable(false);
                            progressDialog2.setTitle("Processing");
                            progressDialog2.setMessage("Inviting the selected user to the current list");
                            progressDialog2.show();

                            ServerRequests serverRequests = ServerRequests.getInstance(ActivityInvite.this);
                            serverRequests.inviteInBackground(selectedListId, currentUserId, sharedWithId, new SendInviteResponse() {
                                @Override
                                public void done(String response) {
                                    progressDialog2.dismiss();
                                    System.out.println("Otgovoryt na invite e : " + response);
                                    if (response.equals("That person has already been invited to that list!\n")) {
                                        showError();
                                    }
                                    else if (response.equals("Cannot invite the creator of the list!\n")){
                                        showCreatorError();
                                    }
                                    else if (response.equals("Timeout")) {
                                        showConnectionError();
                                    } else {
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
                }
        );
    }

    private void showConnectionError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityInvite.this);
        allertBuilder.setMessage("Network error! Check your internet connection and try again!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("That person has already been invited to list '" + listName +"'!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showCreatorError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("That is the creator of the list '" + listName +"'! He/She already has acces to this list!");
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
