package com.wordofmouth.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.wordofmouth.R;

public class CustomMenuItemAdapter extends ArrayAdapter<String> {

    String[] menuItems;

    public CustomMenuItemAdapter(Context context, String[] menuItems) {
        super(context, R.layout.drawer_listview_item, menuItems);
        this.menuItems = menuItems;
    }

    static class ViewHolderItem{
        TextView menuItemText;
        ImageView menuItemIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.drawer_listview_item, parent, false);
            viewHolder = new ViewHolderItem();

            viewHolder.menuItemText  = (TextView) convertView.findViewById(R.id.menuItemText);
            viewHolder.menuItemIcon = (ImageView) convertView.findViewById(R.id.menuItemIcon);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        String itemText = menuItems[position];
        viewHolder.menuItemText.setText(itemText);
        switch (position){
            case 0:
                viewHolder.menuItemIcon.setImageResource(R.drawable.home);
                break;
            case 1:
                viewHolder.menuItemIcon.setImageResource(R.drawable.notification);
                break;
            case 2:
                viewHolder.menuItemIcon.setImageResource(R.drawable.feedback);
                break;
            case 3:
                viewHolder.menuItemIcon.setImageResource(R.drawable.about);
                break;
            case 4:
                viewHolder.menuItemIcon.setImageResource(R.drawable.logout);
                break;
        }
        return convertView;
    }

}
