package com.github.hiiyl.mmuhub;

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
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;
import com.github.hiiyl.mmuhub.helper.DownloadCompleteEvent;
import com.github.hiiyl.mmuhub.helper.DownloadListRecycleEvent;
import com.github.hiiyl.mmuhub.helper.FileOpen;
import com.github.hiiyl.mmuhub.helper.RefreshTokenEvent;

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


public class DownloadActivity extends AppCompatActivity {
    private static int mSubjectID;


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
//        getMenuInflater().inflate(R.menu.menu_download, menu);
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
        private MMLSDownloadAdapter adapter;
        private ButtonFlat mDownloadAllButton;
        private String mSubjectName;
        private String mSubjectID;

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

        public void onEventMainThread(DownloadCompleteEvent event) {
            SnackBar new_snackbar = new SnackBar(getActivity(), event.message);
            new_snackbar.show();
            adapter.notifyDataSetChanged();
        }
        public void onEventMainThread(RefreshTokenEvent event) {
            SnackBar new_snackbar = new SnackBar(getActivity(), event.status);
            new_snackbar.show();
        }
        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {



            mOpenHelper = new MMUDbHelper(getActivity());
            View rootView = inflater.inflate(R.layout.fragment_download, container, false);
            download_list = (ListView) rootView.findViewById(R.id.mmls_download_listview);

            final String subject_id = Integer.toString(DownloadActivity.mSubjectID);
            Log.d("Subject ID is ", subject_id);
            mSubjectID = subject_id;
            mSubjectName = Utility.getSubjectName(getActivity(), mSubjectID);
            final Cursor cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.FilesEntry.TABLE_NAME, null, MMUContract.FilesEntry.COLUMN_SUBJECT_KEY + " = ? AND " + MMUContract.FilesEntry.COLUMN_ANNOUNCEMENT_KEY + " IS NULL",
                    new String[] {subject_id}, null, null,null );
            adapter = new MMLSDownloadAdapter(getActivity(), cursor);
            download_list.setAdapter(adapter);

            mDownloadAllButton = (ButtonFlat)rootView.findViewById(R.id.download_all_button);



            mDownloadAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SnackBar download_all_confirm = new SnackBar(getActivity(), "Download All Items?", "Yes",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    download_list.setRecyclerListener(new AbsListView.RecyclerListener() {
                                        @Override
                                        public void onMovedToScrapHeap(View view) {
                                            EventBus.getDefault().post(new DownloadListRecycleEvent(download_list));
                                        }
                                    });
                                    Cursor new_cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.FilesEntry.TABLE_NAME, null, MMUContract.FilesEntry.COLUMN_SUBJECT_KEY + " = ? AND " + MMUContract.FilesEntry.COLUMN_ANNOUNCEMENT_KEY + " IS NULL",
                                            new String[]{subject_id}, null, null, null);
                                    new_cursor.moveToFirst();
                                    int count = 0;
                                    while (!new_cursor.isAfterLast()) {
                                        String file_name = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME));
                                        String file_path = Environment.getExternalStorageDirectory().getPath() + "/" + Utility.DOWNLOAD_FOLDER + "/" + mSubjectName + "/" + file_name;
                                        String file_directory = Environment.getExternalStorageDirectory().getPath() + "/" + Utility.DOWNLOAD_FOLDER + "/" + mSubjectName + "/";
                                        File file = new File(file_path);

                                        if(!file.exists()) {
                                            File temp = new File(file_directory);
                                            temp.mkdirs();
//                            FragmentActivity activity = (FragmentActivity) view.getContext();
                                            final DownloadTask downloadTask = new DownloadTask(getActivity());
                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                            downloadTask.file_name = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME));
                                            downloadTask.content_type = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE));
                                            downloadTask.content_id = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_ID));
                                            downloadTask.remote_file_path = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH));
                                            downloadTask.token = prefs.getString("token", "");
                                            downloadTask.cookie = "laravel_session=" + prefs.getString("cookie", "");
                                            downloadTask.local_file_path = file_path;
                                            downloadTask.mPosition = count;
                                            downloadTask.view = getViewByPosition(count,download_list);
                                            downloadTask.isDownloadAll = true;
                                            final int firstListItemPosition = download_list.getFirstVisiblePosition();
                                            final int lastListItemPosition = firstListItemPosition + download_list.getChildCount() - 1;
                                            View view;
                                            if (count < firstListItemPosition || count > lastListItemPosition ) {
                                                downloadTask.isVisible = false;
                                            } else {
                                                final int childIndex = count - firstListItemPosition;
                                                downloadTask.view = download_list.getChildAt(childIndex);
                                                downloadTask.isVisible = true;
                                            }


                                            Log.d("APP", "Now downloading " + downloadTask.file_name);

                                            downloadTask.execute("https://mmls.mmu.edu.my/form-download-content");

                                        }
                                        new_cursor.moveToNext();
                                        count++;
                                    }
                                    new_cursor.close();

                                }
                            });
                    download_all_confirm.show();
                }
            });


            download_list.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    Cursor new_cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.FilesEntry.TABLE_NAME, null, MMUContract.FilesEntry.COLUMN_SUBJECT_KEY + " = ? AND " + MMUContract.FilesEntry.COLUMN_ANNOUNCEMENT_KEY + " IS NULL",
                            new String[]{subject_id}, null, null, null);
                    new_cursor.moveToPosition(position);

                    String file_name = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME));
                    String file_path = Environment.getExternalStorageDirectory().getPath() + "/" + Utility.DOWNLOAD_FOLDER + "/" + mSubjectName + "/" + file_name;
                    String file_directory = Environment.getExternalStorageDirectory().getPath() + "/" + Utility.DOWNLOAD_FOLDER + "/" + mSubjectName + "/";
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

                        File temp = new File(file_directory);
                        temp.mkdirs();
                        view.setHasTransientState(true);
                        FragmentActivity activity = (FragmentActivity) view.getContext();
                        final DownloadTask downloadTask = new DownloadTask(getActivity());
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        downloadTask.file_name = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME));
                        downloadTask.content_type = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_TYPE));
                        downloadTask.content_id = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_CONTENT_ID));
                        downloadTask.remote_file_path = new_cursor.getString(new_cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_REMOTE_FILE_PATH));
                        downloadTask.token = prefs.getString("token", "");
                        downloadTask.cookie = "laravel_session=" + prefs.getString("cookie", "");
                        downloadTask.local_file_path = file_path;
                        downloadTask.view = view;


                        Log.d("APP", "Now downloading " + downloadTask.file_name);

                        new_cursor.close();

                        downloadTask.execute("https://mmls.mmu.edu.my/form-download-content");
                    }

                }
            });
            return rootView;
        }
    }
    public static View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        View view;

        if (position < firstListItemPosition || position > lastListItemPosition ) {
            view = listView.getAdapter().getView(position, listView.getChildAt(position), listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            view = listView.getChildAt(childIndex);
        }
        return view;
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
        private ProgressBar mProgressBar;
        private HttpsURLConnection connection = null;
        private View view;
        private int mPosition;
        boolean isDownloadAll = false;
        boolean isVisible = true;

        public DownloadTask(Context context) {
            this.context = context;
            EventBus.getDefault().register(this);
        }
        public void onEvent(DownloadListRecycleEvent event) {
            ListView listView = event.download_list;
            final int firstListItemPosition = listView.getFirstVisiblePosition();
            final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
            View view;
            if (mPosition < firstListItemPosition || mPosition > lastListItemPosition ) {
                isVisible = false;
            } else {
                final int childIndex = mPosition - firstListItemPosition;
                view = listView.getChildAt(childIndex);
                mProgressBar = (ProgressBar) view.findViewById(R.id.listitem_download_mmls_progress_bar);
                isVisible = true;
            }
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
                connection.setRequestProperty("Content-Type","multipart/form-data; boundary=----WebKitFormBoundary6IMihbtBLkOsS4fR");
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
                Log.d("HTTP URL CINNECT","STREAM FLUSHED AND CLOSED");

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("CONNECT", " FAILED AND RESPONSE CODE IS " + connection.getResponseCode());
                    if(connection.getResponseCode() == 302) {
                        Log.d("WOW!", "REFRESHING TOKEN");
                        Utility.refreshToken(context);
                    }
                    Log.d("CONNECTION CODE", "RESPONSE CODE IS " + connection.getResponseCode());
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                Log.d("CONNECTION CODE", "RESPONSE CODE IS " + connection.getResponseCode());
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
            Log.d("HELLO THERE", Boolean.toString(isVisible));
            if(isVisible) {
                mProgressBar = (ProgressBar) view.findViewById(R.id.listitem_download_mmls_progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setIndeterminate(true);
                mProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                view.setHasTransientState(true);
            }

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
            if(isVisible) {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setIndeterminate(false);
                mProgressBar.setMax(100);
                mProgressBar.setProgress(progress[0]);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            String download_result;
            if (result != null) {
                Log.d("RESULT IS", result);
                if(isVisible) {
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    mProgressBar.setMax(100);
                    mProgressBar.setProgress(100);
                }
                download_result = "Download Failed";
            }
            else {
                download_result = "Download Successful";
                EventBus.getDefault().post(new DownloadCompleteEvent(download_result));
            }
        }
    };
}
