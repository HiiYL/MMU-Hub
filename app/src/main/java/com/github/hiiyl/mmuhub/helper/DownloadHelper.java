package com.github.hiiyl.mmuhub.helper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.hiiyl.mmuhub.Utility;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hii on 4/22/15.
 */
public class DownloadHelper {

    public static void updateSubject(final Context context, final String subject_id) {
        final String subject_url, subject_name;
        final Context mContext = context;
        MMUDbHelper mOpenHelper = new MMUDbHelper(mContext);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor = db.query(MMUContract.SubjectEntry.TABLE_NAME,
                null,
                MMUContract.SubjectEntry._ID + " = ? ",
                new String[]{subject_id}, null, null, null);
        if (cursor.moveToFirst()) {
            RequestQueue queue = Volley.newRequestQueue(mContext);
            String url = "https://mmu-api.herokuapp.com/refresh_subject";
            subject_url = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_URL));
            subject_name = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));

            cursor.close();

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
                            String subject_name = jobj.getString("name");

                            mCursor = db.query(MMUContract.SubjectEntry.TABLE_NAME,
                                    new String[]{MMUContract.SubjectEntry.COLUMN_NAME, MMUContract.SubjectEntry._ID},
                                    MMUContract.SubjectEntry.COLUMN_NAME + " = ? ",
                                    new String[]{subject_name},
                                    null,
                                    null,
                                    null
                            );
                            long subject_id;
                            if (!mCursor.moveToFirst()) {
                                ContentValues subjectValues = new ContentValues();
                                subjectValues.put(MMUContract.SubjectEntry.COLUMN_NAME, subject_name);
                                subject_id = db.insert(MMUContract.SubjectEntry.TABLE_NAME, null, subjectValues);
                            } else {
                                subject_id = mCursor.getLong(mCursor.getColumnIndex(MMUContract.SubjectEntry._ID));
                            }
                            List<String> announcement_list = new ArrayList<String>();
                            JSONArray weeks = jobj.getJSONArray("weeks");
                            if (weeks != null && subject_id != -1) {
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
                                    mCursor = db.rawQuery(sql, new String[]{week_title, Long.toString(subject_id)});
                                    long week_id;
                                    if (!mCursor.moveToFirst()) {
                                        ContentValues weekValues = new ContentValues();
                                        weekValues.put(MMUContract.WeekEntry.COLUMN_TITLE, week_title);
                                        weekValues.put(MMUContract.WeekEntry.COLUMN_SUBJECT_KEY, subject_id);
                                        week_id = db.insert(MMUContract.WeekEntry.TABLE_NAME, null, weekValues);
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
                                            mCursor = db.query(MMUContract.AnnouncementEntry.TABLE_NAME,
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
                                                long _id = db.insert(MMUContract.AnnouncementEntry.TABLE_NAME, null, announcementValues);
                                            }
                                        }
                                    }
                                }
                            }
                            JSONArray filesArray = jobj.getJSONArray("subject_files");
                            if (filesArray != null && subject_id != -1) {
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
                                    mCursor = db.rawQuery(sql, new String[]{file_name, Long.toString(subject_id)});
                                    if (!mCursor.moveToFirst()) {
                                        ContentValues fileValues = new ContentValues();
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_NAME, file_name);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_TOKEN, token);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_ID, content_id);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE, content_type);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH, remote_file_path);
                                        fileValues.put(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY, subject_id);
                                        long _id = db.insert(MMUContract.FilesEntry.TABLE_NAME, null, fileValues);
                                    }

                                }
                            }

                        mCursor.close();
                        mProgressDialog.dismiss();
                        Utility.refreshToken(mContext);

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
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        switch (networkResponse.statusCode) {
                            case 400:
                                json = new String(networkResponse.data);
                                json = Utility.trimMessage(json, "message");
                                if (json != null)
                                    Toast.makeText(mContext, json, Toast.LENGTH_SHORT).show();
                                mProgressDialog.setMessage(json);
//                                mmls_load_status.setText(json);
                                break;
                            default:
//                            progressBar.setVisibility(View.INVISIBLE);
                                mProgressDialog.setMessage("NO INTERNET CONNECTION");
                                Toast.makeText(mContext, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
//                            mmls_load_status.setText("NO INTERNET CONNECTION");
                        }
                        //Additional cases
                    }
//                else
//                {
//                    progressBar.setVisibility(View.INVISIBLE);

//                    mmls_load_status.setText("NO INTERNET CONNECTION");
//                }
                    AlertDialog.Builder alertDialogBuilder =
                            new AlertDialog.Builder(mContext)
                                    .setTitle("Connection Error")
                                    .setMessage("An error has occurred with the MMLS query.")
                                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            DownloadHelper.updateSubject(context, subject_id);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alertDialog = alertDialogBuilder.show();
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
        ;
    }
}
