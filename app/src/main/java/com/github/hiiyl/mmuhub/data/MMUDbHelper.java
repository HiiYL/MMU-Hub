package com.github.hiiyl.mmuhub.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.hiiyl.mmuhub.data.MMUContract.AnnouncementEntry;
import com.github.hiiyl.mmuhub.data.MMUContract.SubjectEntry;
import com.github.hiiyl.mmuhub.data.MMUContract.WeekEntry;
import com.github.hiiyl.mmuhub.data.MMUContract.FilesEntry;
/**
 * Created by Hii on 4/19/15.
 */
public class MMUDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "mmuhub.db";

    public MMUDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SUBJECT_TABLE = "CREATE TABLE " + SubjectEntry.TABLE_NAME + " (" +
                SubjectEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SubjectEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL " +
                " );";
        final String SQL_CREATE_WEEK_TABLE = "CREATE TABLE " + WeekEntry.TABLE_NAME + " (" +
                WeekEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WeekEntry.COLUMN_SUBJECT_KEY + " INTEGER NOT NULL, " +
                WeekEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" +WeekEntry.COLUMN_SUBJECT_KEY + ") REFERENCES " +
                SubjectEntry.TABLE_NAME + " (" + SubjectEntry._ID + "));";
        final String SQL_CREATE_ANNOUNCEMENT_TABLE = "CREATE TABLE " + AnnouncementEntry.TABLE_NAME + " (" +
                AnnouncementEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AnnouncementEntry.COLUMN_SUBJECT_KEY + " INTEGER NOT NULL, " +
                AnnouncementEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                AnnouncementEntry.COLUMN_CONTENTS + " TEXT NOT NULL, " +
                AnnouncementEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                AnnouncementEntry.COLUMN_POSTED_DATE + " TEXT NOT NULL, " +
                AnnouncementEntry.COLUMN_WEEK_KEY + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" +AnnouncementEntry.COLUMN_SUBJECT_KEY + ") REFERENCES " +
                SubjectEntry.TABLE_NAME + " (" + SubjectEntry._ID + "), " +
                "FOREIGN KEY (" +AnnouncementEntry.COLUMN_WEEK_KEY + ") REFERENCES " +
                WeekEntry.TABLE_NAME + " (" + WeekEntry._ID + "));";
        final String SQL_CREATE_FILES_TABLE = "CREATE TABLE " + FilesEntry.TABLE_NAME + " (" +
                FilesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FilesEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_TOKEN + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_FILE_PATH + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_CONTENT_TYPE + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_SUBJECT_KEY + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + FilesEntry.COLUMN_SUBJECT_KEY + ") REFERENCES " +
                SubjectEntry.TABLE_NAME + " (" + SubjectEntry._ID + " );";
        sqLiteDatabase.execSQL(SQL_CREATE_SUBJECT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEEK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ANNOUNCEMENT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SubjectEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeekEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AnnouncementEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public void onLogout(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SubjectEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeekEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AnnouncementEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
