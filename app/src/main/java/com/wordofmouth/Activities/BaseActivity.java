package com.wordofmouth.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;


public abstract class BaseActivity extends AppCompatActivity{

    private DrawerLayout mDrawerLayout;
    private ListView menuListView;
    private String [] drawerListViewItems;
    private ArrayAdapter<String> menuAdapter;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView menuProfilePicture;
    protected static DBHandler dbHandler;
    protected static ServerRequests serverRequests;
    protected static UserLocalStore userLocalStore;
    private DrawerItemClickListener drawerItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("BaseActivity onCreate dbHander : " + dbHandler == null);
        super.onCreate(savedInstanceState);

        if(userLocalStore == null){
            userLocalStore = UserLocalStore.getInstance(this);
        }

        if(serverRequests == null){
            serverRequests = ServerRequests.getInstance();
        }

        if(dbHandler == null){
            dbHandler = DBHandler.getInstance(this);
        }
    }


    @Override
    public void setContentView(int layoutResID) {
        System.out.println("BaseActivity setContentView dbHandler : " + dbHandler == null);
        LinearLayout baseLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.base_layout, null);
        View view = getLayoutInflater().inflate(layoutResID, null);


        mDrawerLayout = (DrawerLayout) baseLayout.findViewById(R.id.drawer_layout);

        menuListView = (ListView) mDrawerLayout.findViewById(R.id.list_slidermenu);
        drawerListViewItems = getResources().getStringArray(R.array.menu_items);
        menuAdapter= new CustomMenuItemAdapter(this, drawerListViewItems);
        menuListView.setAdapter(menuAdapter);
        drawerItemClickListener = new DrawerItemClickListener(this, mDrawerLayout);
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


        menuProfilePicture = (ImageView) mDrawerLayout.findViewById(R.id.menuProfilePicture);
        menuProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(BaseActivity.this, ActivityProfile.class);
                startActivity(myIntent);
                mDrawerLayout.closeDrawers();
                //obmisli go tova
//                ((Activity)context).finish();
            }
        });

        User currentUser = userLocalStore.getUserLoggedIn();
        String pic = dbHandler.getProfilePicture(currentUser.getId());
        if (pic != null) {
            Bitmap bitmap = StringToBitMap(pic);
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

        public DrawerItemClickListener(Context context, DrawerLayout mDrawerLayout) {
            this.context = context;
            this.mDrawerLayout = mDrawerLayout;
        }

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
           switch (position){
               case 0:
                   Intent home = new Intent(context, MainActivity.class);
                   //home.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   context.startActivity(home);
                   mDrawerLayout.closeDrawers();
                   break;
               case 1:
                   Intent notifications = new Intent(context, ActivityNotifications.class);
                   //notifications.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   context.startActivity(notifications);
                   mDrawerLayout.closeDrawers();
                   break;
               case 2:
                   Intent feedback = new Intent(context, ActivityFeedback.class);
                   //feedback.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   context.startActivity(feedback);
                   mDrawerLayout.closeDrawers();
                   break;
               case 3:
                   Intent about = new Intent(context, ActivityAbout.class);
                   //about.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   context.startActivity(about);
                   mDrawerLayout.closeDrawers();
                   break;
               case 4:
                   Intent logout = new Intent(context, ActivityLogin.class);
                   logout.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
/*
    // not working for some reason
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    public Bitmap StringToBitMap(String encodedString){
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, scaleOptions);

        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= 100
                && scaleOptions.outHeight / scale / 2 >= 100) {
            scale *= 2;
        }

        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,outOptions);
    }

    /*
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }*/
}