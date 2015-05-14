package com.github.hiiyl.mmuhub.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.github.hiiyl.mmuhub.MySingleton;

/**
 * Created by Hii on 4/19/15.
 */
public class MMUProvider extends ContentProvider {
//    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private SQLiteDatabase mDatabase;

    static final int SUBJECT  = 100;
    static final int ANNOUNCEMENT = 200;
    static final int ANNOUNCEMENT_WITH_WEEK = 201;
    static final int ANNOUNCEMENT_WITH_SUBJECT = 202;
    static final int ANNOUNCEMENT_WITH_SUBJECT_AND_WEEK = 203;
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

    private static final String sSubjectAndWeekSelection =
            MMUContract.SubjectEntry.TABLE_NAME +
                    "." + MMUContract.SubjectEntry._ID + " = ? AND " +
                    MMUContract.WeekEntry.TABLE_NAME + "." + MMUContract.WeekEntry._ID + " = ? ";



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
//        String subject_id = MMUContract.AnnouncementEntry.getSubjectFromUri(uri);
//        Cursor retCursor;
//
//        return retCursor;
//    };
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MMUContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MMUContract.PATH_ANNOUNCEMENT, ANNOUNCEMENT);
        matcher.addURI(authority, MMUContract.PATH_ANNOUNCEMENT + "/*", ANNOUNCEMENT_WITH_SUBJECT);
        matcher.addURI(authority, MMUContract.PATH_ANNOUNCEMENT + "/*/#", ANNOUNCEMENT_WITH_SUBJECT_AND_WEEK);
        matcher.addURI(authority, MMUContract.PATH_WEEK, WEEK);

        matcher.addURI(authority, MMUContract.PATH_SUBJECT, SUBJECT);
        return matcher;
    }
    @Override
    public boolean onCreate() {
        mDatabase = MySingleton.getInstance(getContext()).getDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)) {
            case ANNOUNCEMENT:
            {
                Log.d("ANNOUNCEMENT", "QUERY");
                retCursor = mDatabase.query(
                        MMUContract.AnnouncementEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case WEEK:
            {
                Log.d("WEEK", "QUERY");
                retCursor = mDatabase.query(
                        MMUContract.WeekEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                retCursor.setNotificationUri(getContext().getContentResolver(), MMUContract.WeekEntry.CONTENT_URI);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case ANNOUNCEMENT_WITH_SUBJECT_AND_WEEK:
                return MMUContract.AnnouncementEntry.CONTENT_TYPE;
            case ANNOUNCEMENT_WITH_SUBJECT:
                return MMUContract.AnnouncementEntry.CONTENT_TYPE;
            case ANNOUNCEMENT_WITH_WEEK:
                return MMUContract.AnnouncementEntry.CONTENT_TYPE;
            case ANNOUNCEMENT:
                return MMUContract.AnnouncementEntry.CONTENT_TYPE;
            case WEEK:
                return MMUContract.WeekEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
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
