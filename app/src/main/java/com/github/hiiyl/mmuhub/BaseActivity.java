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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.greenrobot.event.EventBus;


public class BaseActivity extends AppCompatActivity
{
    public DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private Handler mHandler;
    private Runnable mPendingRunnable;

    private Button mLogOutButton;
//    private ButtonFlat mTimetableButton;
//    private ButtonFlat mCamsysButton;

    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see

    String TITLES[] = {"MMLS","Bulletin", "Student Center", "Settings"};
    int ICONS[] = {R.drawable.ic_mmls,R.drawable.ic_bulletin, R.drawable.ic_group_black_36dp, R.drawable.ic_settings};

    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view

    ListView mListView;
    NavigationDrawerAdapter mAdapter;                        // Declaring Adapter For Recycler View

    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    protected void onCreateDrawer()
    {
        mListView = (ListView) findViewById(R.id.left_drawer); // Assigning the RecyclerView Object to the xml View

        View headerView = getLayoutInflater().inflate(R.layout.header,mListView, false);

        TextView student_name = (TextView) headerView.findViewById(R.id.student_name);
        TextView student_id = (TextView) headerView.findViewById(R.id.student_id);
        TextView faculty = (TextView) headerView.findViewById(R.id.faculty_text);

        mLogOutButton = (Button) findViewById(R.id.logout_button);


        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        student_name.setText(prefs.getString("name", ""));
        student_id.setText(prefs.getString("student_id", ""));
        faculty.setText(prefs.getString("faculty",""));

        mListView.addHeaderView(headerView, null, false);

        mAdapter = new NavigationDrawerAdapter(this,TITLES,ICONS);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mListView.setAdapter(mAdapter);

        mHandler = new Handler();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);        // Drawer object Assigned to the vie
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


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawer(Gravity.START);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                selectItem(position);

            }
        });

        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            Intent intent;
            @Override
            public void onClick(View v) {
//                SnackBar log_out_confirm = new SnackBar(BaseActivity.this, "Confirm log out?", "Yes",
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                mDrawerLayout.closeDrawer(Gravity.START);
//                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                                getSupportActionBar().setHomeButtonEnabled(true);
//                                SharedPreferences.Editor editor = prefs.edit();
//                                editor.putBoolean("logged_in", false);
//                                editor.apply();
//                                EventBus.getDefault().removeAllStickyEvents();
//
//                                intent = new Intent(BaseActivity.this, LoginActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                mPendingRunnable = new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                };
//
//                            }
//                        });
//                log_out_confirm.show();
            }
        });

    }

    private void selectItem(int position) {
        Log.d("HELLO", "COUNTED AS SELECTED AND POSITION IS " + position);
        Intent intent = null;
        switch (position) {
            case 0:
                break;
            case 1:
                intent = new Intent(this, MMLSActivity.class);
                break;
            case 2:
                intent = new Intent(this, BulletinActivity.class);
                break;
            case 3:
                intent = new Intent(this, StudentCenterActivity.class);
                break;
            case 4:
                intent = new Intent(this, SettingsActivity.class);
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
