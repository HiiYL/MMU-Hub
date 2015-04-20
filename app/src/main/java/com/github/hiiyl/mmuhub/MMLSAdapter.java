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
 * Created by Hii on 4/19/15.
 */
public class MMLSAdapter extends CursorAdapter {
    public MMLSAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listitem_announcement_mmls,parent,false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvTitle = (TextView) view.findViewById(R.id.listitem_mmls_title);
        TextView tvPostedAt = (TextView) view.findViewById(R.id.list_item_mmls_posted_at);
        TextView tvAuthor = (TextView) view.findViewById(R.id.list_item_mmls_author);

        String title = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_TITLE));
        String posted_at = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE));
        String author = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_AUTHOR));

        tvTitle.setText(title);
        tvPostedAt.setText(posted_at);
        tvAuthor.setText(author);
    }
}
