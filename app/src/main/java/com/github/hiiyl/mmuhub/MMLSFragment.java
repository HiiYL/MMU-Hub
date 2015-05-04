package com.github.hiiyl.mmuhub;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;
import com.github.hiiyl.mmuhub.helper.FileOpen;
import com.github.hiiyl.mmuhub.helper.SyncEvent;
import com.github.hiiyl.mmuhub.helper.ViewEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import de.greenrobot.event.EventBus;

/**
 * Created by Hii on 4/23/15.
 */
public class MMLSFragment extends Fragment {
    private static final String DOWNLOAD_TAG = "download";
    private ExpandableListView mExListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MMLSAdapter mAdapter;
    private Cursor cursor;
    private MMUDbHelper mOpenHelper;
    private RequestQueue requestQueue;
    private ProgressBar mProgressBar;
    private TextView mInteractionPromptText;
    private int mReceivedCount = 0;
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
    public static MMLSFragment newInstance(int sectionNumber) {
        MMLSFragment fragment = new MMLSFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MMLSFragment() {
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

    // This method will be called when a MessageEvent is posted
    public void onEventMainThread(SyncEvent event){
        if(event.message.equals(Utility.SYNC_FINISHED)) {
            Log.d("SYNC COMPLETE", "COMPLETE");
            mSwipeRefreshLayout.setRefreshing(false);
            cursor = MySingleton.getInstance(getActivity()).getDatabase().
                    query(MMUContract.WeekEntry.TABLE_NAME, null, "subject_id = ?", new String[]{slide_str}, null, null, null);
            mAdapter.changeCursor(cursor);
            mExListView.expandGroup(0);
            EventBus.getDefault().removeStickyEvent(event);
        }else if(event.message.equals(Utility.SYNC_BEGIN)) {
            Log.d("SYNC STARTING", "NOTIFICATINO RECEIVED");
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }
    public void onEventMainThread(ViewEvent event) {
        if(event.message.equals(Utility.VIEW_ANNOUNCEMENT_EVENT)) {
            Log.d("ANNOUNCEMENT", "VIEW EVENT RECEIVED");
            mAdapter.notifyDataSetChanged();
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requestQueue = MySingleton.getInstance(getActivity()).getRequestQueue();

        View rootView = inflater.inflate(R.layout.fragment_mml, container, false);

        mOpenHelper = new MMUDbHelper(getActivity());
        int slide = getArguments().getInt(ARG_SECTION_NUMBER, 0);
        slide_str = Integer.toString(slide);
        mExListView = (ExpandableListView) rootView.findViewById(R.id.listview_expandable_mmls);
        cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.WeekEntry.TABLE_NAME, null, "subject_id = ?", new String[]{slide_str}, null, null, null);
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
                v.setHasTransientState(true);
                TextView snippet =  (TextView)v.findViewById(R.id.listitem_mmls_snippet);
                LinearLayout layout = (LinearLayout)v.findViewById(R.id.announcement_detail_download_layout);
                if(snippet.getLineCount() == 1) {
                    snippet.setSingleLine(false);
                    snippet.setEllipsize(null);
                    Cursor has_seen_cursor = mAdapter.getChild(
                            groupPosition, childPosition);
                    ContentValues has_seen = new ContentValues();
                    has_seen.put(MMUContract.AnnouncementEntry.COLUMN_HAS_SEEN, true);
                    MySingleton.getInstance(getActivity()).getDatabase().update(MMUContract.AnnouncementEntry.TABLE_NAME,
                            has_seen, MMUContract.AnnouncementEntry._ID + "=?",
                            new String[]{has_seen_cursor.getString(has_seen_cursor.getColumnIndex(MMUContract.AnnouncementEntry._ID))});
                    String attachment_file_query =
                            "SELECT * FROM " + MMUContract.FilesEntry.TABLE_NAME +
                                    " WHERE " + MMUContract.FilesEntry.COLUMN_ANNOUNCEMENT_KEY + " = " +
                                    has_seen_cursor.getLong(has_seen_cursor.getColumnIndex(MMUContract.AnnouncementEntry._ID));
                    final Cursor new_cursor = MySingleton.getInstance(getActivity()).getDatabase().rawQuery(attachment_file_query, null);
                    has_seen_cursor.close();
                    if(new_cursor.moveToFirst()) {
                        TextView attachment_view = (TextView) v.findViewById(R.id.announcement_detail_attachment_name);
                        mInteractionPromptText = (TextView) v.findViewById(R.id.announcement_detail_attachment_interaction_prompt);
                        mProgressBar = (ProgressBar) v.findViewById(R.id.announcement_detail_attachment_progressbar);


                        layout.setVisibility(View.VISIBLE);
                        attachment_view.setText(new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME)));
                        final String file_name = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME));


                        String subject_name_query = "SELECT " + MMUContract.SubjectEntry.COLUMN_NAME
                                + " FROM " + MMUContract.SubjectEntry.TABLE_NAME + " WHERE " + MMUContract.SubjectEntry._ID + " = " +
                                new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY));

