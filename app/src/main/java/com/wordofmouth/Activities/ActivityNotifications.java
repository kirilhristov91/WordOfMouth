package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetItems;
import com.wordofmouth.Interfaces.GetList;
import com.wordofmouth.Interfaces.GetUsernames;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.List;
import com.wordofmouth.ObjectClasses.Notification;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.util.ArrayList;

public class ActivityNotifications extends BaseActivity {

    ArrayList<Notification> notifications;
    ListView notificationItemListView;
    int notificationId, listId;
    DBHandler dbHandler;
    ServerRequests serverRequests;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        dbHandler = DBHandler.getInstance(this);
        serverRequests = ServerRequests.getInstance(this);
        userLocalStore = UserLocalStore.getInstance(ActivityNotifications.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                            showError("You have already accepted that invitation!");
                        } else {
                            notificationId = notifications.get(position).getId();
                            showConfirmationDialog(notifications.get(position));
                        }
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!authenticate()){
            startActivity(new Intent(ActivityNotifications.this, ActivityLogin.class));
            finish();
        }
    }

    private boolean authenticate(){
        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
        return userLocalStore.getIfLoggedIn();
    }

    private void showConfirmationDialog(Notification notification){
        final Notification n = notification;
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Do you accept the invitation? In case you do the list you have been invited and all its items will be downloaded on your phone.");
        allertBuilder.setCancelable(false);

        allertBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!isNetworkAvailable()) {
                    showError("Network error! Check your internet connection and try again!");
                } else {
                    final ProgressDialog progressDialogDownloadList = new ProgressDialog(ActivityNotifications.this, R.style.MyTheme);
                    progressDialogDownloadList.setCancelable(false);
                    progressDialogDownloadList.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                    progressDialogDownloadList.show();
                    serverRequests.downloadListInBackground(n.getListId(), userLocalStore.getUserLoggedIn().getId(), new GetList() {
                        @Override
                        public void done(List list) {
                            progressDialogDownloadList.dismiss();
                            if (list.get_username().equals("Timeout")) {
                                showError("Network error! Check your internet connection and try again!");
                            } else if (list.get_username().equals("UpdError")) {
                                showError("Server error");
                            } else {
                                DBHandler dbHandler = DBHandler.getInstance(ActivityNotifications.this);
                                list.setHasNewContent(1);
                                dbHandler.addList(list);
                                downloadItems(list);
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

    private void downloadItems(List list){

        listId = list.get_listId();

        final ProgressDialog progressDialogDownloadItem = new ProgressDialog(ActivityNotifications.this,R.style.MyTheme);
        progressDialogDownloadItem.setCancelable(false);
        progressDialogDownloadItem.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        progressDialogDownloadItem.show();
        serverRequests.downloadItemsInBackground(listId, new GetItems() {
            @Override
            public void done(ArrayList<Item> items) {
                progressDialogDownloadItem.dismiss();
                if (items.size() > 0)
                    if (items.get(0).get_itemId() == -1) {
                        showError("Network error! Check your internet connection and try again!");
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
                        showError("Network error! Check your internet connection and try again!");
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
}
