package com.github.hiiyl.mmuhub;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;


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
        if(extras != null) {
            ANNOUNCEMENT_ID = extras.getString("ANNOUNCEMENT_ID");
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_announcement_detail, menu);
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


        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            String announcement_id = AnnouncementDetailActivity.ANNOUNCEMENT_ID;
            String slide_str = "1";
            Log.d("MYAPP", "RECEIVED " + announcement_id);
            View rootView = inflater.inflate(R.layout.fragment_announcement_detail, container, false);
            TextView title_textview = (TextView) rootView.findViewById(R.id.announcement_detail_title);
            TextView contents_textview = (TextView) rootView.findViewById(R.id.announcement_detail_contents);
            TextView author_textview = (TextView) rootView.findViewById(R.id.announcement_detail_author);
            TextView posted_date_textview = (TextView) rootView.findViewById(R.id.announcement_detail_posted_date);
            MMUDbHelper mOpenHelper = new MMUDbHelper(getActivity());
            MainActivity.database = mOpenHelper.getReadableDatabase();
            Cursor cursor = MainActivity.database.query(MMUContract.AnnouncementEntry.TABLE_NAME, null, MMUContract.AnnouncementEntry._ID + "=?",new String[] {announcement_id},null,null,null);
            if(cursor.moveToFirst()) {
                Log.d(TAG, "Successful Query");
            }
            else {
                Log.d(TAG, "Something went wrong");
            }

            String title = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_TITLE));
            String posted_at = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE));
            String author = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry._ID));
            title_textview.setText(cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_TITLE)));
            contents_textview.setText(cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_CONTENTS)));
            author_textview.setText(cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_AUTHOR)));
            posted_date_textview.setText(cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE)));
            cursor.close();
            return rootView;
        }
    }
}
