package com.github.hiiyl.mmuhub;

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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gc.materialdesign.widgets.SnackBar;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;
import com.github.hiiyl.mmuhub.helper.FileOpen;
import com.github.hiiyl.mmuhub.helper.RefreshTokenEvent;
import com.github.hiiyl.mmuhub.helper.ViewEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.greenrobot.event.EventBus;


public class AnnouncementDetailActivity extends ActionBarActivity {
    private static String ANNOUNCEMENT_ID;
    private static String TAG = AnnouncementDetailActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Bundle extras = getIntent().getExtras();
        ANNOUNCEMENT_ID = extras.getString("ANNOUNCEMENT_ID");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_announcement_detail, menu);
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
    public static class PlaceholderFragment extends Fragment {
        private String mSubjectName;
        private String mFileName;
        private String mFileDirectory;
        private static ProgressBar mProgressBar;
        private static TextView mInteractionPromptText;


        public PlaceholderFragment() {
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
        public void onEventMainThread(RefreshTokenEvent event) {
            SnackBar new_snackbar = new SnackBar(getActivity(), event.status);
            new_snackbar.show();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            String announcement_id = AnnouncementDetailActivity.ANNOUNCEMENT_ID;
            String slide_str = "1";
            Log.d("MYAPP", "RECEIVED " + announcement_id);
            final View rootView = inflater.inflate(R.layout.fragment_announcement_detail, container, false);
            TextView title_textview = (TextView) rootView.findViewById(R.id.announcement_detail_title);
            TextView contents_textview = (TextView) rootView.findViewById(R.id.announcement_detail_contents);
            TextView author_textview = (TextView) rootView.findViewById(R.id.announcement_detail_author);
            TextView posted_date_textview = (TextView) rootView.findViewById(R.id.announcement_detail_posted_date);
            MMUDbHelper mOpenHelper = new MMUDbHelper(getActivity());
            ContentValues has_seen = new ContentValues();
            has_seen.put(MMUContract.AnnouncementEntry.COLUMN_HAS_SEEN, true);
            MySingleton.getInstance(getActivity()).getDatabase().update(MMUContract.AnnouncementEntry.TABLE_NAME, has_seen, MMUContract.AnnouncementEntry._ID + "=?", new String[]{announcement_id});
            EventBus.getDefault().post(new ViewEvent(Utility.VIEW_ANNOUNCEMENT_EVENT));
            Cursor cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.AnnouncementEntry.TABLE_NAME, null, MMUContract.AnnouncementEntry._ID + "=?",new String[] {announcement_id},null,null,null);

            if(cursor.moveToFirst()) {
                Log.d(TAG, "Successful Query");
            }
            else {
                Log.d(TAG, "Something went wrong");
            }

            String title = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_TITLE));
            String contents = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_CONTENTS));
            String posted_at = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE));
            String author = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry._ID));
            title_textview.setText(title);
            contents_textview.setText(contents);
            author_textview.setText(author);
            posted_date_textview.setText(posted_at);

            String attachment_file_query =
                    "SELECT * FROM " + MMUContract.FilesEntry.TABLE_NAME +
                            " WHERE " + MMUContract.FilesEntry.COLUMN_ANNOUNCEMENT_KEY + " = " +
                            cursor.getLong(cursor.getColumnIndex(MMUContract.AnnouncementEntry._ID));
            cursor = MySingleton.getInstance(getActivity()).getDatabase().rawQuery(attachment_file_query, null);

            if(cursor.moveToFirst()) {
                LinearLayout layout = (LinearLayout)rootView.findViewById(R.id.announcement_detail_download_layout);
                TextView attachment_view = (TextView) rootView.findViewById(R.id.announcement_detail_attachment_name);
                mInteractionPromptText = (TextView) rootView.findViewById(R.id.announcement_detail_attachment_interaction_prompt);
                mProgressBar = (ProgressBar) rootView.findViewById(R.id.announcement_detail_attachment_progressbar);


                layout.setVisibility(View.VISIBLE);
                attachment_view.setText(cursor.getString(cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME)));
                final Cursor finalCursor = cursor;
                final String file_name = finalCursor.getString(finalCursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME));
                Log.d("SUBJECT ID IS ", cursor.getString(cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY)));

                String subject_name_query = "SELECT " + MMUContract.SubjectEntry.COLUMN_NAME
                        + " FROM " + MMUContract.SubjectEntry.TABLE_NAME + " WHERE " + MMUContract.SubjectEntry._ID + " = " +
                cursor.getString(cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY));

                Log.d("ID OF SUBJECT IS ", Long.toString(finalCursor.getLong(finalCursor.getColumnIndex(MMUContract.FilesEntry._ID))));
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
                            mProgressBar.setVisibility(View.VISIBLE);
                            File temp = new File(file_directory);
                            temp.mkdirs();
                            final DownloadTask downloadTask = new DownloadTask(getActivity());
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            downloadTask.file_name = file_name;
                            downloadTask.content_type = finalCursor.getString(finalCursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE));
                            downloadTask.content_id = finalCursor.getString(finalCursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_ID));
                            downloadTask.remote_file_path = finalCursor.getString(finalCursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH));
                            downloadTask.token = prefs.getString("token", "");
                            downloadTask.cookie = "laravel_session=" + prefs.getString("cookie", "");
                            downloadTask.local_file_path = file_path;

                            Log.d("APP", "Now downloading " + downloadTask.file_name);
                            downloadTask.execute("https://mmls.mmu.edu.my/form-download-content");
                        }

                    }
                });



            }
            return rootView;
        }
        private static class DownloadTask extends AsyncTask<String, Integer, String> {

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
    }

}
