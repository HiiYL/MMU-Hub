package com.github.hiiyl.mmuhub;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.helper.ViewEvent;

import de.greenrobot.event.EventBus;


public class BulletinDetailActivity extends AppCompatActivity {
    private static String bulletin_id ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            bulletin_id = extras.getString("BULLETIN_ID");
            Log.d("RECEIVED", "NOT NULL ANNOUNCEMENT_ID = " + bulletin_id);
        } else {
            Log.d("RECEIVED", "NULL ANNOUNCEMENT_ID = " + bulletin_id);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_bulletin_detail, menu);
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
            View rootView = inflater.inflate(R.layout.fragment_bulletin_detail, container, false);
            TextView bulletin_title = (TextView) rootView.findViewById(R.id.bulletin_detail_title);
            TextView bulletin_author = (TextView) rootView.findViewById(R.id.bulletin_detail_author);
            TextView bulletin_contents = (TextView) rootView.findViewById(R.id.bulletin_detail_contents);
            TextView bulletin_posted_at = (TextView) rootView.findViewById(R.id.bulletin_posted_date);

            ContentValues has_seen = new ContentValues();
            has_seen.put(MMUContract.BulletinEntry.COLUMN_HAS_SEEN, true);
            MySingleton.getInstance(getActivity()).getDatabase().update(MMUContract.BulletinEntry.TABLE_NAME, has_seen, MMUContract.AnnouncementEntry._ID + "=?", new String[]{bulletin_id});
            EventBus.getDefault().post(new ViewEvent(Utility.VIEW_BULLETIN_EVENT));
            Cursor cursor = MySingleton.getInstance(getActivity()).getDatabase().query(MMUContract.BulletinEntry.TABLE_NAME, null, MMUContract.BulletinEntry._ID + "=?",new String[] {bulletin_id},null,null,null);
            cursor.moveToFirst();
            bulletin_title.setText(cursor.getString(cursor.getColumnIndex(MMUContract.BulletinEntry.COLUMN_TITLE)));
            bulletin_contents.setText(cursor.getString(cursor.getColumnIndex(MMUContract.BulletinEntry.COLUMN_CONTENTS)));
            bulletin_author.setText(cursor.getString(cursor.getColumnIndex(MMUContract.BulletinEntry.COLUMN_AUTHOR)));
//            bulletin_posted_at.setText(cursor.getString(cursor.getColumnIndex(MMUContract.BulletinEntry.COLUMN_POSTED_DATE)));

            return rootView;
        }
    }
}
