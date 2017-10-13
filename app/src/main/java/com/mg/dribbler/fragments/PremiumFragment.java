package com.mg.dribbler.fragments;

import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.models.User;

import me.relex.circleindicator.CircleIndicator;

public class PremiumFragment extends Fragment {

    private MainActivity activity;
    private View contentView;
    private RelativeLayout rlViewPagerLayout;
//    private PercentRelativeLayout plMainLayout;

    /**
     * Subscribe Pages
     */
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private CircleIndicator mIndicator;
    private int NUM_PAGES = 3;
    public int current_page = 0;


    /**
     * Life Cycle
     */
    public PremiumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_premium, container, false);
        activity = (MainActivity) getActivity();

        rlViewPagerLayout = (RelativeLayout) contentView.findViewById(R.id.rl_viewpager_container);
//        plMainLayout = (PercentRelativeLayout) contentView.findViewById(R.id.pl_main);
//        contentView.findViewById(R.id.btn_train).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                activity.switchFragment(0);
//            }
//        });

        // Init subscribe view pager
        mPager = (ViewPager) contentView.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mIndicator = (CircleIndicator) contentView.findViewById(R.id.indicator);
        mPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mPager);
        mPager.setCurrentItem(current_page);

//        showViewPager(User.currentUser().subscribe);
//        if (User.currentUser().subscribe == 0) {
//            NUM_PAGES = 3;
//        } else {
//            NUM_PAGES = 2;
//        }
        return contentView;
    }

    /**
     * Private Methods
     */
    private void showViewPager(int subscribeType) {
        if (subscribeType == 3) {
            rlViewPagerLayout.setVisibility(View.GONE);
//            plMainLayout.setVisibility(View.VISIBLE);
        } else {
            rlViewPagerLayout.setVisibility(View.VISIBLE);
//            plMainLayout.setVisibility(View.GONE);
        }
    }

    /**
     * A pager adapter that represents 2 ScreenSlidePageFragment objects, in sequence.
     */
    protected class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            PremiumUpgradeFragment fragment = new PremiumUpgradeFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
