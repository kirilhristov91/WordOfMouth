package com.wordofmouth.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.util.ArrayList;

import TabLibraries.SlidingTabLayout;

public class MainActivity extends BaseActivity{

    // Tabs variables
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"My Lists","Shared With Me"};
    int NumberOfTabs =2;
    ArrayList<MyList> myLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,NumberOfTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // taka se slaga koi tab da se pokaje 0 ili 1
        if(getIntent().getIntExtra("tab", 0) == 1) {
            pager.setCurrentItem(1);
        }

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabbs Fixed set this true, This makes the tabs Space Evenly in Available width

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
        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
        return userLocalStore.getIfLoggedIn();
    }
}
