package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.github.hiiyl.mmuhub.data.MMUContract;

import java.io.File;

/**
 * Created by Hii on 4/21/15.
 */
public class MMLSDownloadAdapter extends CursorAdapter {
    public MMLSDownloadAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listitem_download_mmls,parent,false);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvTitle = (TextView) view.findViewById(R.id.file_download_name_textview);
        TextView tvStatus = (TextView) view.findViewById(R.id.file_download_downloaded_status);
        TextView tvHint = (TextView) view.findViewById(R.id.file_download_interaction_hint);
        String file_name = cursor.getString(cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME));
        String subject_id = cursor.getString(cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_SUBJECT_KEY));
        String sql = "SELECT " + MMUContract.SubjectEntry.TABLE_NAME + "." + MMUContract.SubjectEntry.COLUMN_NAME + " FROM " + MMUContract.SubjectEntry.TABLE_NAME + "," +
                MMUContract.FilesEntry.TABLE_NAME + " WHERE " + MMUContract.SubjectEntry.TABLE_NAME + "." + MMUContract.SubjectEntry._ID + "=" + subject_id;

        Cursor temp_cursor = MySingleton.getInstance(context).getDatabase().rawQuery(sql, null);
        temp_cursor.moveToFirst();
        String subject_name = temp_cursor.getString(temp_cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));
        String file_path = Environment.getExternalStorageDirectory().getPath() + "/" + Utility.DOWNLOAD_FOLDER + "/" + subject_name + "/" + file_name;
        Log.d("SQL IS ", sql);
        File file = new File(file_path);
        tvTitle.setText(cursor.getString(cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME)));
        if(file.exists()) {
            tvStatus.setText("Downloaded");
            tvHint.setText("Tap to View");
        } else {
            tvStatus.setText("Not Downloaded");
            tvHint.setText("Tap to Download");
        }
        temp_cursor.close();


    }
}
