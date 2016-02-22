package com.wordofmouth.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;

import java.util.ArrayList;


public class CustomUserRowAdapter extends ArrayAdapter<String> {

    ArrayList<User> users;
    String [] usernames;
    Context context;
    Utilities utilities;

    public CustomUserRowAdapter(Context context, String[] usernames, ArrayList<User> users) {
        super(context, R.layout.custom_user_row, usernames);
        this.users = users;
        this.usernames = usernames;
        utilities = Utilities.getInstance(context);
    }

    static class ViewHolderItem{
        TextView customRowName;
        TextView customRowUsername;
        ImageView customRowPicture;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_user_row, parent, false);
            viewHolder = new ViewHolderItem();

            viewHolder.customRowName  = (TextView) convertView.findViewById(R.id.customRowName);
            viewHolder.customRowUsername = (TextView) convertView.findViewById(R.id.customRowUsername);
            viewHolder.customRowPicture = (ImageView) convertView.findViewById(R.id.customRowPicture);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        String username = usernames[position];
        String name = users.get(position).getName();
        String image = users.get(position).getPicture();
        if(!image.equals("null")){
            viewHolder.customRowPicture.setImageBitmap(utilities.StringToBitMap(image, 100, 100));
        }
        else {
            viewHolder.customRowPicture.setImageResource(R.drawable.profiledefault);
        }
        viewHolder.customRowUsername.setText(username);
        viewHolder.customRowName.setText(name);
        return convertView;
    }
}
