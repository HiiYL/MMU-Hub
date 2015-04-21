package com.github.hiiyl.mmuhub;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.Vector;


public class MainActivity extends ActionBarActivity {
    public static final String LOGGED_IN_PREF_TAG = "logged_in";
    private TextView welcome_text;
    private TextView faculty_text;
    private TextView student_id_textview;
    private TextView mmls_load_status;
    private Button pager_button;
    private Button download_button;
    private ProgressBar progressBar;

    private Cursor mCursor;

    private ProgressDialog mProgressDialog;


    static ArrayList<List<String>> annoucement_list_array = new ArrayList<List<String>>();
    static ArrayList<String> subject_names = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // declare the dialog as a member field of your activity


// instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean logged_in = prefs.getBoolean(LOGGED_IN_PREF_TAG, false);

        if(!logged_in) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_main);
        welcome_text = (TextView) findViewById(R.id.welcome_text);
        faculty_text = (TextView) findViewById(R.id.faculty_text);
        student_id_textview = (TextView) findViewById(R.id.student_id_textview);
        pager_button = (Button) findViewById(R.id.pager_button);
        progressBar = (ProgressBar) findViewById(R.id.mmls_load_progressbar);
        mmls_load_status = (TextView) findViewById(R.id.mmls_load_status);
        download_button = (Button) findViewById(R.id.download_button);


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
        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                downloadTask.execute("https://mmls.mmu.edu.my/form-download-content");

//                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        downloadTask.cancel(true);
//                    }
//                });
            }
        });



