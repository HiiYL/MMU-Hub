package com.github.hiiyl.mmuhub.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Hii on 4/19/15.
 */
public class MMUContract {
    public static final String CONTENT_AUTHORITY = "com.github.hiiyl.mmuhub";

    public static final String PATH_SUBJECT = "subject";
    public static final String PATH_WEEK = "week";
    public static final String PATH_ANNOUNCEMENT = "announcement";
    public static final String PATH_BULLETIN = "bulletin";
    public static final String PATH_FILE = "file";
    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static class SubjectEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBJECT).build();
        public static final String TABLE_NAME = "subject";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_URL = "url";

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBJECT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBJECT;

        public static Uri buildSubjectUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
    public static class WeekEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEEK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEEK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEEK;

        public static final String TABLE_NAME = "week";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SUBJECT_KEY = "subject_id";

        public static Uri buildWeekUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildWeekWithSubjectUri(String subject) {
            return CONTENT_URI.buildUpon().appendPath(subject).build();
        }
    }
    public static class AnnouncementEntry implements  BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ANNOUNCEMENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ANNOUNCEMENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ANNOUNCEMENT;
        public static final String TABLE_NAME = "announcement";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENTS = "contents";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_POSTED_DATE = "posted_date";
        public static final String COLUMN_WEEK_KEY = "week_id";
        public static final String COLUMN_SUBJECT_KEY = "subject_id";
        public static final String COLUMN_HAS_SEEN = "announcement_seen";

        public static Uri buildAnnouncementUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildAnnouncementWithSubjectUri(String subject) {
            return CONTENT_URI.buildUpon().appendPath(subject).build();
        }
        public static Uri buildAnnouncementWithWeekUri(String week) {
            return CONTENT_URI.buildUpon().appendPath(week).build();
        }
    }
    public static class BulletinEntry implements  BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BULLETIN).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BULLETIN;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BULLETIN;
        public static final String TABLE_NAME = "bulletin";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENTS = "contents";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_POSTED_DATE = "posted_date";
        public static final String COLUMN_HAS_SEEN = "bulletin_has_seen";
    }
    public static class FilesEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FILE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FILE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FILE;
        public static final String TABLE_NAME = "subject_files";
        public static final String COLUMN_NAME = "file_name";
        public static final String COLUMN_TOKEN = "file_token";
        public static final String COLUMN_CONTENT_TYPE = "content_type";
        public static final String COLUMN_CONTENT_ID = "content_id";
        public static final String COLUMN_REMOTE_FILE_PATH = "remote_file_path";
        public static final String COLUMN_LOCAL_FILE_PATH = "local_file_path";
        public static final String COLUMN_SUBJECT_KEY = "subject_id";
        public static final String COLUMN_ANNOUNCEMENT_KEY = "announcement_id";
        public static final String COLUMN_DOWNLOADED = "downloaded";
        public static Uri buildFileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildFileWithSubject(String subject) {
            return CONTENT_URI.buildUpon().appendPath(subject).build();
        }
    }
}
