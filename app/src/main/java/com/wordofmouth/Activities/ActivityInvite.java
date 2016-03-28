package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetResponse;
import com.wordofmouth.Interfaces.GetUsers;
import com.wordofmouth.Other.Utilities;
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
    int userClicked;
    RelativeLayout inviteLayout;
    Utilities utilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_invite);

        // get data from intent extra
        Intent intent = getIntent();
        selectedListId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");
        tabToreturn = intent.getIntExtra("tab", 0);
        // set the list name in the Action bar
        getSupportActionBar().setTitle("Share list: " + listName);

        userLocalStore = UserLocalStore.getInstance(this);
        utilities = Utilities.getInstance(this);

        // link GUI elements and set listeners
        searchView = (SearchView) findViewById(R.id.searchUsersField);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.onActionViewExpanded();
            }
        });
        searchView.setQueryHint("Search users");
        searchView.setIconified(false);

        searchButton = (Button) findViewById(R.id.searchUsersButton);
        searchButton.setOnClickListener(this);

        fetchedUserList = (ListView) findViewById(R.id.fetchedUsersList);

        inviteLayout = (RelativeLayout) findViewById(R.id.inviteLayout);
        inviteLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchUsersButton:
                searchView.clearFocus();
                hideKeyboard(v);
                searchView.clearFocus();
                // check for network
                if (!isNetworkAvailable()) {
                    showError("Network error! Check your internet connection and try again!");
                } else {
                    // show progress dialog
                    final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                    progressDialog.show();

                    // get the users mathching the entered text in the search box
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
                            } else {
                                Toast.makeText(ActivityInvite.this, "No users were found matching this criteria", Toast.LENGTH_SHORT).show();
                                display(returnedUsers);
                            }
                        }
                    });
                }
                break;
        }
    }

    // displays the fetched from the server users
    public void display(ArrayList<User> returnedUsers){
        users = returnedUsers;
        usernames = new String[users.size()];
        for(int i =0; i< users.size();i++){
            usernames[i] = users.get(i).getUsername();
        }

        ArrayAdapter<String> userAdapter = new CustomUserRowAdapter(ActivityInvite.this, usernames, users);
        fetchedUserList.setAdapter(userAdapter);
        if (usernames.length == 0){
            userAdapter.clear();
        }
        // set listener to the listView of users
        fetchedUserList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        userClicked = position;
                        // show confirmation dialog when a card containing a user is clicked
                        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityInvite.this);
                        allertBuilder.setMessage("Are you sure you want to share the list with that user?");
                        allertBuilder.setCancelable(false);

                        allertBuilder.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // check for network
                                if (!isNetworkAvailable()) {
                                    showError("Network error! Check your internet connection and try again!");
                                } else {
                                    int sharedWithId = users.get(userClicked).getId();
                                    int currentUserId = userLocalStore.getUserLoggedIn().getId();

                                    // show progress dialog
                                    final ProgressDialog progressDialog = new ProgressDialog(ActivityInvite.this, R.style.MyTheme);
                                    progressDialog.setCancelable(false);
                                    progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                                    progressDialog.show();

                                    // send invitation to the selected user - stores on the server the id of the list and the ids of the two users
                                    ServerRequests serverRequests = ServerRequests.getInstance(ActivityInvite.this);
                                    serverRequests.inviteInBackground(selectedListId, currentUserId, sharedWithId, new GetResponse() {
                                        @Override
                                        public void done(String response) {
                                            progressDialog.dismiss();
                                            if (response.equals("That person has already been invited to that list!")) {
                                                showError("That person has already been invited to list '" + listName + "'!");
                                            } else if (response.equals("Cannot invite the creator of the list!")) {
                                                showError("That is the creator of the list '" + listName + "'! He/She already has acces to this list!");
                                            } else if (response.equals("Timeout")) {
                                                showError("Network error! Check your internet connection and try again!");
                                            } else {
                                                Toast.makeText(ActivityInvite.this, "The selected user was invited to the current list", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });

                        allertBuilder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        allertBuilder.create().show();
                    }
                }
        );
    }
}
