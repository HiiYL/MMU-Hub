package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.lzyzsd.circleprogress.ArcProgress;

/**
 * Created by Hii on 5/9/15.
 */
public class AttendanceAdapter extends CursorAdapter {

    public AttendanceAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listitem_attendance,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d("BIND VIEW", "CALLED");
        TextView subject_textview = (TextView) view.findViewById(R.id.section_label);
        ArcProgress attendance_lecture = (ArcProgress) view.findViewById(R.id.arc_attendence_lecture);
        ArcProgress attendance_tutorial = (ArcProgress) view.findViewById(R.id.arc_attendence_tutorial);
        subject_textview.setText(cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME)));
        if(!cursor.isNull(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_LECTURE))) {
            attendance_lecture.setVisibility(View.VISIBLE);
            attendance_lecture.setProgress((int) Float.parseFloat(cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_LECTURE))));
        }
        if(!cursor.isNull(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_TUTORIAL))) {
            attendance_tutorial.setVisibility(View.VISIBLE);
            attendance_tutorial.setProgress((int) Float.parseFloat(cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_TUTORIAL))));
        }
    }
}
