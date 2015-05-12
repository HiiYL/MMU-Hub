package com.github.hiiyl.mmuhub;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.helper.AttendanceCompleteEvent;
import com.github.hiiyl.mmuhub.helper.StartPreviousActivityEvent;

import java.util.Locale;

import de.greenrobot.event.EventBus;


public class StudentCenterActivity extends BaseActivity  {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_center);
        super.onCreateDrawer();

        if(!Utility.isCamsysLoggedIn(this)) {
            Utility.camsysAuthenticate(this);
        }else {
            Utility.refreshAttendance(this);
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    public void onEventMainThread(AttendanceCompleteEvent event) {
        mSectionsPagerAdapter.notifyDataSetChanged();
    }
    public void onEventMainThread(StartPreviousActivityEvent event) {
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_student_center, menu);
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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position) {
                case 0:
                    return PlaceholderFragment.newInstance(position + 1);
                case 1:
                    return FeesDueFragment.newInstance(position +1);
                case 2:
                    return ExamTimetableFragment.newInstance(position + 1);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "ATTENDANCE";
                case 1:
                    return "FEES DUE";
                case 2:
                    return "EXAM TIMETABLE";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment{
        private AttendanceAdapter mAdapter;
        private ListView mAttendanceListView;
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
//        @Override
//        public void onStart() {
//            super.onStart();
//            EventBus.getDefault().register(this);
//        }
//
//        @Override
//        public void onStop() {
//            EventBus.getDefault().unregister(this);
//            super.onStop();
//        }

//        public void onEventMainThread(AttendanceCompleteEvent event) {
//            Cursor newCursor = MySingleton.getInstance(getActivity()).getDatabase().query(
//                    MMUContract.SubjectEntry.TABLE_NAME, null ,
//                    null, null, null, null, null
//            );
//            mAdapter.changeCursor(newCursor);
//        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_student_center, container, false);
            mAttendanceListView = (ListView)rootView.findViewById(R.id.listview_attendance);
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Cursor cursor = MySingleton.getInstance(getActivity()).getDatabase().query(
                    MMUContract.SubjectEntry.TABLE_NAME, null ,
                    null, null, null, null, null
            );

            mAdapter = new AttendanceAdapter(getActivity(), cursor, 0);
            mAttendanceListView.setAdapter(mAdapter);

            return rootView;
        }
    }

}
