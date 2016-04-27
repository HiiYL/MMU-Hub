package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

//import com.gc.materialdesign.views.ButtonRectangle;
import com.github.hiiyl.mmuhub.data.MMUContract;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hii on 5/13/15.
 */
public class ExamTimetableAdapter extends CursorAdapter {
    public ExamTimetableAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listitem_final_exam_timetable,parent,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView subject_name = (TextView) view.findViewById(R.id.exam_timetable_subject_name_textview);
        TextView subject_time = (TextView) view.findViewById(R.id.exam_timetable_subject_time_textview);
        TextView subject_date = (TextView) view.findViewById(R.id.exam_timetable_subject_date_textview);

        Button add_to_calendar_btn = (Button) view.findViewById(R.id.exam_timetable_add_to_calendar_btn);
        final long start = cursor.getLong(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_FINALS_START_DATETIME));
        final long end = cursor.getLong(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_FINALS_END_DATETIME));
        Log.d("STORED VALUE", String.valueOf(start));
        final Date start_date = new Date(start);
        Date end_date = new Date(end);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd");
        final String exam_subject = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));
        String exam_time = timeFormat.format(start_date) + " - " + timeFormat.format(end_date);
        String exam_date = dateFormat.format(start_date);

        final String exam_calendar_event_name = exam_subject + " Finals";

        if(Utility.isExamInCal(context, exam_subject)) {
            add_to_calendar_btn.setEnabled(false);
            add_to_calendar_btn.setText("Added");

        }else {
            add_to_calendar_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra("beginTime", start);
//                intent.putExtra("allDay", true);
//                intent.putExtra("rrule", "FREQ=YEARLY");
                    intent.putExtra("endTime", end);
                    intent.putExtra("title", exam_calendar_event_name);
                    context.startActivity(intent);
                }
            });
        }

        subject_name.setText(exam_subject);
        subject_time.setText(exam_time);
        subject_date.setText(exam_date);

    }
}
