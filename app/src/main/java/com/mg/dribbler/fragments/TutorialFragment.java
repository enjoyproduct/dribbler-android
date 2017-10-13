package com.mg.dribbler.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;


public class TutorialFragment extends Fragment {

    // the fragment initialization parameters
    private static final String ARG_PARAM1 = "param1";
    private int mTutorialPageIndex;

    private View contentView;

    /**
     * Life Cycle
     */
    public TutorialFragment() {
        // Required empty public constructor
    }

    public static TutorialFragment newInstance(int index) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTutorialPageIndex = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        switch (mTutorialPageIndex) {
            case 0:
                contentView = inflater.inflate(R.layout.fragment_tutorial1, container, false);
                instantiateSkillButton(contentView);
                break;
            case 1:
                contentView = inflater.inflate(R.layout.fragment_tutorial2, container, false);
                instantiateSkillButton(contentView);
                break;
            case 2:
                contentView = inflater.inflate(R.layout.fragment_tutorial3, container, false);
                instantiateSkillButton(contentView);
                break;
            case 3:
                contentView = inflater.inflate(R.layout.fragment_tutorial4, container, false);
                instantiateSkillButton(contentView);
                break;
            case 4:
                contentView = inflater.inflate(R.layout.fragment_tutorial5, container, false);
                instantiateTrainButton(contentView);
                break;
        }

        return contentView;
    }

    /**
     * Private Methodsa
     */
    private void instantiateSkillButton(View view) {
        Button btn = (Button) view.findViewById(R.id.btn_skill);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).switchFragment(1);
            }
        });
    }

    private void instantiateTrainButton(View view) {
        Button btn = (Button) view.findViewById(R.id.btn_train);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).switchFragment(1);
            }
        });
    }

}
