package com.wordofmouth.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wordofmouth.ObjectClasses.Notification;
import com.wordofmouth.Other.DBHandler;
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
        }

        notificationItemListView = (ListView) findViewById(R.id.notificationListView);
        ArrayAdapter<String> notificationAdapter =
                new CustomNotificationRowAdapter(ActivityNotifications.this, messages);
        notificationItemListView.setAdapter(notificationAdapter);

        notificationItemListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(ActivityNotifications.this, notifications.get(position).getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

}
