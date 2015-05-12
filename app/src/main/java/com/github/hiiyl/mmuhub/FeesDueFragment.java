package com.github.hiiyl.mmuhub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.hiiyl.mmuhub.helper.AttendanceCompleteEvent;

import de.greenrobot.event.EventBus;


public class FeesDueFragment extends android.support.v4.app.Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private SharedPreferences mPrefs;
    private TextView mFeesDue;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FeesDueFragment newInstance(int sectionNumber) {
        FeesDueFragment fragment = new FeesDueFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FeesDueFragment() {
    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEventMainThread(AttendanceCompleteEvent event) {
        if(mPrefs.contains("fees_due")) {
            mFeesDue.setText(mPrefs.getString("fees_due", ""));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fees_due, container, false);
        mFeesDue = (TextView) rootView.findViewById(R.id.fragment_fees_due_textview);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(mPrefs.contains("fees_due")) {
            mFeesDue.setText("FEES DUE: $" + mPrefs.getString("fees_due", ""));
        }else {
            mFeesDue.setText("You have no outstanding charges at this time.");
        }
        return rootView;
    }
}
