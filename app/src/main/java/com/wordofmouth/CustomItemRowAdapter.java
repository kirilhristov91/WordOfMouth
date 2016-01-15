package com.wordofmouth;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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

            //
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
            viewHolder.itemImage.setImageBitmap(StringToBitMap(image));
        }
        viewHolder.itemTitle.setText(singleItem);
        viewHolder.addedBy.setText(itemsList.get(position).get_creatorUsername());
        viewHolder.ratingBar.setRating((float)itemsList.get(position).get_rating());
        return convertView;
    }

    public Bitmap StringToBitMap(String encodedString){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
