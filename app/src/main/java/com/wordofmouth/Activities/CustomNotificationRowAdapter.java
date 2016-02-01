package com.wordofmouth.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.wordofmouth.R;

public class CustomNotificationRowAdapter extends ArrayAdapter<String> {

    public CustomNotificationRowAdapter(Context context, String[] messages) {
        super(context, R.layout.custom_notification_row, messages);
    }

    static class ViewHolderItem{
        ImageView notificationImage;
        TextView notificationText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_notification_row, parent, false);
            viewHolder = new ViewHolderItem();

            viewHolder.notificationText = (TextView) convertView.findViewById(R.id.notificationText);
            viewHolder.notificationImage = (ImageView) convertView.findViewById(R.id.notificationImage);


            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        String msg = getItem(position);
        viewHolder.notificationText.setText(msg);
        viewHolder.notificationImage.setImageResource(R.drawable.notification);
        return convertView;
    }

}