                        Log.d("ID OF SUBJECT IS ", Long.toString(new_cursor.getLong(new_cursor.getColumnIndex(MMUContract.FilesEntry._ID))));
                        Cursor subject_cursor = MySingleton.getInstance(getActivity()).getDatabase().rawQuery(subject_name_query, null);
                        subject_cursor.moveToFirst();
                        String mSubjectName = subject_cursor.getString(subject_cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));
                        final String file_path = Environment.getExternalStorageDirectory().getPath() + "/" + Utility.DOWNLOAD_FOLDER + "/" + mSubjectName + "/" + Utility.ANNOUNCEMENT_ATTACHMENT_FOLDER + "/" + file_name;
                        final String file_directory = Environment.getExternalStorageDirectory().getPath() + "/" + Utility.DOWNLOAD_FOLDER + "/" + mSubjectName + "/"  + Utility.ANNOUNCEMENT_ATTACHMENT_FOLDER + "/";
                        File file = new File(file_path);
                        if(file.exists()) {
                            mInteractionPromptText.setText("Tap to View");
                        }else {
                            mInteractionPromptText.setText("Tap to Download");
                        }

                        layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("FILE NAME", file_name);
                                Log.d("FILE PATH", file_path);
                                File file = new File(file_path);
                                if(file.exists()) {
                                    try {
                                        FileOpen.openFile(getActivity(), file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    if(Utility.isNetworksAvailable(getActivity())) {
                                        mProgressBar.setVisibility(View.VISIBLE);
                                        File temp = new File(file_directory);
                                        temp.mkdirs();
                                        final DownloadTask downloadTask = new DownloadTask(getActivity());
                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                        downloadTask.file_name = file_name;
                                        downloadTask.content_type = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE));
                                        downloadTask.content_id = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_ID));
                                        downloadTask.remote_file_path = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH));
                                        downloadTask.token = prefs.getString("token", "");
                                        downloadTask.cookie = "laravel_session=" + prefs.getString("cookie", "");
                                        downloadTask.local_file_path = file_path;

                                        Log.d("APP", "Now downloading " + downloadTask.file_name);
                                        downloadTask.execute("https://mmls.mmu.edu.my/form-download-content");
                                    }

                                }

                            }
                        });
                    }
                mAdapter.notifyDataSetChanged();
                }else {
                    layout.setVisibility(View.GONE);
                    snippet.setSingleLine(true);
                    snippet.setEllipsize(TextUtils.TruncateAt.END);
                }

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
//                Cursor new_cursor = mAdapter.getChild(
//                        groupPosition, childPosition);
//                if (new_cursor != null) {
//                    String announcement_id = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.AnnouncementEntry._ID));
//                    Intent intent = new Intent(getActivity(), AnnouncementDetailActivity.class);
//                    intent.putExtra("ANNOUNCEMENT_ID", announcement_id);
//                    startActivity(intent);
//                }
                return true;
            }
        });
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mExListView.setIndicatorBounds(screenWidth-72, screenWidth);
        } else {
            mExListView.setIndicatorBoundsRelative(screenWidth-72, screenWidth);
        }
        return rootView;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String local_file_path;
        private String cookie;
        private String content_id;
        private String file_name;
        private String content_type;
        private String token;
        private String remote_file_path;
        private HttpsURLConnection connection = null;

        public DownloadTask(Context context) {
            this.context = context;
        }


        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
