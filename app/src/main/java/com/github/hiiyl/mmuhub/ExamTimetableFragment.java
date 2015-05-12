package com.github.hiiyl.mmuhub;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.hiiyl.mmuhub.data.MMUContract;

public class ExamTimetableFragment extends android.support.v4.app.Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ListView mListView;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ExamTimetableFragment newInstance(int sectionNumber) {
        ExamTimetableFragment fragment = new ExamTimetableFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exam_timetable, container, false);
        mListView = (ListView) rootView.findViewById(R.id.exam_timetable_listview);
        Cursor timetable_cursor = MySingleton.getInstance(getActivity()).getDatabase().query(
                MMUContract.SubjectEntry.TABLE_NAME, null,
                MMUContract.SubjectEntry.COLUMN_FINALS_START_DATETIME + " NOT NULL",
                null,
                null,
                null,
                null
                , null);
        ExamTimetableAdapter mAdapter = new ExamTimetableAdapter(getActivity(), timetable_cursor, 0);
        mListView.setAdapter(mAdapter);
        return rootView;
    }
}