// execute this when the downloader must be fired

        //getAnnouncementJSON();
    }
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            String cookie = "laravel_session=eyJpdiI6ImZGZXM4MFwvazc0c1E3N0tKZTFqQWFnPT0iLCJ2YWx1ZSI6IkdSVlVlcjMxbCtqSWl0eFlPdXZ6dmNQRWlpcXhpREVUTml2OHFtdnpPaWdvZzduZ25mTGxGSzdRd0hpNzZCNG4rc2ZEMlNTcXJWa1JhOUJXUUhNNTV3PT0iLCJtYWMiOiI2OWEzZDUxNjhlOGYwNTFiNmUxNzFlYjliZmEwM2JlZTM2NjY3ZTc3YzBiMDJiY2ZlNzNhMGI3YTI0OTQ0NjhhIn0%3D"
            String content_id = "1205";
            String file_name = "Lec8.pdf";
            String content_type = "5";
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Accept-Encoding","gzip, deflate");
                connection.setRequestProperty("Cache-Control","max-age=0");
                connection.setRequestProperty("Connection","keep-alive");
                connection.setRequestProperty("Content-Length","693");
                connection.setRequestProperty("Content-Type","multipart/form-data; boundary=----WebKitFormBoundary6IMihbtBLkOsS4fR");
                connection.setRequestProperty("Cookie",
                connection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
                connection.setRequestMethod("POST");
                connection.connect();
                String payload = "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"_token\"\n" +
                        "\n" +
                        "P8QvN9q9bpNMSsCLkSEmaKPcEBrPy7UzllZmNbqS\n" +
                        "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"content_id\"\n" +
                        "\n" +
                        content_id + "\n" +
                        "------WebKitFormBoundary6IMihbtBLkOsS4fR\n" +
                        "Content-Disposition: form-data; name=\"file_path\"\n" +
                        "\n" +
                        "CYBER/TPT1201/notes\n" +
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
                Log.d("HTTP URL CINNECT","STREAM FLUSHED AND CLOSED");

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("DownloadTask","CONNECTION ERROR");
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream("/sdcard/Lec8.pdf");

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
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mProgressDialog.setMessage("Please wait... Authenticating");
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setMessage("Downloading...");
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            Log.d("FD","Updating Progress");
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            mWakeLock.release();
            mProgressDialog.dismiss();
            Log.d("FD","DOWNLOAD COMPLETE");
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
        }
    };

    private void setupVariables() {

    }

    private void downloadNotes() {
        MMUDbHelper mOpenHelper = new MMUDbHelper(this);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://mmls.mmu.edu.my/form-download-content";
        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    Toast.makeText(MainActivity.this, "TRYING TO SUBMIT", Toast.LENGTH_SHORT);
                    JSONArray jarray = new JSONArray(response);
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(jarray.length());
                    for(int i = 0; i < jarray.length(); i++)
                    {
                        JSONObject jobj = jarray.getJSONObject(i);
                        String subject_name = jobj.getString("name");

                        mCursor = db.query(MMUContract.SubjectEntry.TABLE_NAME,
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
                            subject_id = db.insert(MMUContract.SubjectEntry.TABLE_NAME, null, subjectValues);
                        }
                        else {
                            subject_id = mCursor.getLong(mCursor.getColumnIndex(MMUContract.SubjectEntry._ID));
                        }
                        //TODO REMOVE USELESS WEEK MODEL
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
                                mCursor = db.rawQuery(sql, new String[] {week_title, Long.toString(subject_id)});
                                long week_id;
                                if(!mCursor.moveToFirst()) {
                                    ContentValues weekValues = new ContentValues();
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_TITLE, week_title);
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_SUBJECT_KEY, subject_id);
                                    week_id = db.insert(MMUContract.WeekEntry.TABLE_NAME, null, weekValues);
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
                                        mCursor = db.query(MMUContract.AnnouncementEntry.TABLE_NAME,
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
                                            long _id = db.insert(MMUContract.AnnouncementEntry.TABLE_NAME, null, announcementValues);
                                        }
                                    }
                                }
                            }
                        }
                        annoucement_list_array.add(announcement_list);
                    }

                    Toast.makeText(MainActivity.this, "POST DATA DONE", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    mmls_load_status.setText("DATA FETCH COMPLETE");



                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null && networkResponse.data != null){
                    switch(networkResponse.statusCode){
                        case 400:
                            json = new String(networkResponse.data);
                            json = Utility.trimMessage(json, "message");
                            if(json != null)
                                Toast.makeText(MainActivity.this, json, Toast.LENGTH_SHORT).show();
                            mmls_load_status.setText(json);
                            break;
                        default:
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                            mmls_load_status.setText("NO INTERNET CONNECTION");
                    }
                    //Additional cases
                }
                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                    mmls_load_status.setText("NO INTERNET CONNECTION");
                }
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                params.put("_token", "P8QvN9q9bpNMSsCLkSEmaKPcEBrPy7UzllZmNbqS");
                params.put("file_path", "CYBER/TPT1201/notes");
                params.put("file_name", "Lec8.pdf");
                params.put("content_type", "5");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String, String>();
                headers.put("Content-Type","multipart/form-data");
                headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                headers.put("Connection", "keep-alive");
                headers.put("Content-Length", "693");
                headers.put("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary6IMihbtBLkOsS4fR");
                headers.put("Cookie", "laravel_session=eyJpdiI6InRsTWVSVVVTMjV4RENTdHdhSlJxNGc9PSIsInZhbHVlIjoiU2JMUGt5VTh6ZTNWVTN3V3lMb0hpZTdpa3JyRGhKcWhZK2RcL0xTMVNqT2dUeUx4NU1OSmdqMWxTRWVma1VSd2syeU5iQ1FySmtrb1J0c3JmWFh0MTF3PT0iLCJtYWMiOiI5YmEzNGZhY2VmZTM4OGYwNTkzNDgwMjJiN2ViZGQ3YzllOTUwNjAyNjZmODYxNWJkZTIzODU2NjU2NDllZWFmIn0%3D");
                headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
                return headers;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    };




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
        if (id == R.id.action_logout) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
            finish();
            MMUDbHelper mOpenHelper = new MMUDbHelper(MainActivity.this);
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            mOpenHelper.onLogout(db);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public ArrayList<List<String>> getAnnouncementJSON() {
        MMUDbHelper mOpenHelper = new MMUDbHelper(this);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://mmu-api.herokuapp.com/mmls_api";
//        String url = "https://mmu-api.herokuapp.com/login_test.json";
        Toast.makeText(MainActivity.this, "QUERYING DATA", Toast.LENGTH_SHORT).show();
        mmls_load_status.setText("ATTEMPTING TO QUERY SERVER...");
        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    Toast.makeText(MainActivity.this, "TRYING TO PARSE", Toast.LENGTH_SHORT).show();
                    JSONArray jarray = new JSONArray(response);
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(jarray.length());
                    for(int i = 0; i < jarray.length(); i++)
                    {
                        JSONObject jobj = jarray.getJSONObject(i);
                        String subject_name = jobj.getString("name");

                        mCursor = db.query(MMUContract.SubjectEntry.TABLE_NAME,
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
                            subject_id = db.insert(MMUContract.SubjectEntry.TABLE_NAME, null, subjectValues);
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
                                mCursor = db.rawQuery(sql, new String[] {week_title, Long.toString(subject_id)});
                                long week_id;
                                if(!mCursor.moveToFirst()) {
                                    ContentValues weekValues = new ContentValues();
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_TITLE, week_title);
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_SUBJECT_KEY, subject_id);
                                    week_id = db.insert(MMUContract.WeekEntry.TABLE_NAME, null, weekValues);
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
                                        mCursor = db.query(MMUContract.AnnouncementEntry.TABLE_NAME,
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
                                            long _id = db.insert(MMUContract.AnnouncementEntry.TABLE_NAME, null, announcementValues);
                                        }
                                    }
                                }
                            }
                        }
                        if(!jobj.isNull("subject_files")) {
                            JSONArray filesArray = jobj.getJSONArray("subject_files");

                        }

                    }

                    Toast.makeText(MainActivity.this, "POST DATA DONE", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    mmls_load_status.setText("DATA FETCH COMPLETE");



                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null && networkResponse.data != null){
                    switch(networkResponse.statusCode){
                        case 400:
                            json = new String(networkResponse.data);
                            json = Utility.trimMessage(json, "message");
                            if(json != null)
                                Toast.makeText(MainActivity.this, json, Toast.LENGTH_SHORT).show();
                                mmls_load_status.setText(json);
                            break;
                        default:
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                            mmls_load_status.setText("NO INTERNET CONNECTION");
                    }
                    //Additional cases
                }
                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                    mmls_load_status.setText("NO INTERNET CONNECTION");
                }
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
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
        return annoucement_list_array;
    };
    public void getBulletinData() {
        MMUDbHelper mOpenHelper = new MMUDbHelper(this);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://mmu-api.herokuapp.com/bulletin_api";
//        String url = "https://mmu-api.herokuapp.com/login_test.json";
        Toast.makeText(MainActivity.this, "QUERYING DATA", Toast.LENGTH_SHORT).show();
        mmls_load_status.setText("ATTEMPTING TO QUERY SERVER...");
        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    Toast.makeText(MainActivity.this, "TRYING TO FETCH BULLETIN", Toast.LENGTH_SHORT);
                    JSONArray jarray = new JSONArray(response);
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(jarray.length());
                    for(int i = 0; i < jarray.length(); i++)
                    {
                        JSONObject jobj = jarray.getJSONObject(i);
                        String subject_name = jobj.getString("name");

                        mCursor = db.query(MMUContract.SubjectEntry.TABLE_NAME,
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
                            subject_id = db.insert(MMUContract.SubjectEntry.TABLE_NAME, null, subjectValues);
                        }
                        else {
                            subject_id = mCursor.getLong(mCursor.getColumnIndex(MMUContract.SubjectEntry._ID));
                        }
                        //TODO REMOVE USELESS WEEK MODEL
                        List<String> announcement_list = new ArrayList<String>();
                        JSONArray weeks = jobj.getJSONArray("weeks");
                        if(weeks != null && subject_id != -1) {
                            for(int j = 0; j < weeks.length(); j++) {
                                JSONObject week_obj = weeks.getJSONObject(j);
                                String week_title = week_obj.getString("title");
                                mCursor = db.query(MMUContract.WeekEntry.TABLE_NAME,
                                        new String[] {MMUContract.WeekEntry.COLUMN_TITLE, MMUContract.WeekEntry._ID},
                                        MMUContract.WeekEntry.COLUMN_TITLE + " = ? ",
                                        new String[] {subject_name},
                                        null,
                                        null,
                                        null
                                );
                                long week_id;
                                if(!mCursor.moveToFirst()) {
                                    ContentValues weekValues = new ContentValues();
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_TITLE, week_title);
                                    weekValues.put(MMUContract.WeekEntry.COLUMN_SUBJECT_KEY, subject_id);
                                    week_id = db.insert(MMUContract.WeekEntry.TABLE_NAME, null, weekValues);
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
                    mCursor.close();

                    Toast.makeText(MainActivity.this, "POST DATA DONE", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    mmls_load_status.setText("DATA FETCH COMPLETE");



                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null && networkResponse.data != null){
                    switch(networkResponse.statusCode){
                        case 400:
                            json = new String(networkResponse.data);
                            json = Utility.trimMessage(json, "message");
                            if(json != null)
                                Toast.makeText(MainActivity.this, json, Toast.LENGTH_SHORT).show();
                            mmls_load_status.setText(json);
                            break;
                        default:
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                            mmls_load_status.setText("NO INTERNET CONNECTION");
                    }
                    //Additional cases
                }
                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                    mmls_load_status.setText("NO INTERNET CONNECTION");
                }
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                params.put("student_id", prefs.getString("student_id", ""));
                params.put("password", prefs.getString("icems_password", ""));
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
