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
    int tabToreturn;

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
        tabToreturn = intent.getIntExtra("tab", 0);
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
                    showError("Network error! Check your internet connection and try again!");
                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                    progressDialog.show();

                    ServerRequests serverRequests = ServerRequests.getInstance(this);
                    serverRequests.fetchUsersInBackground(searchView.getQuery().toString(), userLocalStore.getUserLoggedIn().getId(), new GetUsers() {
                        @Override
                        public void done(ArrayList<User> returnedUsers) {
                            progressDialog.dismiss();
                            if (returnedUsers.size() > 0) {
                                if (returnedUsers.get(0).getUsername().equals("Timeout")) {
                                    showError("Network error! Check your internet connection and try again!");
                                } else {
                                    display(returnedUsers);
                                }
                            } else
                                Toast.makeText(ActivityInvite.this, "No users were found matching this criteria", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
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
                            showError("Network error! Check your internet connection and try again!");
                        } else {
                            int sharedWithId = users.get(position).getId();
                            int currentUserId = userLocalStore.getUserLoggedIn().getId();

                            final ProgressDialog progressDialog = new ProgressDialog(ActivityInvite.this,R.style.MyTheme);
                            progressDialog.setCancelable(false);
                            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                            progressDialog.show();

                            ServerRequests serverRequests = ServerRequests.getInstance(ActivityInvite.this);
                            serverRequests.inviteInBackground(selectedListId, currentUserId, sharedWithId, new SendInviteResponse() {
                                @Override
                                public void done(String response) {
                                    progressDialog.dismiss();
                                    if (response.equals("That person has already been invited to that list!\n")) {
                                        showError("That person has already been invited to list '" + listName +"'!");
                                    } else if (response.equals("Cannot invite the creator of the list!\n")) {
                                        showError("That is the creator of the list '" + listName + "'! He/She already has acces to this list!");
                                    } else if (response.equals("Timeout")) {
                                        showError("Network error! Check your internet connection and try again!");
                                    } else {
                                        Intent myIntent = new Intent(ActivityInvite.this, ActivityItemsOfAList.class);
                                        myIntent.putExtra("listId", selectedListId);
                                        myIntent.putExtra("name", listName);
                                        myIntent.putExtra("tab", tabToreturn);
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

    private void showError(String message){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage(message);
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActivityItemsOfAList.class);
        intent.putExtra("listId", selectedListId);
        intent.putExtra("name", listName);
        intent.putExtra("tab", tabToreturn);
        startActivity(intent);
        finish();
    }
}
