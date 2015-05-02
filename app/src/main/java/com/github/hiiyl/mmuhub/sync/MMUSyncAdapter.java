package com.github.hiiyl.mmuhub.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.hiiyl.mmuhub.AnnouncementDetailActivity;
import com.github.hiiyl.mmuhub.BulletinActivity;
import com.github.hiiyl.mmuhub.MySingleton;
import com.github.hiiyl.mmuhub.R;
import com.github.hiiyl.mmuhub.Utility;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;
import com.github.hiiyl.mmuhub.helper.DownloadCompleteEvent;
import com.github.hiiyl.mmuhub.helper.SyncEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by Hii on 4/22/15.
 */
public class MMUSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String SYNC_FINISHED = "sync_finished";
    public static final String SYNC_STARTING = "sync_starting";
    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final int ANNOUNCEMENT_NOTIFICATION_ID = 3004;

    private int mNumberOfSubjects = Utility.getSubjectCount(getContext());
    private int mSubjectSyncCount = 0;

    private SQLiteDatabase database;
    private RequestQueue sync_queue;
    MMUDbHelper helper;
    String SYNC_TAG = "Sync Downloads";
    final String LOG_TAG = MMUSyncAdapter.class.getSimpleName();
    public MMUSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        if(Utility.isFirstSync(getContext())) {
            Log.d("FIRST SYNC", "FIRST SYNC");
            EventBus.getDefault().postSticky(new SyncEvent(Utility.SYNC_BEGIN));
        }
        sync_queue = MySingleton.getInstance(getContext()).
                getRequestQueue();
        Log.d(LOG_TAG, "onPerformSync Called.");
        helper = new MMUDbHelper(getContext());
        database = helper.getWritableDatabase();
        String s = "1";
        Cursor cursor= database.query(MMUContract.SubjectEntry.TABLE_NAME, null, null, null,null,null,null);
        if(cursor.moveToFirst()) {
            for (int i = 1; i < cursor.getCount() + 1; i++)
                updateSubject(getContext(), Integer.toString(i));
        }
        updateBulletin(getContext());
    }
    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MMUSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }
    public void updateSubject(final Context context, final String subject_id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final String subject_url, subject_name;
        final Context mContext = context;
        final MMUDbHelper mOpenHelper = new MMUDbHelper(mContext);
        Cursor cursor = database.query(MMUContract.SubjectEntry.TABLE_NAME,
                null,
                MMUContract.SubjectEntry._ID + " = ? ",
                new String[]{subject_id}, null, null, null);
        if (cursor.moveToFirst()) {
            String url = "https://mmu-api.co/refresh_subject";
            subject_url = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_URL));
            subject_name = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));
            Log.d("SENT DATA COOKIE", prefs.getString("cookie", ""));
            Log.d("SENT DATA URI", subject_url);

            StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    AsyncTask.execute(new Runnable() {
                        public void run() {
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
                                        mCursor = database.rawQuery(sql, new String[]{week_title, subject_id});
                                        long week_id;
                                        if (!mCursor.moveToFirst()) {
                                            ContentValues weekValues = new ContentValues();
                                            weekValues.put(MMUContract.WeekEntry.COLUMN_TITLE, week_title);
                                            weekValues.put(MMUContract.WeekEntry.COLUMN_SUBJECT_KEY, subject_id);
                                            week_id = database.insert(MMUContract.WeekEntry.TABLE_NAME, null, weekValues);
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
                                                mCursor = database.query(MMUContract.AnnouncementEntry.TABLE_NAME,
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
                                                    announcement_id = database.insert(MMUContract.AnnouncementEntry.TABLE_NAME, null, announcementValues);

                                                    String max_posted_date_query = "SELECT MAX(" + MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE + ") FROM " + MMUContract.AnnouncementEntry.TABLE_NAME;
                                                    Cursor posted_date_cursor = MySingleton.getInstance(context).getDatabase().query(
                                                            MMUContract.AnnouncementEntry.TABLE_NAME, new String[] {MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE},
                                                            null, null, null, null, MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE + " DESC ", "1"
                                                            );

                                                    if(Utility.getNotificationsEnabled(context) && posted_date_cursor.moveToFirst()) {
                                                        String latest_posted_date = posted_date_cursor.getString(posted_date_cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE));
                                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                        Log.d("POSTED_DATE", posted_date_cursor.getString(posted_date_cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE)));
                                                        Log.d("IS AFTER", Boolean.toString(sdf.parse(announcement_posted_date).before(sdf.parse(latest_posted_date))));
                                                        if (!sdf.parse(announcement_posted_date).before(sdf.parse(latest_posted_date))) {
                                                            Intent resultIntent = new Intent(getContext(), AnnouncementDetailActivity.class);
                                                            resultIntent.putExtra("ANNOUNCEMENT_ID", Long.toString(announcement_id));
                                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
                                                            stackBuilder.addParentStack(AnnouncementDetailActivity.class);
                                                            stackBuilder.addNextIntent(resultIntent);
                                                            PendingIntent resultPendingIntent =
                                                                    stackBuilder.getPendingIntent(
                                                                            0,
                                                                            PendingIntent.FLAG_UPDATE_CURRENT
                                                                    );
                                                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getContext())
                                                                    .setSmallIcon(R.drawable.ic_mmls)
                                                                    .setContentTitle(announcement_title)
                                                                    .setContentText(announcement_author);
                                                            notificationBuilder.setContentIntent(resultPendingIntent);
                                                            NotificationManager manager =
                                                                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                                            notificationBuilder.setAutoCancel(true);
                                                            manager.notify(ANNOUNCEMENT_NOTIFICATION_ID, notificationBuilder.build());
                                                        }
                                                    }
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
                                                    mCursor = database.rawQuery(sql, new String[]{file_name, Long.toString(announcement_id)});
                                                    if(!mCursor.moveToFirst()) {
                                                        ContentValues fileValues = new ContentValues();
                                                        fileValues.put(MMUContract.FilesEntry.COLUMN_NAME, file_name);
                                                        fileValues.put(MMUContract.FilesEntry.COLUMN_TOKEN, token);
                                                        fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_ID, content_id);
                                                        fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE, content_type);
                                                        fileValues.put(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH, remote_file_path);
                                                        fileValues.put(MMUContract.FilesEntry.COLUMN_ANNOUNCEMENT_KEY, announcement_id);
                                                        fileValues.put(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY, subject_id);
                                                        long _id = database.insert(MMUContract.FilesEntry.TABLE_NAME, null, fileValues);
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
                                        mCursor = database.rawQuery(sql, new String[]{file_name, subject_id});
                                        if (!mCursor.moveToFirst()) {
                                            ContentValues fileValues = new ContentValues();
                                            fileValues.put(MMUContract.FilesEntry.COLUMN_NAME, file_name);
                                            fileValues.put(MMUContract.FilesEntry.COLUMN_TOKEN, token);
                                            fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_ID, content_id);
                                            fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE, content_type);
                                            fileValues.put(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH, remote_file_path);
                                            fileValues.put(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY, subject_id);
                                            long _id = database.insert(MMUContract.FilesEntry.TABLE_NAME, null, fileValues);


                                        }

                                    }
                                }

                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                            }
                            mSubjectSyncCount++;
                            if (mSubjectSyncCount == mNumberOfSubjects) {
                                EventBus.getDefault().postSticky(new SyncEvent(Utility.SYNC_FINISHED));
                                mSubjectSyncCount = 0;
                            }
                            // database insert here
                        }
                    });
                        }
                    }, new Response.ErrorListener() {
                        String json = null;

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse != null && networkResponse.data != null) {
                                switch (networkResponse.statusCode) {
                                    case 400:
                                        Log.d("HELLO THERE~", "HTTTP 400");
                                        sync_queue.cancelAll(SYNC_TAG);
                                        refreshTokenAndRetry(context, subject_id);
                                        break;
                                    default:
                                }
                                //Additional cases
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
                    0));
            sr.setTag(SYNC_TAG);
            sync_queue.add(sr);
        }
    }
    public void refreshTokenAndRetry(final Context context, final String subject_id) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String url = "https://mmu-api.co/refresh_token";
        Log.d("HELLO THERE~", "PREPARING TO REFRESH TOKEN");
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
                MMUSyncAdapter.syncImmediately(getContext());

            }
        }, new Response.ErrorListener() {
            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
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
                0,
                0));
        sync_queue.add(sr);
    }
    public void updateBulletin(final Context context) {
        Log.d("UPDATE Bulletin", "UPDATING BULLETIN");
        String url = "https://mmu-api.co/bulletin_api";
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("RECEIVE", "RECEIVED");
                    JSONArray bulletin_array = new JSONArray(response);
                    for (int i = 0; i < bulletin_array.length(); i++) {
                        JSONObject bulletin_obj = bulletin_array.getJSONObject(i);
                        String bulletin_title = bulletin_obj.getString("title");
                        String bulletin_posted_date = bulletin_obj.getString("posted_date");
                        String bulletin_author = bulletin_obj.getString("author");
                        String bulletin_contents = bulletin_obj.getString("contents");


                        Cursor cursor = MySingleton.getInstance(context).getDatabase().query(MMUContract.BulletinEntry.TABLE_NAME,
                                null, MMUContract.BulletinEntry.COLUMN_TITLE + " = ? AND " + MMUContract.BulletinEntry.COLUMN_POSTED_DATE + " = ? "
                                , new String[]{bulletin_title, bulletin_posted_date}, null, null, null);
                        if (!cursor.moveToFirst()) {
                            ContentValues bulletinValues = new ContentValues();
                            bulletinValues.put(MMUContract.BulletinEntry.COLUMN_TITLE, bulletin_title);
                            bulletinValues.put(MMUContract.BulletinEntry.COLUMN_POSTED_DATE, bulletin_posted_date);
                            bulletinValues.put(MMUContract.BulletinEntry.COLUMN_CONTENTS, bulletin_contents);
                            bulletinValues.put(MMUContract.BulletinEntry.COLUMN_AUTHOR, bulletin_author);
                            MySingleton.getInstance(context).getDatabase().insert(MMUContract.BulletinEntry.TABLE_NAME, null, bulletinValues);
                        }
                    }
                    EventBus.getDefault().post(new DownloadCompleteEvent(BulletinActivity.BulletinFragment.BULLETIN_SYNC_COMPLETE));
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        sr.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(context).addToRequestQueue(sr);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
