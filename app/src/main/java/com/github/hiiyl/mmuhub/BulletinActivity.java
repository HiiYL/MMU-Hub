package com.github.hiiyl.mmuhub;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;
import com.github.hiiyl.mmuhub.helper.DownloadCompleteEvent;
import com.github.hiiyl.mmuhub.helper.ViewEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;


public class BulletinActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new BulletinFragment())
                    .commit();
        }
        super.onCreateDrawer();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_bulletin, menu);
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
     * A placeholder fragment containing a simple view.
     */
    public static class BulletinFragment extends Fragment {
        public static String BULLETIN_SYNC_COMPLETE = "Bulletin Sync Complete";
        Cursor mCursor;
        RequestQueue mRequestQueue;
        MMUDbHelper mOpenHelper;
        ListView mBulletinListView;
        BulletinAdapter mAdapter;
        SwipeRefreshLayout mSwipeRefreshLayout;

        public BulletinFragment() {

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
        public void onEventMainThread(DownloadCompleteEvent event) {
            if(event.message.equals(BULLETIN_SYNC_COMPLETE)) {
//                SnackBar new_snackbar = new SnackBar(getActivity(), event.message);
//                new_snackbar.show();
                Cursor newCursor = MySingleton.getInstance(getActivity()).getDatabase().query(
                        MMUContract.BulletinEntry.TABLE_NAME, null, null, null, null, null, null);
                mAdapter.changeCursor(newCursor);
            }
        }
        public void onEventMainThread(ViewEvent event) {
            if(event.message.equals(Utility.VIEW_BULLETIN_EVENT)) {
                Log.d("ANNOUNCEMENT", "BULLETIN VIEW EVENT RECEIVED");
                Cursor newCursor = MySingleton.getInstance(getActivity()).getDatabase().query(
                        MMUContract.BulletinEntry.TABLE_NAME, null, null, null, null, null, null);
                mAdapter.changeCursor(newCursor);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_bulletin, container, false);
            mRequestQueue = MySingleton.getInstance(getActivity()).getRequestQueue();

            mOpenHelper = new MMUDbHelper(getActivity());
            mBulletinListView = (ListView) rootView.findViewById(R.id.bulletin_listview);
            mCursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.BulletinEntry.TABLE_NAME, null, null, null, null, null, null);
            mAdapter = new BulletinAdapter(getActivity(), mCursor, 0);
            mBulletinListView.setAdapter(mAdapter);

            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.bulletin_activity_swipe_refresh);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    updateBulletin(getActivity());
                }
            });
            mBulletinListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), BulletinDetailActivity.class);
                    intent.putExtra("BULLETIN_ID", Integer.toString(position + 1));
                    startActivity(intent);
                }
            });
            return rootView;
        }

        public void updateBulletin(final Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Log.d("UPDATE Bulletin", "UPDATING BULLETIN");
            final Context mContext = context;
            final MMUDbHelper mOpenHelper = new MMUDbHelper(mContext);
            Cursor cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.BulletinEntry.TABLE_NAME, null, null, null, null, null, null);
                String url = "https://mmu-api.co/bulletin_api";
                mSwipeRefreshLayout.setRefreshing(true);
                StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                                try {
                                    Log.d("RECEIVE", "RECEIVED");
                                    JSONArray bulletin_array = new JSONArray(response);
                                    for (int i = 0; i < bulletin_array.length(); i++) {
                                        JSONObject bulletin_obj = bulletin_array.getJSONObject(i);
                                        String bulletin_title = bulletin_obj.getString("title");
                                        String bulletin_posted_date = bulletin_obj.getString("posted_date");
                                        String bulletin_author = bulletin_obj.getString("author");
                                        String bulletin_contents = bulletin_obj.getString("contents");


                                        Cursor cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.BulletinEntry.TABLE_NAME,
                                                null, MMUContract.BulletinEntry.COLUMN_TITLE + " = ? AND " + MMUContract.BulletinEntry.COLUMN_POSTED_DATE + " = ? "
                                                , new String[]{bulletin_title, bulletin_posted_date}, null, null, null);
                                        if (!cursor.moveToFirst()) {
                                            ContentValues bulletinValues = new ContentValues();
                                            bulletinValues.put(MMUContract.BulletinEntry.COLUMN_TITLE, bulletin_title);
                                            bulletinValues.put(MMUContract.BulletinEntry.COLUMN_POSTED_DATE, bulletin_posted_date);
                                            bulletinValues.put(MMUContract.BulletinEntry.COLUMN_CONTENTS, bulletin_contents);
                                            bulletinValues.put(MMUContract.BulletinEntry.COLUMN_AUTHOR, bulletin_author);
                                            MySingleton.getInstance(getActivity()).getDatabase().insert(MMUContract.BulletinEntry.TABLE_NAME, null, bulletinValues);
                                        }
                                    }
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    EventBus.getDefault().post(new DownloadCompleteEvent(BULLETIN_SYNC_COMPLETE));

                                } catch (JSONException exception) {
                                    exception.printStackTrace();
                                }
                    }
                }, new Response.ErrorListener() {
                    String json = null;
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Attendance Update", "ITS DEAD JIM");
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                        params.put("cookie", prefs.getString("cookie", ""));
                        return params;
                    }
                };
            sr.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    0,
                    0));
            MySingleton.getInstance(getActivity()).addToRequestQueue(sr);
        }
    }
}
