package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetItems;
import com.wordofmouth.Interfaces.GetListId;
import com.wordofmouth.Interfaces.GetUsernames;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.ObjectClasses.Notification;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.util.ArrayList;

public class ActivityNotifications extends BaseActivity {

    ArrayList<Notification> notifications;
    ListView notificationItemListView;
    int notificationId;
    DBHandler dbHandler;
    ServerRequests serverRequests;
    int listId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_notifications);

        dbHandler = DBHandler.getInstance(this);
        serverRequests = ServerRequests.getInstance(this);

        notifications = dbHandler.getNotifications();
        String[] messages = new String[notifications.size()];
        for(int i = 0; i<notifications.size();i++){
            messages[i] = notifications.get(i).getMsg();
        }

        notificationItemListView = (ListView) findViewById(R.id.notificationListView);
        ArrayAdapter<String> notificationAdapter =
                new CustomNotificationRowAdapter(ActivityNotifications.this, messages, notifications);
        notificationItemListView.setAdapter(notificationAdapter);

        notificationItemListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Toast.makeText(ActivityNotifications.this, notifications.get(position).getMsg(), Toast.LENGTH_SHORT).show();

                        if (notifications.get(position).getAccepted() == 1) {
                            showAlreadyAccepted();
                        } else {
                            notificationId = notifications.get(position).getId();
                            showConfirmationDialog(notifications.get(position));
                        }
                    }
                }
        );

    }

    private void showConfirmationDialog(Notification notification){
        final Notification n = notification;
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Do you accept the invitation? In case you do the list you have been invited and all its items will be downloaded on your phone.");
        allertBuilder.setCancelable(false);

        allertBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!isNetworkAvailable()) {
                    showConnectionError();
                } else {
                    UserLocalStore userLocalStore = UserLocalStore.getInstance(ActivityNotifications.this);
                    final ProgressDialog progressDialogDownloadList = new ProgressDialog(ActivityNotifications.this, R.style.MyTheme);
                    progressDialogDownloadList.setCancelable(false);
                    progressDialogDownloadList.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                    progressDialogDownloadList.show();
                    serverRequests.downloadListInBackgroudn(n.getListId(), userLocalStore.getUserLoggedIn().getId(), new GetListId() {
                        @Override
                        public void done(MyList myList) {
                            progressDialogDownloadList.dismiss();
                            if (myList.get_username().equals("Timeout")) {
                                showConnectionError();
                            } else if (myList.get_username().equals("UpdError")) {
                                showServerError();
                            } else {
                                DBHandler dbHandler = DBHandler.getInstance(ActivityNotifications.this);
                                myList.setHasNewContent(1);
                                dbHandler.addList(myList);
                                downloadItems(myList);
                            }
                        }
                    });

                }
            }
        });

        allertBuilder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ActivityNotifications.this, "DECLINED", Toast.LENGTH_SHORT).show();
            }
        });
        allertBuilder.create().show();
    }

    private void downloadItems(MyList list){

        listId = list.get_listId();

        final ProgressDialog progressDialogDownloadItem = new ProgressDialog(ActivityNotifications.this,R.style.MyTheme);
        progressDialogDownloadItem.setCancelable(false);
        progressDialogDownloadItem.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        progressDialogDownloadItem.show();
        serverRequests.downloadItemsInBackgroudn(listId, new GetItems() {
            @Override
            public void done(ArrayList<Item> items) {
                progressDialogDownloadItem.dismiss();
                if (items.size() > 0)
                    if (items.get(0).get_itemId() == -1) {
                        showConnectionError();
                    } else {
                        dbHandler.addMultipleItems(items);
                    }
                downloadUsernames();
            }

        });
    }

    private void downloadUsernames(){
        final ProgressDialog progressDialogDownloadItem = new ProgressDialog(ActivityNotifications.this,R.style.MyTheme);
        progressDialogDownloadItem.setCancelable(false);
        progressDialogDownloadItem.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        progressDialogDownloadItem.show();
        serverRequests.downloadUsernamesInBackground(listId, new GetUsernames() {
            @Override
            public void done(ArrayList<String> usernames) {
                if (usernames.size() > 0) {
                    for (int i = 0; i < usernames.size(); i++) {
                        System.out.println("Userite koito poluchavam na teglene na user lists");
                        System.out.println(usernames.get(i));
                    }
                    if (usernames.get(0).equals("Error: Timeout")) {
                        showConnectionError();
                    } else {
                        dbHandler.addMultipleUsersToSharedWith(listId, usernames);
                    }
                }
                dbHandler.updateAccepted(notificationId);
                Intent intent = new Intent(ActivityNotifications.this, MainActivity.class);
                intent.putExtra("tab", 1);
                startActivity(intent);
                finish();

            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showServerError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Server Error");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showConnectionError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Network error! Check your internet connection and try again!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showAlreadyAccepted(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("You have already accepted that notification!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //System.out.println(userLocalStore.userLocalDatabase.getAll().toString());
        if(!authenticate()){
            startActivity(new Intent(ActivityNotifications.this, ActivityLogin.class));
            finish();
        }
    }

    private boolean authenticate(){
        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
        return userLocalStore.getIfLoggedIn();
    }
}