//

            try {
                URL url = new URL(sUrl[0]);
                Log.d("Download Cookie", cookie);
                Log.d("Download FileName", file_name);
                Log.d("Download ContentType", content_type);
                Log.d("Download ContentID", content_id);
                Log.d("Download Token", token);
                connection = (HttpsURLConnection) url.openConnection();
//                connection.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//                connection.setRequestProperty("Accept-Encoding","gzip, deflate");
//                connection.setRequestProperty("Cache-Control","max-age=0");
//                connection.setRequestProperty("Connection","keep-alive");
//                connection.setRequestProperty("Content-Length","693");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary6IMihbtBLkOsS4fR");
                connection.setRequestProperty("Cookie", cookie);
//                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
                connection.setRequestMethod("POST");
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(1000);
//                connection.connect();
                String payload = "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"_token\"\n" +
                        "\n" +
                        token + "\n" +
                        "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"content_id\"\n" +
                        "\n" +
                        content_id + "\n" +
                        "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"file_path\"\n" +
                        "\n" +
                        remote_file_path + "\n" +
                        "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"file_name\"\n" +
                        "\n" +
                        file_name + "\n" +
                        "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"content_type\"\n" +
                        "\n" +
                        content_type + "\n" +
                        "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"btnsubmit\"\n" +
                        "\n" +
                        "\n" +
                        "------WebKitFormBoundary6IMihbtBLkOsS4fR--";
                OutputStream os = connection.getOutputStream();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
                pw.write(payload);
                pw.flush();
                pw.close();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("CONNECT", " FAILED AND RESPONSE CODE IS " + connection.getResponseCode());
                    if (connection.getResponseCode() == 302) {
                        Utility.refreshToken(context);
                    }
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

//                String file_path = Environment.getExternalStorageDirectory().getPath() + "/" + file_name;
                Log.d("FILE PATH", local_file_path);

                output = new FileOutputStream(local_file_path);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
            mProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            if (result != null) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                mProgressBar.setMax(100);
                mProgressBar.setProgress(100);
            } else {
                mInteractionPromptText.setText("Tap to View");
            }
        }
    }

    private void refreshContent(Context context, String subject_id) {
        updateSubject(context, subject_id);
    }
    public void updateSubject(final Context context, final String subject_id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final String subject_url, subject_name;
        final Context mContext = context;
        final MMUDbHelper mOpenHelper = new MMUDbHelper(mContext);
        Cursor cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.SubjectEntry.TABLE_NAME,
                null,
                MMUContract.SubjectEntry._ID + " = ? ",
                new String[]{subject_id}, null, null, null);
        if (cursor.moveToFirst()) {
            String url = "https://mmu-api.co/refresh_subject";
            subject_url = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_URL));
            subject_name = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));

            mSwipeRefreshLayout.setRefreshing(true);

            StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Cursor mCursor = null;
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
                                mCursor = MySingleton.getInstance(getActivity()).getDatabase().rawQuery(sql, new String[]{week_title, subject_id});
                                long week_id;
                                if (!mCursor.moveToFirst()) {
                                    ContentValues weekValues = new ContentValues();
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_TITLE, week_title);
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_SUBJECT_KEY, subject_id);
                                    week_id = MySingleton.getInstance(getActivity()).getDatabase().insert(MMUContract.WeekEntry.TABLE_NAME, null, weekValues);
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
                                        mCursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.AnnouncementEntry.TABLE_NAME,
                                                new String[]{MMUContract.AnnouncementEntry._ID},
                                                MMUContract.AnnouncementEntry.COLUMN_TITLE + " = ? AND " +
                                                        MMUContract.AnnouncementEntry.COLUMN_CONTENTS + " = ?",
                                                new String[]{announcement_title, announcement_contents},
                                                null,
                                                null,
                                                null);
                                        long announcement_id;
                                        if (!mCursor.moveToFirst()) {
                                            Log.d("ANNOUNCEMENT VALUE", "NOT UNIQUE");
                                            ContentValues announcementValues = new ContentValues();
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_TITLE, announcement_title);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_CONTENTS, announcement_contents);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_WEEK_KEY, week_id);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_AUTHOR, announcement_author);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE, announcement_posted_date);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_SUBJECT_KEY, subject_id);
                                            announcement_id = MySingleton.getInstance(getActivity()).getDatabase().insert(MMUContract.AnnouncementEntry.TABLE_NAME, null, announcementValues);
                                        }else {
                                            announcement_id = mCursor.getLong(mCursor.getColumnIndex(MMUContract.AnnouncementEntry._ID));
                                        }
                                        JSONArray announcement_files = announcement.getJSONArray("subject_files");
                                        for(int l = 0; l < announcement_files.length(); l++) {
                                            Log.d("HELO THERE", "HAS FILES " + Long.toString(announcement_id) + " AND SUBJECT ID IS " + subject_id);
                                            JSONObject file = announcement_files.getJSONObject(l);
                                            String file_name = file.getString("file_name");
                                            String token = file.getString("token");
                                            String content_id = file.getString("content_id");
                                            String content_type = file.getString("content_type");
                                            String remote_file_path = file.getString("file_path");
                                            String raw_sql = "SELECT * FROM " + MMUContract.FilesEntry.TABLE_NAME + ", " +
                                                    MMUContract.SubjectEntry.TABLE_NAME + " WHERE " +
                                                    MMUContract.FilesEntry.TABLE_NAME + "." +
                                                    MMUContract.FilesEntry.COLUMN_SUBJECT_KEY + " = " +
                                                    MMUContract.AnnouncementEntry.TABLE_NAME + "." +
                                                    MMUContract.AnnouncementEntry._ID + " AND " +
                                                    MMUContract.FilesEntry.TABLE_NAME + "." +
                                                    MMUContract.FilesEntry.COLUMN_NAME + " = ? " + " AND " +
                                                    MMUContract.AnnouncementEntry.TABLE_NAME + "." +
                                                    MMUContract.AnnouncementEntry._ID + " = ? ;";
                                            mCursor = MySingleton.getInstance(getActivity()).getDatabase().rawQuery(sql, new String[]{file_name, Long.toString(announcement_id)});
                                            if(!mCursor.moveToFirst()) {
                                                ContentValues fileValues = new ContentValues();
                                                fileValues.put(MMUContract.FilesEntry.COLUMN_NAME, file_name);
                                                fileValues.put(MMUContract.FilesEntry.COLUMN_TOKEN, token);
                                                fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_ID, content_id);
                                                fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE, content_type);
                                                fileValues.put(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH, remote_file_path);
                                                fileValues.put(MMUContract.FilesEntry.COLUMN_ANNOUNCEMENT_KEY, announcement_id);
                                                fileValues.put(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY, subject_id);
                                                long _id = MySingleton.getInstance(getActivity()).getDatabase().insert(MMUContract.FilesEntry.TABLE_NAME, null, fileValues);
                                            }

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
                                mCursor = MySingleton.getInstance(getActivity()).getDatabase().rawQuery(sql, new String[]{file_name, subject_id});
                                if (!mCursor.moveToFirst()) {
                                    ContentValues fileValues = new ContentValues();
                                    fileValues.put(MMUContract.FilesEntry.COLUMN_NAME, file_name);
                                    fileValues.put(MMUContract.FilesEntry.COLUMN_TOKEN, token);
                                    fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_ID, content_id);
                                    fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE, content_type);
                                    fileValues.put(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH, remote_file_path);
                                    fileValues.put(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY, subject_id);
                                    long _id = MySingleton.getInstance(getActivity()).getDatabase().insert(MMUContract.FilesEntry.TABLE_NAME, null, fileValues);
                                }

                            }
                        }
                        Cursor new_cursor = MySingleton.getInstance(getActivity()).
                                getDatabase().query(MMUContract.WeekEntry.TABLE_NAME, null, "subject_id = ?",
                                new String[]{slide_str}, null, null, null);
                        mAdapter.changeCursor(new_cursor);
                        mSwipeRefreshLayout.setRefreshing(false);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                String json = null;

                @Override
                public void onErrorResponse(VolleyError error) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        switch (networkResponse.statusCode) {
                            case 400:
                                requestQueue.cancelAll(DOWNLOAD_TAG);
                                SnackBar snackbar = new SnackBar(getActivity(), "Your session cookie has expired.",
                                        "Refresh and Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        refreshTokenAndRetry(getActivity(), subject_id);
                                    }
                                });
                                snackbar.show();
                                break;
                            default:
                                SnackBar new_snackbar = new SnackBar(getActivity(), "Internal Server Error",
                                        "Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        updateSubject(getActivity(), subject_id);
                                    }
                                });
                                new_snackbar.show();
                        }
                        //Additional cases
                    }
                    else {
                        SnackBar new_snackbar = new SnackBar(getActivity(), "No Internet Connection",
                                "Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateSubject(getActivity(), subject_id);
                            }
                        });
                        new_snackbar.show();
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
            requestQueue.add(sr);
        }
    }
    public void refreshTokenAndRetry(final Context context, final String subject_id) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Refreshing Token & Cookie...");
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        String url = "https://mmu-api.co/refresh_token";
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
        requestQueue.add(sr);
    }
}