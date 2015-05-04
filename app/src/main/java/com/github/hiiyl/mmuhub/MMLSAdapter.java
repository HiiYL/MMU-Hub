package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;

/**
 * Created by Hii on 4/19/15.
 */
public class MMLSAdapter extends CursorTreeAdapter {
    Context mContext;
    Cursor mCursor;
    MMUDbHelper mHelper;
    public MMLSAdapter(Cursor cursor, Context context){
        super(cursor, context);
        mContext = context;
    }



    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        String week_id = groupCursor.getString(groupCursor.getColumnIndex(MMUContract.WeekEntry._ID));
        mCursor = MySingleton.getInstance(mContext).getDatabase().query(MMUContract.AnnouncementEntry.TABLE_NAME, null,
                "week_id = ?", new String[] {week_id}, null, null, null);
        mCursor.moveToFirst();
        return mCursor;
    }




    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listgroup_announcement_mmls,parent,false);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        TextView tvWeek = (TextView) view.findViewById(R.id.listgroup_week_textview);
        tvWeek.setText(cursor.getString(cursor.getColumnIndex(MMUContract.WeekEntry.COLUMN_TITLE)));

    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listitem_announcement_mmls,parent,false);
    }
    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ImageView ivHasSeen = (ImageView) view.findViewById(R.id.imageview_has_seen);


        TextView tvTitle = (TextView) view.findViewById(R.id.listitem_mmls_title);
        TextView tvPostedAt = (TextView) view.findViewById(R.id.list_item_mmls_posted_at);
        TextView tvAuthor = (TextView) view.findViewById(R.id.list_item_mmls_author);
        TextView tvSnippet = (TextView) view.findViewById(R.id.listitem_mmls_snippet);

        String title = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_TITLE));
        String posted_at = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE));
        String author = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_AUTHOR));
        String contents = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_CONTENTS));

        tvTitle.setText(title.trim());
        tvPostedAt.setText(Utility.humanizeDate(posted_at));
        tvAuthor.setText(author);

        tvSnippet.setText(Html.fromHtml(contents.replace("\n", "<br>")));

        if(cursor.getInt(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_HAS_SEEN)) == 0) {
            ivHasSeen.setVisibility(View.VISIBLE);
        }else {
            ivHasSeen.setVisibility(View.INVISIBLE);
        }

    }
}
