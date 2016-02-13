package com.wordofmouth.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.ObjectClasses.Shared;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.R;
import java.util.ArrayList;

public class CustomListRowAdapter extends ArrayAdapter<String> {

    ArrayList<Bitmap> bitmaps;
    ArrayList<MyList> lists;
    ArrayList<Shared> usernames;
    public CustomListRowAdapter(Context context, String[] listNames, ArrayList<MyList> lists, ArrayList<Bitmap> bitmaps, ArrayList<Shared> usernames) {
        super(context, R.layout.custom_list_row, listNames);
        this.lists = lists;
        this.bitmaps = bitmaps;
        this.usernames = usernames;
    }

    static class ViewHolderItem{
        ImageView listImage;
        TextView listTitle;
        TextView descriptionTitle;
        TextView descriptionText;
        TextView listAddedBy;
        TextView listAddedByUsername;
        TextView sharedWithTitle;
        TextView sharedWithUsernames;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_list_row, parent, false);
            viewHolder = new ViewHolderItem();

            viewHolder.listImage = (ImageView) convertView.findViewById(R.id.listImage);
            viewHolder.listTitle  = (TextView) convertView.findViewById(R.id.listTitle);
            viewHolder.descriptionTitle = (TextView) convertView.findViewById(R.id.descriptionTitle);
            viewHolder.descriptionText = (TextView) convertView.findViewById(R.id.descriptionText);
            viewHolder.listAddedBy = (TextView) convertView.findViewById(R.id.listAddedBy);
            viewHolder.listAddedByUsername = (TextView) convertView.findViewById(R.id.listAddedByUsername);
            viewHolder.sharedWithTitle = (TextView) convertView.findViewById(R.id.sharedWithTitle);
            viewHolder.sharedWithUsernames = (TextView) convertView.findViewById(R.id.sharedWithUsernames);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        /*for(int i=0; i<usernames.size(); i++){
            System.out.println(usernames.get(i).getUsername());
        }*/

        String sharedWith = "";
        for(int i=0; i<usernames.size(); i++){
            if(usernames.get(i).getListId() == lists.get(position).get_listId()){
                sharedWith += usernames.get(i).getUsername() + ", ";
            }
        }

        if(sharedWith.length()>0){
            sharedWith = sharedWith.substring(0, sharedWith.length()-2);
            viewHolder.sharedWithUsernames.setText(sharedWith);
        }

        String title = getItem(position);
        String image = lists.get(position).getImage();
        if(!image.equals("")){
            viewHolder.listImage.setImageBitmap(bitmaps.get(position));
        }
        else {
            viewHolder.listImage.setImageResource(R.drawable.logowom);
        }
        viewHolder.listTitle.setText(title);
        if(lists.get(position).get_description().length()>0) {
            viewHolder.descriptionText.setText(lists.get(position).get_description());
        }
        viewHolder.listAddedByUsername.setText(lists.get(position).get_username());
        if(lists.get(position).getHasNewContent() == 1){
            int c = Color.parseColor("#A5D6A7");
            convertView.setBackgroundColor(c);
        }
        return convertView;
    }
}
