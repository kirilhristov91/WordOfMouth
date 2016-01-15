package com.wordofmouth;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemsOfAListView extends AppCompatActivity implements View.OnClickListener{

    TextView addItemText;
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
        itemsListView = (ListView) findViewById(R.id.itemsListView);

        // get the sent information from Another activity
        // there is no listID 0 in the database so set 0 as itemimage value
        Intent intent = getIntent();
        selectedListId = intent.getIntExtra("listId", 0);
        listName = intent.getStringExtra("name");
        getSupportActionBar().setTitle(listName);

        // create an instance of the local database
        dbHandler = DBHandler.getInstance(this);

        // get all the items for the selected list
        long start = System.currentTimeMillis();
        items = new ArrayList<Item>();
        items = dbHandler.getItems(selectedListId);
        long end = System.currentTimeMillis();
        System.out.println("\nElapsed time for db: " + (end - start) + " milliseconds");

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
                ArrayAdapter<String> womAdapter =
                        new CustomItemRowAdapter(ItemsOfAListView.this, itemNames, items, result);
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
                        Intent myIntent = new Intent(ItemsOfAListView.this, ItemView.class);
                        myIntent.putExtra("listId", selectedListId);
                        myIntent.putExtra("listName", listName);
                        myIntent.putExtra("itemId", itemIdClicked);
                        myIntent.putExtra("itemName", itemName);
                        startActivity(myIntent);
                        finish();
                    }
                }
        );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {
        // open addItemView
        switch(v.getId()) {
            case R.id.addItemText:
                Intent intent = new Intent(this, AddItemView.class);
                intent.putExtra("listId", selectedListId);
                intent.putExtra("name", listName);
                startActivity(intent);
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
