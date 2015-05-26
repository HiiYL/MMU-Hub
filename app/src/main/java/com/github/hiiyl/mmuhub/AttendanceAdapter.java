package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
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
        RelativeLayout attendance_lecture_layout = (RelativeLayout) view.findViewById(R.id.rel_layout_attendence_lecture);
        RelativeLayout attendance_tutorial_layout = (RelativeLayout) view.findViewById(R.id.rel_layout_attendence_tutorial);
        RelativeLayout attendance_laboratory_layout = (RelativeLayout) view.findViewById(R.id.rel_layout_attendence_laboratory);
        ArcProgress attendance_lecture = (ArcProgress) view.findViewById(R.id.arc_attendence_lecture);
        ArcProgress attendance_tutorial = (ArcProgress) view.findViewById(R.id.arc_attendence_tutorial);
        ArcProgress attendance_laboratory = (ArcProgress) view.findViewById(R.id.arc_attendence_laboratory);
        subject_textview.setText(cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME)));
        if(!cursor.isNull(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_LECTURE))) {
            attendance_lecture_layout.setVisibility(View.VISIBLE);
            attendance_lecture.setProgress((int) Float.parseFloat(cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_LECTURE))));
        }
        if(!cursor.isNull(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_TUTORIAL))) {
            attendance_tutorial_layout.setVisibility(View.VISIBLE);
            attendance_tutorial.setProgress((int) Float.parseFloat(cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_TUTORIAL))));
        }
        if(!cursor.isNull(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_LABORATORY))) {
            attendance_laboratory_layout.setVisibility(View.VISIBLE);
            attendance_laboratory.setProgress((int) Float.parseFloat(cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_LABORATORY))));
        }
    }
}
