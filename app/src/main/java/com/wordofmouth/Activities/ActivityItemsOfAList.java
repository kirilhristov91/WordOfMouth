package com.wordofmouth.Activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wordofmouth.Other.DBGetData;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Interfaces.GetBitmap;
import com.wordofmouth.Interfaces.GetItems;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.R;
import com.wordofmouth.Other.StringToBitmapRequests;

import java.util.ArrayList;

public class ActivityItemsOfAList extends BaseActivity implements View.OnClickListener{

    TextView addItemText;
    TextView invitePeople;
    ListView itemsListView;
    ArrayList<Item> items;
    int selectedListId;
    String listName;
    String[] itemNames;
    int tabToreturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_of_alist_view);
        addItemText = (TextView) findViewById(R.id.addItemText);
        addItemText.setOnClickListener(this);
        invitePeople = (TextView) findViewById(R.id.invitePeople);
        invitePeople.setOnClickListener(this);
        itemsListView = (ListView) findViewById(R.id.itemsListView);

        // get the sent information from Another activity
        // there is no listID 0 in the database so set 0 as itemimage value
        Intent intent = getIntent();
        selectedListId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");
        tabToreturn = intent.getIntExtra("tab", 0);
        getSupportActionBar().setTitle(listName);

        // create an instance of the local database
        items = new ArrayList<Item>();
        DBGetData dbGetData = DBGetData.getInstance(this);
        final ProgressDialog progressDialogFetching = new ProgressDialog(this);
        progressDialogFetching.setCancelable(false);
        progressDialogFetching.setTitle("Processing");
        progressDialogFetching.setMessage("Fetching data from database...");
        progressDialogFetching.show();
        
        dbGetData.GetItemsInBackground(selectedListId, new GetItems() {
            @Override
            public void done(ArrayList<Item> returnedItems) {
                progressDialogFetching.dismiss();
                System.out.println("Number of elements " + returnedItems.size());
                Display(returnedItems);
            }
        });
    }

    // method to display the items after fetching from database is done
    public void Display(ArrayList<Item> returnedItems){
        items = returnedItems;
        System.out.println("Number of elements in items " + items.size());
        // display via adapter
        itemNames = new String[items.size()];
        for(int i =0; i< items.size();i++){
            itemNames[i] = items.get(i).get_name();
        }

        StringToBitmapRequests stbr = StringToBitmapRequests.getInstance(this);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        stbr.stringToBitmapInBackground(items, new GetBitmap() {
            @Override
            public void done(ArrayList<Bitmap> result) {
                progressDialog.dismiss();
                /*System.out.println("SIZE OF BITMAPS " + result.size());
                final Runtime runtime = Runtime.getRuntime();
                final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
                final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
                System.out.println("Total heap size: " + maxHeapSizeInMB + " MB");
                System.out.println("Available heap size: " + usedMemInMB + " MB");*/

                ArrayAdapter<String> womAdapter =
                        new CustomItemRowAdapter(ActivityItemsOfAList.this, itemNames, items, result);
                itemsListView.setAdapter(womAdapter);

            }
        });

        // set adapter listener to open itemView if there is an item selected
        itemsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String itemName = String.valueOf(parent.getItemAtPosition(position));
                        int itemIdClicked = items.get(position).get_itemId();
                        Intent myIntent = new Intent(ActivityItemsOfAList.this, ActivityItem.class);
                        myIntent.putExtra("listId", selectedListId);
                        myIntent.putExtra("listName", listName);
                        myIntent.putExtra("itemId", itemIdClicked);
                        myIntent.putExtra("itemName", itemName);
                        myIntent.putExtra("tab", tabToreturn);
                        startActivity(myIntent);
                        finish();
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("tab", tabToreturn);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        // open addItemView
        switch(v.getId()) {
            case R.id.addItemText:
                Intent intent = new Intent(this, ActivityAddItem.class);
                intent.putExtra("listId", selectedListId);
                intent.putExtra("name", listName);
                intent.putExtra("tab", tabToreturn);
                startActivity(intent);
                finish();
                break;
            case R.id.invitePeople:
                Intent invite = new Intent(this, ActivityInvite.class);
                invite.putExtra("listId", selectedListId);
                invite.putExtra("name", listName);
                invite.putExtra("tab", tabToreturn);
                startActivity(invite);
                finish();
                break;
        }
    }
}
