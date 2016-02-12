package com.wordofmouth.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wordofmouth.ObjectClasses.Notification;
import com.wordofmouth.R;

import java.util.ArrayList;

public class CustomNotificationRowAdapter extends ArrayAdapter<String> {

    ArrayList<Notification> notifications;

    public CustomNotificationRowAdapter(Context context, String[] messages, ArrayList<Notification> notifications) {
        super(context, R.layout.custom_notification_row, messages);
        this.notifications = notifications;
    }

    static class ViewHolderItem{
        ImageView notificationImage;
        TextView notificationText;
        TextView notificationDate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_notification_row, parent, false);
            viewHolder = new ViewHolderItem();

            viewHolder.notificationText = (TextView) convertView.findViewById(R.id.notificationText);
            viewHolder.notificationDate = (TextView) convertView.findViewById(R.id.notificationDate);
            viewHolder.notificationImage = (ImageView) convertView.findViewById(R.id.notificationImage);


            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        String msg = getItem(position);
        viewHolder.notificationText.setText(msg);
        viewHolder.notificationDate.setText(notifications.get(position).getDate());
        return convertView;
    }

}
