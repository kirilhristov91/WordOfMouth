package com.wordofmouth.Activities;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ImageView;

import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.R;

import java.util.ArrayList;

public class CustomItemRowAdapter extends ArrayAdapter<String> {
    ArrayList<Bitmap> bitmaps;
    ArrayList<Item> itemsList;
    public CustomItemRowAdapter(Context context, String[] items, ArrayList<Item> itemsList, ArrayList<Bitmap> bitmaps) {
        super(context, R.layout.custom_item_row, items);
        this.itemsList = itemsList;
        this.bitmaps = bitmaps;
    }

    static class ViewHolderItem{
        TextView itemTitle;
        TextView addedBy;
        RatingBar ratingBar;
        ImageView itemImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_item_row, parent, false);
            viewHolder = new ViewHolderItem();

            viewHolder.itemTitle  = (TextView) convertView.findViewById(R.id.itemTitle);
            viewHolder.addedBy = (TextView) convertView.findViewById(R.id.addedByUsername);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.itemImage);
            viewHolder.ratingBar = (RatingBar) convertView.findViewById(R.id.customRowRatingBar);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        String singleItem = getItem(position);
        String image = itemsList.get(position).get_itemImage();
        if(!image.equals("")){
            viewHolder.itemImage.setImageBitmap(bitmaps.get(position));
        }
        else {
            viewHolder.itemImage.setImageResource(R.drawable.logowom);
        }
        viewHolder.itemTitle.setText(singleItem);
        viewHolder.addedBy.setText(itemsList.get(position).get_creatorUsername());
        viewHolder.ratingBar.setRating((float) itemsList.get(position).get_rating());
        if(itemsList.get(position).getSeen() == 0){
            int c = Color.parseColor("#A5D6A7");
            convertView.setBackgroundColor(c);
        }
        else{
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }
}
