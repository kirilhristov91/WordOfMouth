package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    // If the actionBar is not be used by an Activity it should override this method to return false
    public boolean usesToolbar(){
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        //System.out.println("BaseActivity setContentView dbHandler : " + dbHandler == null);
        LinearLayout baseLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.base_layout, null);
        View view = getLayoutInflater().inflate(layoutResID, null);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mDrawerLayout = (DrawerLayout) baseLayout.findViewById(R.id.drawer_layout);

        Toolbar toolbar = (Toolbar) baseLayout.findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        FrameLayout actContent = (FrameLayout) mDrawerLayout.findViewById(R.id.frame_container);
        actContent.addView(view);

        if(!usesToolbar()){
            toolbar.setVisibility(view.GONE);
        }

        else {
            actionBarDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.string.drawer_open,  /* "open drawer" description */
                    R.string.drawer_close  /* "close drawer" description */
            );

            // creates the menu list
            ListView menuListView = (ListView) mDrawerLayout.findViewById(R.id.list_slidermenu);
            String[] drawerListViewItems = getResources().getStringArray(R.array.menu_items);
            ArrayAdapter<String> menuAdapter = new CustomMenuItemAdapter(this, drawerListViewItems);
            menuListView.setAdapter(menuAdapter);
            DrawerItemClickListener drawerItemClickListener = new DrawerItemClickListener(this, mDrawerLayout);
            menuListView.setOnItemClickListener(drawerItemClickListener);

            // set the link to the Profile Activity
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

            // set the profile picture inside the menu
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
        }

        setContentView(baseLayout);
    }

    // sets the menu navigation links
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
                   finish();
                   System.gc();
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
        if(usesToolbar()) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if(usesToolbar()) {
            actionBarDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(usesToolbar()) {
            actionBarDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    // opens the drawer menu on menu icon click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean drawerOpened = false;
        switch (item.getItemId()) {
            case R.id.action_navmenu:
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

    // method to check if the phone has Internet connection
    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // method to display a message containing dialog box to the user
    protected void showError(String message){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage(message);
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    // method to hide the soft keyboard. The event to hide the keyboard is defined in the particular Activity
    public void hideKeyboard(View view){
        InputMethodManager in = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}