package com.wordofmouth.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.R;
import java.util.ArrayList;

public class CustomListRowAdapter extends ArrayAdapter<String> {

    ArrayList<Bitmap> bitmaps;
    ArrayList<MyList> lists;
    public CustomListRowAdapter(Context context, String[] listNames, ArrayList<MyList> lists, ArrayList<Bitmap> bitmaps) {
        super(context, R.layout.custom_list_row, listNames);
        this.lists = lists;
        this.bitmaps = bitmaps;
    }

    static class ViewHolderItem{
        ImageView listImage;
        TextView listTitle;
        TextView descriptionTitle;
        TextView descriptionText;
        TextView listAddedBy;
        TextView listAddedByUsername;
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

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
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
        viewHolder.descriptionText.setText(lists.get(position).get_description());
        viewHolder.listAddedByUsername.setText(lists.get(position).get_username());
        return convertView;
    }
}
