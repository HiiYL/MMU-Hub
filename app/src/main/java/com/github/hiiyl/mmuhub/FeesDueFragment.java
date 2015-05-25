package com.github.hiiyl.mmuhub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fees_due, container, false);
        TextView mYouowe = (TextView) rootView.findViewById(R.id.you_owe_textview);
        mFeesDue = (TextView) rootView.findViewById(R.id.fragment_fees_due_textview);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(mPrefs.contains("fees_due")) {
            mFeesDue.setText("RM " + mPrefs.getString("fees_due", ""));
        }else {
            mYouowe.setVisibility(View.GONE);
            mFeesDue.setText("You have no outstanding charges at this time.");

        }
//        else {
//            mFeesDue.setText("You have no outstanding charges at this time.");
//        }
        return rootView;
    }
}
