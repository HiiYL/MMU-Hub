package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.github.hiiyl.mmuhub.data.MMUContract;

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

        tvTitle.setText(cursor.getString(cursor.getColumnIndex(MMUContract.FilesEntry.COLUMN_NAME)));

    }
}
