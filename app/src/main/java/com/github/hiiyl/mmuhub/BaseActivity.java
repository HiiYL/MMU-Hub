package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;


public class BaseActivity extends AppCompatActivity
{
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    public String[] layers;
    private LinearLayout mDrawerLinear;
    private ArrayAdapter<String> mArrayAdapter;
    private String mActivityTitle;
    private Handler mHandler;
    private Runnable mPendingRunnable;

    private ButtonFlat mLogOutButton;
//    private ButtonFlat mTimetableButton;
//    private ButtonFlat mCamsysButton;

    private TextView mNameTextView;
    private TextView mFacultyTextView;
    private TextView mStuIDTextView;

    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see

    String TITLES[] = {"MMLS","Bulletin"};
    int ICONS[] = {R.drawable.ic_mmls,R.drawable.ic_bulletin};

    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view

    String NAME = "Hii Yong Lian";
    String EMAIL = "yonglian146@gmail.com";
    int PROFILE = R.drawable.nav_drawer_img;

    private Toolbar toolbar;                              // Declaring the Toolbar Object

    ListView mListView;

//    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    NavigationDrawerAdapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout

    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    private Context mContext;



    protected void onCreateDrawer()
    {
        mListView = (ListView) findViewById(R.id.left_drawer); // Assigning the RecyclerView Object to the xml View

        View headerView = getLayoutInflater().inflate(R.layout.header,mListView, false);

        TextView student_name = (TextView) headerView.findViewById(R.id.student_name);
        TextView student_id = (TextView) headerView.findViewById(R.id.student_id);
        TextView faculty = (TextView) headerView.findViewById(R.id.faculty_text);

        mLogOutButton = (ButtonFlat) findViewById(R.id.logout_button);


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






//        mDrawerLinear = (LinearLayout) findViewById(R.id.left_drawer);
//        mNameTextView = (TextView)mDrawerLinear.findViewById(R.id.student_name_textview);
//        mStuIDTextView = (TextView)mDrawerLinear.findViewById(R.id.student_id_textview);
//        mFacultyTextView = (TextView) mDrawerLinear.findViewById(R.id.faculty_textview);

//        mMMLSButton = (ButtonFlat) mDrawerLinear.findViewById(R.id.MMLS_button);
//        mBulletinButton = (ButtonFlat) mDrawerLinear.findViewById(R.id.Bulletin_button);
//        mLogOutButton = (ButtonFlat) mDrawerLinear.findViewById(R.id.Logout_Button);


//        mNameTextView.setText(prefs.getString("name", ""));
//        mStuIDTextView.setText(prefs.getString("student_id", ""));
//        mFacultyTextView.setText(prefs.getString("faculty",""));

//        mMMLSButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawerLayout.closeDrawer(mDrawerLinear);
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                getSupportActionBar().setHomeButtonEnabled(true);
//                final Intent intent = new Intent( BaseActivity.this, MMLSActivity.class);
//                mPendingRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        startActivity(intent);
//                        overridePendingTransition(0, 0);
//                        finish();
//                    }
//                };
//                onCreateDrawer();
//            }
//        });
//        mBulletinButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawerLayout.closeDrawer(mDrawerLinear);
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                getSupportActionBar().setHomeButtonEnabled(true);
//                final Intent intent = new Intent( BaseActivity.this, BulletinActivity.class);
//                mPendingRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        startActivity(intent);
//                        overridePendingTransition(0, 0);
//                        finish();
//                    }
//                };
//                onCreateDrawer();
//            }
//        });
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(Gravity.START);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged_in", false);
                editor.apply();
                final Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                };

            }
        });



//        layers = getResources().getStringArray(R.array.layers_array);
//        mDrawerList = (ListView) findViewById(R.id.navList);
//        View header = getLayoutInflater().inflate(R.layout.drawer_list_header, null);
//        mDrawerList.addHeaderView(header, null, false);

//        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, layers);

//        mDrawerList.setAdapter(mArrayAdapter);
//        View footerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
//                R.layout.drawer_list_footer, null, false);
//        mDrawerList.addFooterView(footerView);

//        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
//                mDrawerLayout.closeDrawer(Gravity.START);
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                getSupportActionBar().setHomeButtonEnabled(true);
//                selectItem(pos);
//            }
//        });
//        View logoutView = View.inflate(this,R.layout.drawer_list_item, null);
//        View headerView = getLayoutInflater().inflate(R.layout.nav_drawer_header, null);
//        TextView textView = (TextView)logoutView.findViewById(R.id.nav_drawer_footer_textview);
//        textView.setText("Log Out");
//        mDrawerList.addFooterView(logoutView);
//        mDrawerList.addHeaderView(headerView);

    }

    private void selectItem(int position) {
        Log.d("HELLO", "COUNTED AS SELECTED AND POSITION IS " + position);
        Intent intent = null;
        switch (position) {
            case 0:
//                intent = new Intent(this, MainActivity.class);
                break;
            case 1:
                intent = new Intent(this, MMLSActivity.class);
                break;
            case 2:
                intent = new Intent(this, BulletinActivity.class);
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
