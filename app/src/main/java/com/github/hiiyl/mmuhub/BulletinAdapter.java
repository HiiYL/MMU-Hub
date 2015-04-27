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
 * Created by Hii on 4/26/15.
 */
public class BulletinAdapter extends CursorAdapter {
    public BulletinAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.bulletin_list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView bulletin_title = (TextView) view.findViewById(R.id.bulletin_title);
        TextView bulletin_posted = (TextView) view.findViewById(R.id.bulletin_posted_date);
        TextView bulletin_author = (TextView) view.findViewById(R.id.bulletin_author);

        bulletin_title.setText(cursor.getString(cursor.getColumnIndex(MMUContract.BulletinEntry.COLUMN_TITLE)));
        bulletin_author.setText(cursor.getString(cursor.getColumnIndex(MMUContract.BulletinEntry.COLUMN_AUTHOR)));
        bulletin_posted.setText(cursor.getString(cursor.getColumnIndex(MMUContract.BulletinEntry.COLUMN_POSTED_DATE)));
    }
}
