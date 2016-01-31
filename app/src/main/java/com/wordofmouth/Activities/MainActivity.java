package com.wordofmouth.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.util.ArrayList;

import TabLibraries.SlidingTabLayout;

public class MainActivity extends BaseActivity{

    ArrayList<MyList> lists;
    UserLocalStore userLocalStore;

    // Tabs variables
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"My Lists","Shared With Me"};
    int NumberOfTabs =2;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //
        userLocalStore = new UserLocalStore(this);
        String username = userLocalStore.getUserLocalDatabase().getString("username", "");
        // get the user`s lists to display on fragment
        dbHandler = DBHandler.getInstance(this);
        lists = new ArrayList<MyList>();
        lists = dbHandler.getLists(username);


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
            startActivity(new Intent(MainActivity.this, ActivityLogin.class));
            finish();
        }
    }

    private boolean authenticate(){
        return userLocalStore.getIfLoggedIn();
    }
}
