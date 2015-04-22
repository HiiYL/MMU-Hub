package com.github.hiiyl.mmuhub;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;
import com.github.hiiyl.mmuhub.sync.MMUSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class MainActivity extends ActionBarActivity {
    public static final String LOGGED_IN_PREF_TAG = "logged_in";
    private TextView welcome_text;
    private TextView faculty_text;
    private TextView student_id_textview;
    private TextView mmls_load_status;
    private Button pager_button;
    private MMUDbHelper mmuDbHelper;
    public static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // declare the dialog as a member field of your activity


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        mmuDbHelper = new MMUDbHelper(this);

        MMUSyncAdapter.initializeSyncAdapter(this);

        database = mmuDbHelper.getWritableDatabase();

        boolean logged_in = prefs.getBoolean(LOGGED_IN_PREF_TAG, false);


        if (!logged_in) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_main);
        welcome_text = (TextView) findViewById(R.id.welcome_text);
        faculty_text = (TextView) findViewById(R.id.faculty_text);
        student_id_textview = (TextView) findViewById(R.id.student_id_textview);
        pager_button = (Button) findViewById(R.id.pager_button);
        mmls_load_status = (TextView) findViewById(R.id.mmls_load_status);


        welcome_text.setText("Welcome, " + prefs.getString("name", ""));
        student_id_textview.setText(prefs.getString("student_id", ""));
        faculty_text.setText(prefs.getString("faculty", ""));
        pager_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MMLSActivity.class);
                startActivity(intent);
            }
        });
    }


    private void setupVariables() {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.action_refresh_token) {
            Utility.refreshToken(MainActivity.this);
        }
        if (id == R.id.action_logout) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
            finish();
            MMUDbHelper mOpenHelper = new MMUDbHelper(MainActivity.this);
            mOpenHelper.onLogout(database);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public static void getAnnouncementJSON(Context context) {
        final Context mContext = context;
        MMUDbHelper mOpenHelper = new MMUDbHelper(mContext);
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = "https://mmu-api.herokuapp.com/mmls_api";
//        String url = "https://mmu-api.herokuapp.com/login_test.json";
        Toast.makeText(context, "QUERYING DATA", Toast.LENGTH_SHORT).show();
        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("Fetching data from MMLS");
        mProgressDialog.setMessage("Establishing connection...");
        mProgressDialog.show();
//        mmls_load_status.setText("ATTEMPTING TO QUERY SERVER...");
        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    Cursor mCursor = null;
                    mProgressDialog.setMessage("Saving ...");
                    JSONArray jarray = new JSONArray(response);
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(jarray.length());
                    for(int i = 0; i < jarray.length(); i++)
                    {
                        JSONObject jobj = jarray.getJSONObject(i);
                        String subject_name = jobj.getString("name");

                        mCursor = database.query(MMUContract.SubjectEntry.TABLE_NAME,
                            new String[] {MMUContract.SubjectEntry.COLUMN_NAME, MMUContract.SubjectEntry._ID},
                            MMUContract.SubjectEntry.COLUMN_NAME + " = ? ",
                            new String[] {subject_name},
                            null,
                            null,
                            null
                            );
                        long subject_id;
                        if(!mCursor.moveToFirst()) {
                            ContentValues subjectValues = new ContentValues();
                            subjectValues.put(MMUContract.SubjectEntry.COLUMN_NAME, subject_name);
                            subject_id = database.insert(MMUContract.SubjectEntry.TABLE_NAME, null, subjectValues);
                        }
                        else {
                            subject_id = mCursor.getLong(mCursor.getColumnIndex(MMUContract.SubjectEntry._ID));
                        }
                        List<String> announcement_list = new ArrayList<String>();
                        JSONArray weeks = jobj.getJSONArray("weeks");
                        if(weeks != null && subject_id != -1) {
                            for(int j = 0; j < weeks.length(); j++) {
                                JSONObject week_obj = weeks.getJSONObject(j);
                                String week_title = week_obj.getString("title");
                                Log.d("WEEK TTILE", week_title);
                                String sql = "SELECT * FROM " + MMUContract.SubjectEntry.TABLE_NAME + ", " +
                                        MMUContract.WeekEntry.TABLE_NAME + " WHERE " +
                                        MMUContract.WeekEntry.TABLE_NAME + "."  +
                                        MMUContract.WeekEntry.COLUMN_SUBJECT_KEY + " = " +
                                        MMUContract.SubjectEntry.TABLE_NAME + "." +
                                        MMUContract.SubjectEntry._ID + " AND " +
                                        MMUContract.WeekEntry.TABLE_NAME + "." +
                                        MMUContract.WeekEntry.COLUMN_TITLE + " = ? " + " AND " +
                                        MMUContract.SubjectEntry.TABLE_NAME + "." +
                                        MMUContract.SubjectEntry._ID + " = ? ;";
                                mCursor = database.rawQuery(sql, new String[] {week_title, Long.toString(subject_id)});
                                long week_id;
                                if(!mCursor.moveToFirst()) {
                                    ContentValues weekValues = new ContentValues();
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_TITLE, week_title);
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_SUBJECT_KEY, subject_id);
                                    week_id = database.insert(MMUContract.WeekEntry.TABLE_NAME, null, weekValues);
                                }
                                else {
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
                                                new String[] {MMUContract.AnnouncementEntry._ID},
                                                MMUContract.AnnouncementEntry.COLUMN_TITLE + " = ? AND " +
                                                        MMUContract.AnnouncementEntry.COLUMN_CONTENTS + " = ?",
                                                new String[] {announcement_title, announcement_contents},
                                                null,
                                                null,
                                                null);
                                        if(!mCursor.moveToFirst()) {
                                            Log.d("ANNOUNCEMENT VALUE", "NOT UNIQUE");
                                            ContentValues announcementValues = new ContentValues();
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_TITLE, announcement_title);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_CONTENTS, announcement_contents);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_WEEK_KEY, week_id);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_AUTHOR, announcement_author);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE, announcement_posted_date);
                                            announcementValues.put(MMUContract.AnnouncementEntry.COLUMN_SUBJECT_KEY, subject_id);
                                            long _id = database.insert(MMUContract.AnnouncementEntry.TABLE_NAME, null, announcementValues);
                                        }
                                    }
                                }
                            }
                        }
                        JSONArray filesArray = jobj.getJSONArray("subject_files");
                        if(filesArray != null && subject_id != -1) {
                            for (int l = 0; l < filesArray.length(); l++) {
                                JSONObject file = filesArray.getJSONObject(l);
                                String file_name = file.getString("file_name");
                                String token = file.getString("token");
                                String content_id = file.getString("content_id");
                                String content_type = file.getString("content_type");
                                String remote_file_path = file.getString("file_path");
                                String sql = "SELECT * FROM " + MMUContract.FilesEntry.TABLE_NAME + ", " +
                                        MMUContract.SubjectEntry.TABLE_NAME + " WHERE " +
                                        MMUContract.FilesEntry.TABLE_NAME + "."  +
                                        MMUContract.FilesEntry.COLUMN_SUBJECT_KEY + " = " +
                                        MMUContract.SubjectEntry.TABLE_NAME + "." +
                                        MMUContract.SubjectEntry._ID + " AND " +
                                        MMUContract.FilesEntry.TABLE_NAME + "." +
                                        MMUContract.FilesEntry.COLUMN_NAME + " = ? " + " AND " +
                                        MMUContract.SubjectEntry.TABLE_NAME + "." +
                                        MMUContract.SubjectEntry._ID + " = ? ;";
                                mCursor = database.rawQuery(sql, new String[] {file_name, Long.toString(subject_id)});
                                if(!mCursor.moveToFirst()) {
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

                    }
                    mCursor.close();
                    mProgressDialog.dismiss();
                    Utility.refreshToken(mContext);

                }
                catch (JSONException e) {
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
                if(networkResponse != null && networkResponse.data != null){
                    switch(networkResponse.statusCode){
                        case 400:
                            json = new String(networkResponse.data);
                            json = Utility.trimMessage(json, "message");
                            if(json != null)
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
                                        MainActivity.getAnnouncementJSON(mContext);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                params.put("student_id", prefs.getString("student_id", ""));
                params.put("password", prefs.getString("mmls_password", ""));
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
    };
}
