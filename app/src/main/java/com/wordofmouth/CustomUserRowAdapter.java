package com.wordofmouth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;


public class CustomUserRowAdapter extends ArrayAdapter<String> {

    ArrayList<User> users;
    String [] usernames;

    public CustomUserRowAdapter(Context context, String[] usernames, ArrayList<User> users) {
        super(context, R.layout.custom_user_row, usernames);
        this.users = users;
        this.usernames = usernames;
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
            viewHolder.customRowPicture.setImageBitmap(StringToBitMap(image));
        }
        else {
            viewHolder.customRowPicture.setImageResource(R.drawable.profiledefault);
        }
        viewHolder.customRowUsername.setText(username);
        viewHolder.customRowName.setText(name);
        return convertView;
    }

    public Bitmap StringToBitMap(String encodedString){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, scaleOptions);

        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= 100
                && scaleOptions.outHeight / scale / 2 >= 100) {
            scale *= 2;
        }

        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,outOptions);
    }
}
