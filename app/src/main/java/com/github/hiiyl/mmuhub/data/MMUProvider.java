package com.github.hiiyl.mmuhub.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Hii on 4/19/15.
 */
public class MMUProvider extends ContentProvider {
//    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MMUDbHelper mmuDbHelper;

    static final int SUBJECT  = 100;
    static final int ANNOUNCEMENT = 200;
    static final int ANNOUNCEMENT_WITH_WEEK = 201;
    static final int ANNOUNCEMENT_WITH_SUBJECT = 202;
    static final int WEEK = 300;
    static final int WEEK_WITH_SUBJECT = 300;
    static final int FILE = 400;
    public static final int FILE_WITH_SUBJECT = 401;

    static final String AUTHORITY = "com.github.hiiyl.mmuhub";

    private static final SQLiteQueryBuilder sAnnouncementByWeekQueryBuilder;
    private static final SQLiteQueryBuilder sAnnouncementBySubjectQueryBuilder;
    private static final SQLiteQueryBuilder sWeekBySubjectQueryBuilder;
    private static final SQLiteQueryBuilder sFileBySubjectQueryBuilder;

    public static String getAuthority() {
        return AUTHORITY;
    }



    static {
        sAnnouncementByWeekQueryBuilder = new SQLiteQueryBuilder();
        sAnnouncementBySubjectQueryBuilder = new SQLiteQueryBuilder();
        sWeekBySubjectQueryBuilder = new SQLiteQueryBuilder();
        sFileBySubjectQueryBuilder = new SQLiteQueryBuilder();

        sAnnouncementByWeekQueryBuilder.setTables(
                MMUContract.AnnouncementEntry.TABLE_NAME + " INNER JOIN " +
                        MMUContract.WeekEntry.TABLE_NAME +
                        " ON " + MMUContract.AnnouncementEntry.TABLE_NAME +
                        "." + MMUContract.AnnouncementEntry.COLUMN_WEEK_KEY +
                        " = " + MMUContract.WeekEntry.TABLE_NAME +
                        "." + MMUContract.WeekEntry._ID);



        sAnnouncementBySubjectQueryBuilder.setTables(
                MMUContract.AnnouncementEntry.TABLE_NAME + " INNER JOIN " +
                        MMUContract.SubjectEntry.TABLE_NAME +
                        " ON " + MMUContract.AnnouncementEntry.TABLE_NAME +
                        "." + MMUContract.AnnouncementEntry.COLUMN_SUBJECT_KEY +
                        " = " + MMUContract.SubjectEntry.TABLE_NAME +
                        "." + MMUContract.SubjectEntry._ID);

        sWeekBySubjectQueryBuilder.setTables(
                MMUContract.WeekEntry.TABLE_NAME + " INNER JOIN " +
                        MMUContract.SubjectEntry.TABLE_NAME +
                        " ON " + MMUContract.WeekEntry.TABLE_NAME +
                        "." + MMUContract.WeekEntry.COLUMN_SUBJECT_KEY +
                        " = " + MMUContract.SubjectEntry.TABLE_NAME +
                        "." + MMUContract.SubjectEntry._ID);

        sFileBySubjectQueryBuilder.setTables(
                MMUContract.FilesEntry.TABLE_NAME + " INNER JOIN " +
                        MMUContract.FilesEntry.TABLE_NAME +
                        " ON " + MMUContract.FilesEntry.TABLE_NAME +
                        "." + MMUContract.FilesEntry.COLUMN_SUBJECT_KEY +
                        " = " + MMUContract.SubjectEntry.TABLE_NAME +
                        "." + MMUContract.SubjectEntry._ID);

    }
//
//    private Cursor getAnnouncementBySubject(Uri uri, String[] projection, String sortOrder) {
//
//
//    };
    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
