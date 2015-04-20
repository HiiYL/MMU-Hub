package com.github.hiiyl.mmuhub;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;

import java.util.ArrayList;
import java.util.Locale;


public class MMLSActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    private static MMUDbHelper mOpenHelper;
    private static SQLiteDatabase db;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final ActionBar actionBar = getActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mmls);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mml, menu);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            mOpenHelper = new MMUDbHelper(MMLSActivity.this);
            db = mOpenHelper.getReadableDatabase();
            String subject_id = Integer.toString(position + 1);
            Cursor cursor = db.query(MMUContract.SubjectEntry.TABLE_NAME, new String[] {MMUContract.SubjectEntry.COLUMN_NAME}, MMUContract.AnnouncementEntry._ID + "=?",new String[] {subject_id}, null ,null, null);
            ArrayList<String> names = new ArrayList<String>();
            if(cursor.moveToFirst()) {
                String subject_name = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));
                cursor.close();
                Log.d("MYAPP", "CURSOR IS VALID AND POSITION IS " + Integer.toString(position));
                return subject_name;

            }
            else {
                cursor.close();
                Log.d("MYAPP", "CURSOR IS INVALID AND POSITION IS " + Integer.toString(position));
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
    public static class PlaceholderFragment extends Fragment {
        private ListView mListView;
        MMLSAdapter mmls_adapter;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_mml, container, false);
            mOpenHelper = new MMUDbHelper(getActivity());
            db = mOpenHelper.getReadableDatabase();
            int slide = getArguments().getInt(ARG_SECTION_NUMBER, 0);
            String slide_str = Integer.toString(slide);
            mListView = (ListView) rootView.findViewById(R.id.listview_mmls);
            final Cursor cursor = db.query(MMUContract.AnnouncementEntry.TABLE_NAME, null, "subject_id = ?",new String[] {slide_str},null,null,null);
            mmls_adapter = new MMLSAdapter(getActivity(), cursor, 0);
            mListView.setAdapter(mmls_adapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // CursorAdapter returns a cursor at the correct position for getItem(), or null
                    // if it cannot seek to that position.
                    Cursor new_cursor = (Cursor) adapterView.getItemAtPosition(position);
                    if (new_cursor != null) {
                        String id = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.AnnouncementEntry._ID));
                        Intent intent = new Intent(getActivity(), AnnouncementDetailActivity.class);
                        intent.putExtra("ANNOUNCEMENT_ID", id);
                        startActivity(intent);
                    }
                }
            });


//            if(MainActivity.annoucement_list_array.size() > (getArguments().getInt(ARG_SECTION_NUMBER, 0) - 1)) {
//                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
//                        getActivity(),
//                        android.R.layout.simple_list_item_1,
//                        MainActivity.annoucement_list_array.get(getArguments().getInt(ARG_SECTION_NUMBER, 0) - 1));
//                lv.setAdapter(arrayAdapter);
//            }

            return rootView;
        }
    }

}
