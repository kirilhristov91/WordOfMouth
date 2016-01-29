package com.wordofmouth.Activities;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.wordofmouth.R;


public abstract class BaseActivity extends AppCompatActivity {

    protected boolean useToolbar() {
        return true;
    }

    ListView menuListView;
    private String[] drawerListViewItems;
    DrawerLayout mDrawerLayout;
    FrameLayout actContent;

    @Override
    public void setContentView(int layoutResID) {
        LinearLayout baseLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.base_layout, null);
        mDrawerLayout = (DrawerLayout) baseLayout.findViewById(R.id.drawer_layout);
        actContent = (FrameLayout) mDrawerLayout.findViewById(R.id.frame_container);

        View view = getLayoutInflater().inflate(layoutResID, null);
        actContent.addView(view);
        if (useToolbar()) {
            Toolbar toolbar = (Toolbar) baseLayout.findViewById(R.id.tool_bar);
            setSupportActionBar(toolbar);
            toolbar.setLogo(R.mipmap.ic_launcher);

            menuListView = (ListView) mDrawerLayout.findViewById(R.id.list_slidermenu);
            drawerListViewItems = getResources().getStringArray(R.array.menu_items);
            menuListView.setAdapter(new ArrayAdapter<String>(view.getContext(), R.layout.drawer_listview_item, drawerListViewItems));
        }

        setContentView(baseLayout);


////        // set the drawer layout as main content view of Activity.
////        setContentView(mDrawerLayout);
////        // add layout of BaseActivities inside framelayout.i.e. frame_container
//        getLayoutInflater().inflate(layoutResID, actContent, true);
//        if(useToolbar()) {
//            View view = getLayoutInflater().inflate(layoutResID, null);
//            Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
//            setSupportActionBar(toolbar);
//            super.setContentView(view);
//        } else {
//            super.setContentView(layoutResID);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean taken = false;
        switch (item.getItemId()) {
            case R.id.action_vili:
                taken = true;
                mDrawerLayout.openDrawer(GravityCompat.START);



                break;
        }
        return taken;
    }
}
