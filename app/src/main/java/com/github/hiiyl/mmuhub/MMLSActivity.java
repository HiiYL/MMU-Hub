package com.github.hiiyl.mmuhub;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gc.materialdesign.views.ButtonFloat;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MMLSActivity extends ActionBarActivity{

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
    private static ButtonFloat mDownloadButton;
    private static int mPosition = 1;
    private static RequestQueue queue;
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

        queue = Volley.newRequestQueue(MMLSActivity.this);
        mDownloadButton = (ButtonFloat)findViewById(R.id.lecture_notes_download);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MMLSActivity.this, DownloadActivity.class);
                intent.putExtra("SUBJECT_ID", MMLSActivity.mViewPager.getCurrentItem()+ 1);
                startActivity(intent);
            }
        });


        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(subjectHasFiles(position))
                    mDownloadButton.show();
                else
                    mDownloadButton.hide();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    private static boolean subjectHasFiles(int position) {
        String pos = Integer.toString(position + 1);
        boolean hasFiles;
        Cursor cursor = MainActivity.database.query(MMUContract.FilesEntry.TABLE_NAME, null,
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
            String subject_id = Integer.toString(position + 1);
            Cursor cursor = MainActivity.database.query(MMUContract.SubjectEntry.TABLE_NAME, new String[] {MMUContract.SubjectEntry.COLUMN_NAME}, MMUContract.AnnouncementEntry._ID + "=?",new String[] {subject_id}, null ,null, null);
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
    public static class PlaceholderFragment extends Fragment{
        private ExpandableListView mExListView;
        private SwipeRefreshLayout mSwipeRefreshLayout;
        private MMLSAdapter mAdapter;
        private Cursor cursor;
        String slide_str;
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
//            if(subjectHasFiles(MMLSActivity.mViewPager.getCurrentItem())) {
//                mDownloadButton.show();
//                Log.d("Button", "Subject Has Files");
//            }
//            else {
//                mDownloadButton.hide();
//                Log.d("Button", "Subject Has No Files");
//            }
        }





        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_mml, container, false);

            mOpenHelper = new MMUDbHelper(getActivity());
            int slide = getArguments().getInt(ARG_SECTION_NUMBER, 0);
            slide_str = Integer.toString(slide);
            mExListView = (ExpandableListView) rootView.findViewById(R.id.listview_expandable_mmls);
            cursor = MainActivity.database.query(MMUContract.WeekEntry.TABLE_NAME, null, "subject_id = ?",new String[] {slide_str},null,null,null);
            mAdapter = new MMLSAdapter(cursor, getActivity());
            mExListView.setAdapter(mAdapter);
            mExListView.expandGroup(0);

            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.MMLS_activity_swipe_refresh);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                         @Override
                                                         public void onRefresh() {
                                                             refreshContent(getActivity(), Integer.toString(MMLSActivity.mViewPager.getCurrentItem() + 1));
                                                         }
                                                     });


            mExListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    // CursorAdapter returns a cursor at the correct position for getItem(), or null
                    // if it cannot seek to that position.
                    Cursor new_cursor = (Cursor)mAdapter.getChild(
                            groupPosition, childPosition);
                    if (new_cursor != null) {
                        String announcement_id = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.AnnouncementEntry._ID));
                        Intent intent = new Intent(getActivity(), AnnouncementDetailActivity.class);
                        intent.putExtra("ANNOUNCEMENT_ID", announcement_id);
                        startActivity(intent);
                    }
                    return true;
                }
            });


            return rootView;
        }
        private void refreshContent(Context context, String subject_id) {
            updateSubject(context, subject_id);
        }
        public void updateSubject(final Context context, final String subject_id) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            final String subject_url, subject_name;
            final Context mContext = context;
            final MMUDbHelper mOpenHelper = new MMUDbHelper(mContext);
            Cursor cursor = MainActivity.database.query(MMUContract.SubjectEntry.TABLE_NAME,
                    null,
                    MMUContract.SubjectEntry._ID + " = ? ",
                    new String[]{subject_id}, null, null, null);
            if (cursor.moveToFirst()) {

                String url = "https://mmu-api.herokuapp.com/refresh_subject";
                subject_url = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_URL));
                subject_name = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));
                Log.d("SENT DATA COOKIE",prefs.getString("cookie", ""));
                Log.d("SENT DATA URI", subject_url);

                final ProgressDialog mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setTitle("Fetching data from MMLS");
                mProgressDialog.setMessage("Downloading latest updates for " + subject_name);
                mProgressDialog.show();
                StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Cursor mCursor = null;
                            mProgressDialog.setMessage("Saving ...");
                            JSONObject jobj = new JSONObject(response);
                            List<String> announcement_list = new ArrayList<String>();
                            JSONArray weeks = jobj.getJSONArray("weeks");
                            if (weeks != null) {
                                for (int j = 0; j < weeks.length(); j++) {
                                    JSONObject week_obj = weeks.getJSONObject(j);
                                    String week_title = week_obj.getString("title");
                                    Log.d("WEEK TTILE", week_title);
                                    String sql = "SELECT * FROM " + MMUContract.SubjectEntry.TABLE_NAME + ", " +
                                            MMUContract.WeekEntry.TABLE_NAME + " WHERE " +
                                            MMUContract.WeekEntry.TABLE_NAME + "." +
                                            MMUContract.WeekEntry.COLUMN_SUBJECT_KEY + " = " +
                                            MMUContract.SubjectEntry.TABLE_NAME + "." +
                                            MMUContract.SubjectEntry._ID + " AND " +
                                            MMUContract.WeekEntry.TABLE_NAME + "." +
                                            MMUContract.WeekEntry.COLUMN_TITLE + " = ? " + " AND " +
                                            MMUContract.SubjectEntry.TABLE_NAME + "." +
                                            MMUContract.SubjectEntry._ID + " = ? ;";
                                    mCursor = MainActivity.database.rawQuery(sql, new String[]{week_title, subject_id});
                                    long week_id;
                                    if (!mCursor.moveToFirst()) {
                                        ContentValues weekValues = new ContentValues();
                                        weekValues.put(MMUContract.WeekEntry.COLUMN_TITLE, week_title);
                                        weekValues.put(MMUContract.WeekEntry.COLUMN_SUBJECT_KEY, subject_id);
                                        week_id = MainActivity.database.insert(MMUContract.WeekEntry.TABLE_NAME, null, weekValues);
                                    } else {
                                        week_id = mCursor.getLong(mCursor.getColumnIndex(MMUContract.WeekEntry._ID));
                                    }

                                    JSONArray announcements = week_obj.getJSONArray("announcements");
                                    if (announcements != null && week_id != -1) {
                                        for (int k = 0; k < announcements.length(); k++) {

                                            JSONObject announcement = announcements.getJSONObject(k);
                                            String announcement_title = announcement.getString("title");
                                            String announcement_contents = announcement.getString("contents");
                                            String announcement_author = announcement.getString("author");
                                            String announcement_posted_date = announcement.getString("posted_date");
                                            mCursor = MainActivity.database.query(MMUContract.AnnouncementEntry.TABLE_NAME,
                                                    new String[]{MMUContract.AnnouncementEntry._ID},
                                                    MMUContract.AnnouncementEntry.COLUMN_TITLE + " = ? AND " +
                                                            MMUContract.AnnouncementEntry.COLUMN_CONTENTS + " = ?",
                                                    new String[]{announcement_title, announcement_contents},
                                                    null,
                                                    null,
                                                    null);
                                            if (!mCursor.moveToFirst()) {
                                                Log.d("ANNOUNCEMENT VALUE", "NOT UNIQUE");
                                                ContentValues announcementValues = new ContentValues();
                                                announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_TITLE, announcement_title);
                                                announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_CONTENTS, announcement_contents);
                                                announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_WEEK_KEY, week_id);
                                                announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_AUTHOR, announcement_author);
                                                announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE, announcement_posted_date);
                                                announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_SUBJECT_KEY, subject_id);
                                                long _id = MainActivity.database.insert(MMUContract.AnnouncementEntry.TABLE_NAME, null, announcementValues);
                                            }
                                        }
                                    }
                                }
                            }
                            JSONArray filesArray = jobj.getJSONArray("subject_files");
                            if (filesArray != null) {
                                for (int l = 0; l < filesArray.length(); l++) {
                                    JSONObject file = filesArray.getJSONObject(l);
                                    String file_name = file.getString("file_name");
                                    String token = file.getString("token");
                                    String content_id = file.getString("content_id");
                                    String content_type = file.getString("content_type");
                                    String remote_file_path = file.getString("file_path");
                                    String sql = "SELECT * FROM " + MMUContract.FilesEntry.TABLE_NAME + ", " +
                                            MMUContract.SubjectEntry.TABLE_NAME + " WHERE " +
                                            MMUContract.FilesEntry.TABLE_NAME + "." +
                                            MMUContract.FilesEntry.COLUMN_SUBJECT_KEY + " = " +
                                            MMUContract.SubjectEntry.TABLE_NAME + "." +
                                            MMUContract.SubjectEntry._ID + " AND " +
                                            MMUContract.FilesEntry.TABLE_NAME + "." +
                                            MMUContract.FilesEntry.COLUMN_NAME + " = ? " + " AND " +
                                            MMUContract.SubjectEntry.TABLE_NAME + "." +
                                            MMUContract.SubjectEntry._ID + " = ? ;";
                                    mCursor = MainActivity.database.rawQuery(sql, new String[]{file_name,subject_id});
                                    if (!mCursor.moveToFirst()) {
                                        ContentValues fileValues = new ContentValues();
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_NAME, file_name);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_TOKEN, token);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_ID, content_id);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE, content_type);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH, remote_file_path);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY, subject_id);
                                        long _id = MainActivity.database.insert(MMUContract.FilesEntry.TABLE_NAME, null, fileValues);
                                    }

                                }
                            }
                            Cursor new_cursor = MainActivity.database.query(MMUContract.WeekEntry.TABLE_NAME, null, "subject_id = ?",new String[] {slide_str},null,null,null);
                            mAdapter.changeCursor(new_cursor);
                            mAdapter.notifyDataSetChanged();
                            mProgressDialog.dismiss();
                            mSwipeRefreshLayout.setRefreshing(false);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    String json = null;

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.setTitle("An Error has occurred");
                        mProgressDialog.dismiss();
                        mSwipeRefreshLayout.setRefreshing(false);
                        NetworkResponse networkResponse = error.networkResponse;
                        AlertDialog.Builder alertDialogBuilder;
                        AlertDialog alertDialog;
                        if (networkResponse != null && networkResponse.data != null) {
                            switch (networkResponse.statusCode) {
                                case 400:
                                    queue.cancelAll(DOWNLOAD_TAG);
                                    alertDialogBuilder =
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle("Connection Error")
                                                    .setMessage("Cookie has Expired")
                                                    .setPositiveButton("Refresh Cookie and Retry", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            refreshTokenAndRetry(context,subject_id);
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                    alertDialog = alertDialogBuilder.show();
                                    break;
                                default:
                                    alertDialogBuilder =
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle("Connection Error")
                                                    .setMessage("Internal Server Error")
                                                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            updateSubject(context, subject_id);
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                    alertDialog = alertDialogBuilder.show();
                            }
                            //Additional cases
                        }
                        else {
                            alertDialogBuilder =
                                    new AlertDialog.Builder(mContext)
                                            .setTitle("Connection Error")
                                            .setMessage("Internet Connection Error")
                                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    updateSubject(context, subject_id);
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                            alertDialog = alertDialogBuilder.show();
                        }
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                        params.put("cookie", prefs.getString("cookie", ""));
                        params.put("subject_url", subject_url);
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/x-www-form-urlencoded");
                        return headers;
                    }
                };
                sr.setRetryPolicy(new DefaultRetryPolicy(
                        30000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                sr.setTag(DOWNLOAD_TAG);
                queue.add(sr);
            }
        }
        public void refreshTokenAndRetry(final Context context, final String subject_id) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Refreshing Token & Cookie...");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            String url = "https://mmu-api.herokuapp.com/refresh_token";
//        String url = "https://mmu-api.herokuapp.com/login_test.json";
            StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    SharedPreferences.Editor editor = prefs.edit();
                    String cookie = Utility.trimMessage(response, "cookie");
                    String token = Utility.trimMessage(response, "token");
                    editor.putString("cookie", cookie);
                    editor.putString("token", token);
                    editor.apply();
                    Log.d("Token", "Successful");
                    progressDialog.dismiss();
                    updateSubject(context, subject_id);

                }
            }, new Response.ErrorListener() {
                String json = null;
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Log.d("Token", "Refresh unsuccessful");
                }
            }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("student_id", prefs.getString("student_id", ""));
                    params.put("password", prefs.getString("mmls_password",""));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<String, String>();
                    headers.put("Content-Type","application/x-www-form-urlencoded");
                    headers.put("abc", "value");
                    return headers;
                }
            };
            sr.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(sr);
        }
    }

}
