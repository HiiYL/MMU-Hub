package com.github.hiiyl.mmuhub.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.hiiyl.mmuhub.data.MMUContract.AnnouncementEntry;
import com.github.hiiyl.mmuhub.data.MMUContract.BulletinEntry;
import com.github.hiiyl.mmuhub.data.MMUContract.FilesEntry;
import com.github.hiiyl.mmuhub.data.MMUContract.SubjectEntry;
import com.github.hiiyl.mmuhub.data.MMUContract.WeekEntry;
/**
 * Created by Hii on 4/19/15.
 */
public class MMUDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 16;

    static final String DATABASE_NAME = "mmuhub.db";

    public MMUDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SUBJECT_TABLE = "CREATE TABLE " + SubjectEntry.TABLE_NAME + " (" +
                SubjectEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SubjectEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                SubjectEntry.COLUMN_URL + " TEXT NOT NULL," +
                SubjectEntry.COLUMN_ATTENDANCE_LECTURE + " REAL,"+
                SubjectEntry.COLUMN_ATTENDANCE_TUTORIAL + " REAL," +
                SubjectEntry.COLUMN_FINALS_START_DATETIME + " INTEGER," +
                SubjectEntry.COLUMN_FINALS_END_DATETIME + " INTEGER" +
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
                AnnouncementEntry.COLUMN_HAS_SEEN + " BOOLEAN NOT NULL DEFAULT FALSE," +  //TODO SEE IF THIS WORKS
                "FOREIGN KEY (" +AnnouncementEntry.COLUMN_SUBJECT_KEY + ") REFERENCES " +
                SubjectEntry.TABLE_NAME + " (" + SubjectEntry._ID + "), " +
                "FOREIGN KEY (" +AnnouncementEntry.COLUMN_WEEK_KEY + ") REFERENCES " +
                WeekEntry.TABLE_NAME + " (" + WeekEntry._ID + "));";
        final String SQL_CREATE_FILES_TABLE = "CREATE TABLE " + FilesEntry.TABLE_NAME + " (" +
                FilesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FilesEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_TOKEN + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_REMOTE_FILE_PATH + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_LOCAL_FILE_PATH + " TEXT, " +
                FilesEntry.COLUMN_CONTENT_ID + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_CONTENT_TYPE + " TEXT NOT NULL, " +
                FilesEntry.COLUMN_SUBJECT_KEY + " INTEGER, " +
                FilesEntry.COLUMN_ANNOUNCEMENT_KEY + " INTEGER, " +
                FilesEntry.COLUMN_DOWNLOADED + " BOOLEAN, " +
                "FOREIGN KEY (" + FilesEntry.COLUMN_ANNOUNCEMENT_KEY + ") REFERENCES " +
                AnnouncementEntry.TABLE_NAME + " (" + AnnouncementEntry._ID + ") " +
                "FOREIGN KEY (" + FilesEntry.COLUMN_SUBJECT_KEY + ") REFERENCES " +
                SubjectEntry.TABLE_NAME + " (" + SubjectEntry._ID + " ));";
        final String SQL_CREATE_BULLETIN_TABLE = "CREATE TABLE " + BulletinEntry.TABLE_NAME + " (" +
                BulletinEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BulletinEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                BulletinEntry.COLUMN_POSTED_DATE + " TEXT NOT NULL, " +
                BulletinEntry.COLUMN_CONTENTS + " TEXT NOT NULL, " +
                BulletinEntry.COLUMN_AUTHOR + " TEXT NOT NULL," +
                BulletinEntry.COLUMN_HAS_SEEN + " BOOLEAN NOT NULL DEFAULT FALSE " +  //TODO SEE IF THIS WORKS
//                SubjectEntry.COLUMN_URL + " TEXT NOT NULL" +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_SUBJECT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEEK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ANNOUNCEMENT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FILES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_BULLETIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        Log.d("DATABASE ON UPGRADE", "HELL OTHERE");
        Log.d("OLD VERSION", String.valueOf(oldVersion));
        if(oldVersion < 15) {
            Log.d("DATABASE", "ADDING COLUMN");
            if(oldVersion < 14) {
                final String SQL_ADD_COLUMN_ATTENDANCE_LECTURE = " ALTER TABLE " + SubjectEntry.TABLE_NAME +
                        " ADD COLUMN " + SubjectEntry.COLUMN_ATTENDANCE_LECTURE + " REAL;";
                sqLiteDatabase.execSQL(SQL_ADD_COLUMN_ATTENDANCE_LECTURE);
            }
            final String SQL_ADD_COLUMN_ATTENDANCE_TUTORIAL = " ALTER TABLE " + SubjectEntry.TABLE_NAME +
                    " ADD COLUMN " + SubjectEntry.COLUMN_ATTENDANCE_TUTORIAL + " REAL;";
            sqLiteDatabase.execSQL(SQL_ADD_COLUMN_ATTENDANCE_TUTORIAL);
        }
        if(oldVersion < 16) {
            Log.d("DATABASE", "ADDING FINALS COLUMN");
            final String SQL_ADD_COLUMN_FINALS_START_DATETIME = " ALTER TABLE " + SubjectEntry.TABLE_NAME +
                    " ADD COLUMN " + SubjectEntry.COLUMN_FINALS_START_DATETIME + " INTEGER;";
            sqLiteDatabase.execSQL(SQL_ADD_COLUMN_FINALS_START_DATETIME);
            final String SQL_ADD_COLUMN_FINALS_END_DATETIME = " ALTER TABLE " + SubjectEntry.TABLE_NAME +
                    " ADD COLUMN " + SubjectEntry.COLUMN_FINALS_END_DATETIME + " INTEGER;";
            sqLiteDatabase.execSQL(SQL_ADD_COLUMN_FINALS_END_DATETIME);
        }
    }
    public void onLogout(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SubjectEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeekEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AnnouncementEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FilesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BulletinEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
