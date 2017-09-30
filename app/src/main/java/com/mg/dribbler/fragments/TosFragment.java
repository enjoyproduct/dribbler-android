package com.mg.dribbler.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class TosFragment extends Fragment {

    private View contentView;

    private TextView mTitle;
    private TextView mContent;
    private int index;

    public TosFragment(int index) {
        this.index = index;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_tos, container, false);
        mTitle = (TextView) contentView.findViewById(R.id.tv_title);
        mContent = (TextView) contentView.findViewById(R.id.tv_content);

        switch (index) {
            case 0: // Terms & Conditions
                mTitle.setText(getString(R.string.terms_and_service));
                mContent.setText(getString(R.string.terms_and_service_content));
                break;
            case 1: // Data Protection
                mTitle.setText(getString(R.string.data_protection));
                mContent.setText(getString(R.string.data_protection_content));
                break;
        }

        return contentView;
    }

}
