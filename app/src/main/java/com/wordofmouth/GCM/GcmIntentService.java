package com.wordofmouth.GCM;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.wordofmouth.Activities.*;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.wordofmouth.Interfaces.GetItem;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.Notification;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    UserLocalStore userLocalStore;
    DBHandler dbHandler;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        Log.d("wom","in gcm intent message "+messageType);
        Log.d("wom","in gcm intent message bundle "+extras);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                String recieved_message=intent.getStringExtra("text_message");
                userLocalStore = UserLocalStore.getInstance(this);
                dbHandler = DBHandler.getInstance(this);
                if(recieved_message.contains("invited")) {
                    // if the messag contains "invited" parse the list id and
                    // create a Notification object to save on the local database
                    // Finally show the push notification to the user
                    int index = recieved_message.length()-1;
                    while(recieved_message.charAt(index)!=' '){
                        index--;
                    }

                    // remove the list id from the message
                    String preparedMessage = recieved_message.substring(0, index);

                    // get the list id and and save the notification object in db
                    String idString = recieved_message.substring(index+1, recieved_message.length());
                    System.out.println("Received id after cutting the string is: " + idString);
                    Integer listId = Integer.parseInt(idString);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String date = sdf.format(new Date());

                    Notification n = new Notification(listId, preparedMessage, date, 0);
                    dbHandler.addNotification(n);

                    sendNotification(preparedMessage);
                }

                else if(recieved_message.contains("rating")) {
                    // if the messag contains "rating" parse the item id and download the item
                    // to uplate the local database
                    int index = recieved_message.length()-1;
                    while(recieved_message.charAt(index)!=' '){
                        index--;
                    }
                    String idString = recieved_message.substring(index+1, recieved_message.length());
                    Integer itemId = Integer.parseInt(idString);
                    ServerRequests serverRequests = ServerRequests.getInstance(this);
                    serverRequests.downloadNewItemInBackground(itemId, new GetItem() {
                        @Override
                        public void done(Item item) {
                            if (item!= null && item.get_itemId()!=-1){
                                dbHandler.updateRating(item);
                            }
                        }
                    });
                }

                else if(recieved_message.contains("new user to list:")){
                    // if the messag contains "new user to list" parse the list id and download the usernames
                    // of all users the list is shared with to update the local database
                    ArrayList<String> splittedString = new ArrayList<>();
                    for (String retval: recieved_message.split(" ")){
                        splittedString.add(retval);
                    }

                    String username = splittedString.get(splittedString.size()-1);
                    String id = splittedString.get(splittedString.size()-2);

                    Integer listId = Integer.parseInt(id);
                    dbHandler.addUserToSharedWith(listId, username);
                }

                else {
                    // parse the item id and download the item
                    // to save it the local database
                    Integer itemId = Integer.parseInt(recieved_message);
                    ServerRequests serverRequests = ServerRequests.getInstance(this);
                    serverRequests.downloadNewItemInBackground(itemId, new GetItem() {
                        @Override
                        public void done(Item item) {
                            if (item!= null && item.get_itemId()!=-1){
                                if (userLocalStore.getUserLoggedIn().getId() != item.get_creatorId()) {
                                    dbHandler.addItem(item);
                                    dbHandler.updateHasNewContent(item.get_listId(), 1);
                                }
                            }
                        }
                    });
                }

                Intent sendIntent =new Intent("message_recieved");
                sendIntent.putExtra("message",recieved_message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it
    private void sendNotification(String msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ActivityNotifications.class), 0);



        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logowom)
                        .setContentTitle("WoM")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setSound(alarmSound)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}