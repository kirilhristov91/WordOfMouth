package com.wordofmouth.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wordofmouth.Other.DBGetItems;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Interfaces.GetBitmap;
import com.wordofmouth.Interfaces.GetItems;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.R;
import com.wordofmouth.Other.StringToBitmapRequests;

import java.util.ArrayList;

public class ActivityItemsOfAList extends AppCompatActivity implements View.OnClickListener{

    TextView addItemText;
    TextView invitePeople;
    ListView itemsListView;
    ArrayList<Item> items;
    int selectedListId;
    String listName;
    DBHandler dbHandler;
    String[] itemNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_of_alist_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
        getSupportActionBar().setTitle(listName);

        // create an instance of the local database
        dbHandler = DBHandler.getInstance(this);
        items = new ArrayList<Item>();

        DBGetItems dbGetItems = new DBGetItems(this);
        dbGetItems.GetItemsInBackground(selectedListId, new GetItems() {
            @Override
            public void done(ArrayList<Item> returnedItems) {
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

        StringToBitmapRequests stbr = new StringToBitmapRequests(this);
        stbr.stringToBitmapInBackground(items, new GetBitmap() {
            @Override
            public void done(ArrayList<Bitmap> result) {
                System.out.println("SIZE OF BITMAPS " + result.size());
                final Runtime runtime = Runtime.getRuntime();
                final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
                final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
                System.out.println("Total heap size: " + maxHeapSizeInMB + " MB");
                System.out.println("Available heap size: " + usedMemInMB + " MB");

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
                        startActivity(myIntent);
                        finish();
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
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
                startActivity(intent);
                finish();
                break;
            case R.id.invitePeople:
                Intent invite = new Intent(this, ActivityInvite.class);
                invite.putExtra("listId", selectedListId);
                invite.putExtra("name", listName);
                startActivity(invite);
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
