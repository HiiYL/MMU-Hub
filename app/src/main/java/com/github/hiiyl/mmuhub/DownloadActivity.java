package com.github.hiiyl.mmuhub;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadActivity extends ActionBarActivity {
    private static int mSubjectID;
    private static ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            mSubjectID = extras.getInt("SUBJECT_ID");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_download, menu);
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
        private ListView download_list;
        private MMUDbHelper mOpenHelper;

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);

            mOpenHelper = new MMUDbHelper(getActivity());
            MainActivity.database = mOpenHelper.getReadableDatabase();
            View rootView = inflater.inflate(R.layout.fragment_download, container, false);
            download_list = (ListView) rootView.findViewById(R.id.mmls_download_listview);

            String subject_id = Integer.toString(DownloadActivity.mSubjectID);
            Log.d("Subject ID is ", subject_id);
            final Cursor cursor = MainActivity.database.query(MMUContract.FilesEntry.TABLE_NAME, null, MMUContract.FilesEntry.COLUMN_SUBJECT_KEY + " = ? ",
                    new String[] {subject_id}, null, null,null );
            final MMLSDownloadAdapter adapter = new MMLSDownloadAdapter(getActivity(), cursor);
            download_list.setAdapter(adapter);

            download_list.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Cursor new_cursor = adapter.getCursor();
                    FragmentActivity activity = (FragmentActivity)view.getContext();
                    final DownloadTask downloadTask = new DownloadTask(getActivity(),activity);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    downloadTask.file_name = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME));
                    downloadTask.content_type = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE));
                    downloadTask.content_id = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_ID));
                    downloadTask.token = prefs.getString("token","");
                    downloadTask.cookie = "laravel_session=" + prefs.getString("cookie", "");

                    Log.d("APP", "Now downloading " + downloadTask.file_name);

                    new_cursor.close();

                    downloadTask.execute("https://mmls.mmu.edu.my/form-download-content");

                }
            });
            return rootView;
        }

    }
    private static class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String cookie;
        private String content_id;
        private String file_name;
        private String content_type;
        private String token;
        private Activity mActivity;
        private HttpURLConnection connection = null;

        public DownloadTask(Context context, Activity activity) {
            this.context = context;
            mActivity = activity;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;

            try {
                URL url = new URL(sUrl[0]);
                Log.d("Download Cookie", cookie);
                Log.d("Download FileName", file_name);
                Log.d("Download ContentType", content_type);
                Log.d("Download ContentID", content_id);
                Log.d("Download Token", token);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Accept-Encoding","gzip, deflate");
                connection.setRequestProperty("Cache-Control","max-age=0");
                connection.setRequestProperty("Connection","keep-alive");
                connection.setRequestProperty("Content-Length","693");
                connection.setRequestProperty("Content-Type","multipart/form-data; boundary=----WebKitFormBoundary6IMihbtBLkOsS4fR");
                connection.setRequestProperty("Cookie", cookie);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
                connection.setRequestMethod("POST");
                connection.setInstanceFollowRedirects(false);
                connection.connect();
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
                    if(connection.getResponseCode() == 302)

                    Log.d("DownloadTask","CONNECTION ERROR" + "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                Log.d("CONNECTION", "RESPONSE CODE RECEIVED");
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream("/sdcard/" + file_name);

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

            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mProgressDialog.setMessage("Please wait... Authenticating");
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            Log.d("DOWNLOADING", Integer.toString(progress[0]));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            mWakeLock.release();
            mProgressDialog.dismiss();
            Log.d("FD","DOWNLOAD COMPLETE");
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
        }
    };
}
