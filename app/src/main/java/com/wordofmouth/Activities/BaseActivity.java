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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.R;
import com.wordofmouth.SharedPreferences.UserLocalStore;


public abstract class BaseActivity extends AppCompatActivity{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Context context;
    private ImageView menuProfilePicture;
    UserLocalStore userLocalStore;
    DBHandler dbHandler;

    protected boolean useMainToolbar() {
        return true;
    }

    @Override
    public void setContentView(int layoutResID) {
        LinearLayout baseLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.base_layout, null);
        mDrawerLayout = (DrawerLayout) baseLayout.findViewById(R.id.drawer_layout);
        FrameLayout actContent = (FrameLayout) mDrawerLayout.findViewById(R.id.frame_container);

        View view = getLayoutInflater().inflate(layoutResID, null);
        context = view.getContext();
        actContent.addView(view);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        userLocalStore = new UserLocalStore(context);
        dbHandler = DBHandler.getInstance(context);
        System.out.println("POLZVAM LI MAINABAR " + useMainToolbar());
        if (useMainToolbar()) {
            Toolbar toolbar = (Toolbar) baseLayout.findViewById(R.id.tool_bar);
            setSupportActionBar(toolbar);
            toolbar.setLogo(R.mipmap.ic_launcher);

            ListView menuListView = (ListView) mDrawerLayout.findViewById(R.id.list_slidermenu);
            String[] drawerListViewItems = getResources().getStringArray(R.array.menu_items);
            ArrayAdapter<String> menuAdapter= new CustomMenuItemAdapter(context, drawerListViewItems);
            menuListView.setAdapter(menuAdapter);
            menuListView.setOnItemClickListener(new DrawerItemClickListener());

            actionBarDrawerToggle = new ActionBarDrawerToggle(
                     this,                  /* host Activity */
                     mDrawerLayout,         /* DrawerLayout object */
                     R.string.drawer_open,  /* "open drawer" description */
                     R.string.drawer_close  /* "close drawer" description */
            );

            menuProfilePicture = (ImageView) mDrawerLayout.findViewById(R.id.menuProfilePicture);
            menuProfilePicture.setOnClickListener(new ProfilePictureClickListener());

            User currentUser = userLocalStore.getUserLoggedIn();
            String pic = dbHandler.getProfilePicture(currentUser.getId());
            if (pic != null) {
                Bitmap bitmap = StringToBitMap(pic);
                menuProfilePicture.setImageBitmap(getRoundedCornerBitmap(bitmap,100));
            }

            // Set actionBarDrawerToggle as the DrawerListener
            mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        }

        setContentView(baseLayout);
    }

    private class ProfilePictureClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.menuProfilePicture:
                    Intent myIntent = new Intent(context, ActivityProfile.class);
                    startActivity(myIntent);
                    mDrawerLayout.closeDrawers();
                    //obmisli go tova
                    //((Activity)context).finish();
                    break;
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
           switch (position){
               case 0:
                   Intent home = new Intent(context, MainActivity.class);
                   //home.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   startActivity(home);
                   mDrawerLayout.closeDrawers();
                   break;
               case 1:
                   Intent notifications = new Intent(context, ActivityNotifications.class);
                   //notifications.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   startActivity(notifications);
                   mDrawerLayout.closeDrawers();
                   break;
               case 2:
                   Intent feedback = new Intent(context, ActivityFeedback.class);
                   //feedback.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   startActivity(feedback);
                   mDrawerLayout.closeDrawers();
                   break;
               case 3:
                   Intent about = new Intent(context, ActivityAbout.class);
                   //about.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   startActivity(about);
                   mDrawerLayout.closeDrawers();
                   break;
               case 4:
                   Intent logout = new Intent(context, ActivityLogin.class);
                   //logout.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                   userLocalStore.clearUserData();
                   userLocalStore.setUserLoggedIn(false);
                   startActivity(logout);
                   mDrawerLayout.closeDrawers();
                   break;
           }

        }
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
    }

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
    }
}