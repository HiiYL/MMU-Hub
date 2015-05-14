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
        View view =  LayoutInflater.from(context).inflate(R.layout.listitem_announcement_mmls,parent,false);
        ChildViewHolder childViewHolder = new ChildViewHolder(view);
        view.setTag(childViewHolder);
        return view;
    }
    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
//        ImageView ivHasSeen = (ImageView) view.findViewById(R.id.imageview_has_seen);
//
//
//        TextView tvTitle = (TextView) view.findViewById(R.id.listitem_mmls_title);
//        TextView tvPostedAt = (TextView) view.findViewById(R.id.list_item_mmls_posted_at);
//        TextView tvAuthor = (TextView) view.findViewById(R.id.list_item_mmls_author);
//        TextView tvSnippet = (TextView) view.findViewById(R.id.listitem_mmls_snippet);

        ChildViewHolder childViewHolder = (ChildViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_TITLE));
        String posted_at = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_POSTED_DATE));
        String author = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_AUTHOR));
        String contents = cursor.getString(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_CONTENTS));

        childViewHolder.tvTitle.setText(title.trim());
        childViewHolder.tvPostedAt.setText(Utility.humanizeDate(posted_at));
        childViewHolder.tvAuthor.setText(author);

        childViewHolder.tvSnippet.setText(Html.fromHtml(contents.replace("\n", "<br>")));

        if(cursor.getInt(cursor.getColumnIndex(MMUContract.AnnouncementEntry.COLUMN_HAS_SEEN)) == 0) {
            childViewHolder.ivHasSeen.setVisibility(View.VISIBLE);
        }else {
            childViewHolder.ivHasSeen.setVisibility(View.INVISIBLE);
        }
    }
    static class ChildViewHolder {
        public final ImageView ivHasSeen;
        public final TextView tvTitle;
        public final TextView tvPostedAt;
        public final TextView tvAuthor;
        public final TextView tvSnippet;

        public ChildViewHolder(View view) {
            ivHasSeen = (ImageView) view.findViewById(R.id.imageview_has_seen);


            tvTitle = (TextView) view.findViewById(R.id.listitem_mmls_title);
            tvPostedAt = (TextView) view.findViewById(R.id.list_item_mmls_posted_at);
            tvAuthor = (TextView) view.findViewById(R.id.list_item_mmls_author);
            tvSnippet = (TextView) view.findViewById(R.id.listitem_mmls_snippet);
        }
    }
}
