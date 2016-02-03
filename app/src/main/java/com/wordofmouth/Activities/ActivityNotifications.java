package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wordofmouth.Interfaces.GetItems;
import com.wordofmouth.Interfaces.GetListId;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_notifications);

        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
        int userId = userLocalStore.getUserLoggedIn().getId();
        DBHandler dbHandler = DBHandler.getInstance(this);
        notifications = dbHandler.getNotifications(userId);
        String[] messages = new String[notifications.size()];
        for(int i = 0; i<notifications.size();i++){
            messages[i] = notifications.get(i).getMsg();
            System.out.println("list id:" + notifications.get(i).getListId() + " userId:" + notifications.get(i).getUserId()+ "!");
        }

        notificationItemListView = (ListView) findViewById(R.id.notificationListView);
        ArrayAdapter<String> notificationAdapter =
                new CustomNotificationRowAdapter(ActivityNotifications.this, messages);
        notificationItemListView.setAdapter(notificationAdapter);

        notificationItemListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Toast.makeText(ActivityNotifications.this, notifications.get(position).getMsg(), Toast.LENGTH_SHORT).show();
                        showConfirmationDialog(notifications.get(position));
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
                    //UserLocalStore userLocalStore = UserLocalStore.getInstance(ActivityNotifications.this);
                    ServerRequests serverRequests = ServerRequests.getInstance(ActivityNotifications.this);
                    final ProgressDialog progressDialogDownloadList = new ProgressDialog(ActivityNotifications.this);
                    progressDialogDownloadList.setCancelable(false);
                    progressDialogDownloadList.setTitle("Processing");
                    progressDialogDownloadList.setMessage("Fetching data from server...");
                    progressDialogDownloadList.show();
                    serverRequests.downloadListInBackgroudn(n.getListId(), n.getUserId(), new GetListId() {
                        @Override
                        public void done(MyList myList) {
                            if (myList.get_username().equals("Timeout")) {
                                showConnectionError();
                            } else if (myList.get_username().equals("UpdError")) {
                                showServerError();
                            } else {
                                DBHandler dbHandler = DBHandler.getInstance(ActivityNotifications.this);
                                dbHandler.addList(myList);
                                progressDialogDownloadList.dismiss();
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

        int listId = list.get_listId();

        ServerRequests serverRequests = ServerRequests.getInstance(this);
        final ProgressDialog progressDialogDownloadList = new ProgressDialog(this);
        progressDialogDownloadList.setCancelable(false);
        progressDialogDownloadList.setTitle("Processing");
        progressDialogDownloadList.setMessage("Fetching data from server...");
        progressDialogDownloadList.show();
        serverRequests.downloadItemsInBackgroudn(listId, new GetItems() {
            @Override
            public void done(ArrayList<Item> items) {
                if(items.size()>0){
                    DBHandler dbHandler = DBHandler.getInstance(ActivityNotifications.this);
                    dbHandler.addMultipleItems(items);
                }
                progressDialogDownloadList.dismiss();
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
}
