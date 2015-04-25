package com.github.hiiyl.mmuhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class BaseActivity extends AppCompatActivity
{
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    public String[] layers;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> mArrayAdapter;
    private String mActivityTitle;
    private Handler mHandler;
    private Runnable mPendingRunnable;


    protected void onCreateDrawer()
    {
        // R.id.drawer_layout should be in every activity with exactly the same id.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mHandler = new Handler();

        mActivityTitle = getTitle().toString();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.drawer_open);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                if (mPendingRunnable != null) {
                    mHandler.post(mPendingRunnable);
                    mPendingRunnable = null;
                }else {
                    getSupportActionBar().setTitle(mActivityTitle);
                }
            }
        };
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setHomeButtonEnabled(true);

        layers = getResources().getStringArray(R.array.layers_array);
        mDrawerList = (ListView) findViewById(R.id.navList);
//        View header = getLayoutInflater().inflate(R.layout.drawer_list_header, null);
//        mDrawerList.addHeaderView(header, null, false);

        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, layers);

        mDrawerList.setAdapter(mArrayAdapter);
//        View footerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
//                R.layout.drawer_list_footer, null, false);
//        mDrawerList.addFooterView(footerView);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
                mDrawerLayout.closeDrawer(Gravity.START);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                selectItem(pos);
            }
        });
        View logoutView = getLayoutInflater().inflate(R.layout.drawer_list_item, null);
//        View headerView = getLayoutInflater().inflate(R.layout.nav_drawer_header, null);
        TextView textView = (TextView)logoutView.findViewById(R.id.nav_drawer_footer_textview);
        textView.setText("Log Out");
        mDrawerList.addFooterView(logoutView);
//        mDrawerList.addHeaderView(headerView);

    }

    private void selectItem(int position) {
        Log.d("HELLO", "COUNTED AS SELECTED AND POSITION IS " + position);
        Intent intent = null;
        switch (position) {
            case 0:
                intent = new Intent(this, MainActivity.class);
                break;
            case 1:
                intent = new Intent(this, MMLSActivity.class);
                break;
            case 2:
                break;
            case 3:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged_in", false);
                editor.apply();
                intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                break;
            default:
                break;
        }
        if(intent != null) {
            final Intent finalIntent = intent;
            mPendingRunnable = new Runnable() {
                @Override
                public void run() {
                    startActivity(finalIntent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            };
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
