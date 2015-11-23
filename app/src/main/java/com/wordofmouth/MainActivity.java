package com.wordofmouth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;

import TabLibraries.SlidingTabLayout;

public class MainActivity extends AppCompatActivity{

    ArrayList<MyList> lists;
    UserLocalStore userLocalStore;

    // Tabs variables
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"My Lists","Shared Lists","Profile"};
    int NumberOfTabs =3;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);

        //
        userLocalStore = new UserLocalStore(this);

        // get the user`s lists to display on fragment
        dbHandler = DBHandler.getInstance(this);
        lists = new ArrayList<MyList>();
        lists = dbHandler.getLists();

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,NumberOfTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return R.color.tabsScrollColor;
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }

    public ArrayList<MyList> getMyLists() {
        return lists;
    }


    @Override
    protected void onStart() {
        super.onStart();
        //System.out.println(userLocalStore.userLocalDatabase.getAll().toString());
        if(!authenticate()){
          startActivity(new Intent(MainActivity.this, Login.class));
        }
    }

    private boolean authenticate(){
        return userLocalStore.getIfLoggedIn();
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

        if(id == R.id.action_logout){
            userLocalStore.clearUserData();
            userLocalStore.setUserLoggedIn(false);
            startActivity(new Intent(this, Login.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
