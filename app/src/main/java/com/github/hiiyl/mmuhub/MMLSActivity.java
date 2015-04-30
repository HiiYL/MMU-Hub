package com.github.hiiyl.mmuhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.helper.SyncEvent;
import com.github.hiiyl.mmuhub.sync.MMUSyncAdapter;

import java.util.ArrayList;
import java.util.Locale;

import de.greenrobot.event.EventBus;


public class MMLSActivity extends BaseActivity{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    public static final String LOGGED_IN_PREF_TAG = "logged_in";
    private static ButtonFloat mDownloadButton;
    private static int mPosition = 1;
    private static String DOWNLOAD_TAG = "download_notes";
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        final ActionBar actionBar = getActionBar();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mmls);

        super.onCreateDrawer();

        mDownloadButton = (ButtonFloat)findViewById(R.id.lecture_notes_download);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(subjectHasFiles(0)) {
                    mDownloadButton.show();
                }else {
                    mDownloadButton.hide();
                }
            }
        }, 200);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);



        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String str_pos = Integer.toString(position + 1);
                if(subjectHasFiles(position)) {
                    Log.d("SHOW", "EXISTS AND SHOWING " + str_pos);
                    mDownloadButton.show();
                }
                else {
                    Log.d("SHOW", "NOT EXISTS AND HIDING " + str_pos);
                    mDownloadButton.hide();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MMLSActivity.this, DownloadActivity.class);
                intent.putExtra("SUBJECT_ID", MMLSActivity.mViewPager.getCurrentItem()+ 1);
                startActivity(intent);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        boolean logged_in = prefs.getBoolean(LOGGED_IN_PREF_TAG, false);
        if (!logged_in) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            MMUSyncAdapter.initializeSyncAdapter(this);
        }
    }
    private boolean subjectHasFiles(int position) {
        String pos = Integer.toString(position + 1);
        boolean hasFiles;
        Cursor cursor = MySingleton.getInstance(MMLSActivity.this).getDatabase().query(MMUContract.FilesEntry.TABLE_NAME, null,
                MMUContract.FilesEntry.COLUMN_SUBJECT_KEY + " = ? ",
                new String[] {pos}, null, null, null);
        if(cursor.moveToFirst()) {
            hasFiles = true;
        }else {
            hasFiles = false;
        }
        cursor.close();
        return hasFiles;

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEventMainThread(SyncEvent event){
        if(event.message.equals(Utility.SYNC_FINISHED)) {
            Log.d("SYNC COMPLETE", "COMPLETE");
            SnackBar sync_notify = new SnackBar(this, "Sync Complete");
            sync_notify.show();
        }else if(event.message.equals(Utility.SYNC_BEGIN)) {
            SnackBar sync_notify = new SnackBar(this, "Syncing ...");
            sync_notify.show();
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_mml, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public class SectionsPagerAdapter extends FragmentPagerAdapter {


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return MMLSFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            Cursor cursor =MySingleton.getInstance(MMLSActivity.this).getDatabase().query(MMUContract.SubjectEntry.TABLE_NAME, null, null, null, null, null, null);
            int count = cursor.getCount();
            cursor.close();
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String subject_id = Integer.toString(position + 1);
            Cursor cursor = MySingleton.getInstance(MMLSActivity.this).getDatabase().query(MMUContract.SubjectEntry.TABLE_NAME, new String[]{MMUContract.SubjectEntry.COLUMN_NAME}, MMUContract.AnnouncementEntry._ID + "=?", new String[]{subject_id}, null, null, null);
            ArrayList<String> names = new ArrayList<String>();
            if(cursor.moveToFirst()) {
                String subject_name = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));
                cursor.close();
                return subject_name;

            }
            else {
                cursor.close();
                Locale l = Locale.getDefault();
                switch (position) {
                    case 0:
                        //return MainActivity.subject_names.get(0);
                        return getString(R.string.title_section1).toUpperCase(l);
                    case 1:
                        //return MainActivity.subject_names.get(1);
                        return getString(R.string.title_section2).toUpperCase(l);
                    case 2:
                        //return MainActivity.subject_names.get(2);
                        return getString(R.string.title_section3).toUpperCase(l);

                      }
            }
           return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */



}
