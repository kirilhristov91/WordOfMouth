package com.wordofmouth.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;


public abstract class BaseActivity extends AppCompatActivity{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        //System.out.println("BaseActivity setContentView dbHandler : " + dbHandler == null);
        LinearLayout baseLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.base_layout, null);
        View view = getLayoutInflater().inflate(layoutResID, null);

        mDrawerLayout = (DrawerLayout) baseLayout.findViewById(R.id.drawer_layout);
        ListView menuListView = (ListView) mDrawerLayout.findViewById(R.id.list_slidermenu);
        String[] drawerListViewItems = getResources().getStringArray(R.array.menu_items);
        ArrayAdapter<String> menuAdapter = new CustomMenuItemAdapter(this, drawerListViewItems);
        menuListView.setAdapter(menuAdapter);
        DrawerItemClickListener drawerItemClickListener = new DrawerItemClickListener(this, mDrawerLayout);
        menuListView.setOnItemClickListener(drawerItemClickListener);

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );


        Toolbar toolbar = (Toolbar) baseLayout.findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        FrameLayout actContent = (FrameLayout) mDrawerLayout.findViewById(R.id.frame_container);
        actContent.addView(view);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ImageView menuProfilePicture = (ImageView) mDrawerLayout.findViewById(R.id.menuProfilePicture);
        menuProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BaseActivity.this.toString().contains("ActivityProfile")) {
                    mDrawerLayout.closeDrawers();
                } else {
                    Intent myIntent = new Intent(BaseActivity.this, ActivityProfile.class);
                    startActivity(myIntent);
                    mDrawerLayout.closeDrawers();
                }
            }
        });

        userLocalStore = UserLocalStore.getInstance(this);
        DBHandler dbHandler = DBHandler.getInstance(this);
        Utilities utilities = Utilities.getInstance(this);
        User currentUser = userLocalStore.getUserLoggedIn();
        String pic = dbHandler.getProfilePicture(currentUser.getId());
        if (pic != null) {
            Bitmap bitmap = utilities.StringToBitMap(pic, 100, 100);
            menuProfilePicture.setImageBitmap(bitmap);
        }

        // Set actionBarDrawerToggle as the DrawerListener
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        //}

        setContentView(baseLayout);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        Context context;
        DrawerLayout mDrawerLayout;
        //UserLocalStore userLocalStore;

        public DrawerItemClickListener(Context context, DrawerLayout mDrawerLayout) {
            this.context = context;
            this.mDrawerLayout = mDrawerLayout;

        }

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
           switch (position){
               case 0:
                   if(context.toString().contains("MainActivity")){
                       mDrawerLayout.closeDrawers();
                   }
                   else {
                       Intent home = new Intent(context, MainActivity.class);
                       //home.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                       context.startActivity(home);
                       mDrawerLayout.closeDrawers();
                   }
                   break;
               case 1:
                   if(context.toString().contains("ActivityNotifications")){
                       mDrawerLayout.closeDrawers();
                   }
                   else {
                       Intent notifications = new Intent(context, ActivityNotifications.class);
                       //notifications.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                       context.startActivity(notifications);
                       mDrawerLayout.closeDrawers();
                   }
                   break;
               case 2:
                   if(context.toString().contains("ActivityFeedback")){
                       mDrawerLayout.closeDrawers();
                   }
                   else {
                       Intent feedback = new Intent(context, ActivityFeedback.class);
                       //feedback.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                       context.startActivity(feedback);
                       mDrawerLayout.closeDrawers();
                   }
                   break;
               case 3:
                   if(context.toString().contains("ActivityAbout")){
                       mDrawerLayout.closeDrawers();
                   }
                   else {
                       Intent about = new Intent(context, ActivityAbout.class);
                       //about.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                       context.startActivity(about);
                       mDrawerLayout.closeDrawers();
                   }
                   break;
               case 4:
                   Intent logout = new Intent(context, ActivityLogin.class);
                   logout.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   userLocalStore.clearUserData();
                   userLocalStore.setUserLoggedIn(false);
                   context.startActivity(logout);
                   mDrawerLayout.closeDrawers();
                   break;
           }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean drawerOpened = false;
        switch (item.getItemId()) {
            case R.id.action_vili:
                drawerOpened = true;
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return drawerOpened;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}