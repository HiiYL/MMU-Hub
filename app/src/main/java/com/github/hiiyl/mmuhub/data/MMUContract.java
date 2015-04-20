package com.github.hiiyl.mmuhub.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Hii on 4/19/15.
 */
public class MMUContract {
    public static final String CONTENT_AUTHORITY = "com.github.hiiyl.mmuhub";

    public static final String PATH_SUBJECT = "subject";
    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static class SubjectEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBJECT).build();
        public static final String TABLE_NAME = "subject";

        public static final String COLUMN_NAME = "name";

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBJECT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBJECT;
    }
    public static class WeekEntry implements BaseColumns {
        public static final String TABLE_NAME = "week";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SUBJECT_KEY = "subject_id";
    }
    public static class AnnouncementEntry implements  BaseColumns {
        public static final String TABLE_NAME = "announcement";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENTS = "contents";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_POSTED_DATE = "posted_date";
        public static final String COLUMN_WEEK_KEY = "week_id";
        public static final String COLUMN_SUBJECT_KEY = "subject_id";
    }
    public static class BulletinEntry implements  BaseColumns {
        public static final String TABLE_NAME = "announcement";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENTS = "contents";
        public static final String COLUMN_DETAILS = "author";
        public static final String COLUMN_POSTED_DATE = "posted_date";
    }
}
