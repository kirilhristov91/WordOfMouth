package com.wordofmouth;


import android.content.Context;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.ArrayList;

public class CustomItemRowAdapter extends ArrayAdapter<String> {

    ArrayList<Item> itemsList;
    public CustomItemRowAdapter(Context context, String[] items, ArrayList<Item> itemsList) {
        super(context, R.layout.custom_item_row, items);
        this.itemsList = itemsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_item_row, parent, false);

        String singleItem = getItem(position);
        TextView itemTitle  = (TextView) customView.findViewById(R.id.itemTitle);
        TextView addedBy = (TextView) customView.findViewById(R.id.addedBy);
        ImageView itemImage = (ImageView) customView.findViewById(R.id.itemImage);
        RatingBar ratingBar = (RatingBar) customView.findViewById(R.id.customRowRatingBar);

        itemImage.setImageResource(R.drawable.itemimage);
        itemTitle.setText(singleItem);
        // dobavi creator id v tablicata za items
        //String addBy = itemsList.get(position).get_creatorID();
        //addedBy.setText("Added by: ");
        ratingBar.setRating((float)itemsList.get(position).get_rating());
        return customView;
    }


}
