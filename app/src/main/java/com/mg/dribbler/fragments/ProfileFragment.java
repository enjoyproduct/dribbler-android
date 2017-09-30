package com.mg.dribbler.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Pagination;
import com.mg.dribbler.models.Score;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import me.relex.circleindicator.CircleIndicator;

public class ProfileFragment extends Fragment {

    private View contentView;
    private MainActivity mActivity;

    /**
     * View Pager
     */
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private FragmentManager fragmentManager;
    private CircleIndicator mIndicator;
    private static final int NUM_PAGES = 3;

    // My Profile
    public User user;
    public boolean isMyProfile;
    public ArrayList<Video> myVideos;
    public Pagination myVideosPagination;
    public ArrayList<Score> myCategoryScore;
    public ArrayList<Score> myTrickScore;
    public ArrayList<Score> myTagScore;


    /**
     * Life Cycle
     */
    @SuppressLint("ValidFragment")
    public ProfileFragment(User user) {
        isMyProfile = false;
        this.user = user;
        initVariables();
    }

    public ProfileFragment() {
        isMyProfile = true;
        this.user = User.currentUser();
        initVariables();
    }
    void initVariables() {
        myVideos = new ArrayList<>();
        myCategoryScore = new ArrayList<>();
        myTrickScore = new ArrayList<>();
        myTagScore = new ArrayList<>();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();

        // load profile data if user is not mine
//        if (!isMyProfile) {
//            loadVideos();
//            loadProfileStatus();
//        }
        loadVideos();
        loadProfileStatus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_profile, container, false);
        fragmentManager = getFragmentManager();

        // Instantiate View Pager
        mPager = (ViewPager) contentView.findViewById(R.id.vertical_viewpager);
        mPagerAdapter = new ProfileFragment.ScreenSlidePagerAdapter(getChildFragmentManager());
        mIndicator = (CircleIndicator) contentView.findViewById(R.id.indicator);
        mPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mPager);

        return contentView;
    }

    /**
     *
     */
    public void goToFollowerPage(User user) {
        FollowerListFragment fragment = new FollowerListFragment(user);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.addToBackStack(fragment.toString());
        transaction.add(R.id.container, fragment).commit();
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
            Fragment fragment = new Fragment();
            switch (position) {
                case 0:
                    fragment = new ProfileLandingFragment();
                    break;
                case 1:
                    fragment = new ProfileOverviewFragment();
                    break;
                case 2:
                    fragment = new ProfileAchievementFragment();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public void goToProfileSettingPage() {
        ProfileSettingFragment fragment = new ProfileSettingFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.addToBackStack(fragment.toString());
        transaction.add(R.id.container, fragment).commit();
    }


    /**
     * Load other's videos
     */
    public void loadVideos() {
        String endPoint = String.format(API.GET_VIDEO, user.userID);
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                myVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                ArrayList<Video> videos = ParseServiceManager.parseVideoArrayResponse(response);
                appendMyVideo(videos);
                sendNotification(mActivity, AppConstant.BROADCAST_GET_OTHER_VIDEOS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    /**
     * Load Profile Status
     */
    public void loadProfileStatus() {
        String endPoint = String.format(API.GET_PROFILE_STATUS, user.userID);
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    myCategoryScore = ParseServiceManager.parseScoreResponse(response.getJSONArray("category"), "category");
                    myTagScore = ParseServiceManager.parseScoreResponse(response.getJSONArray("tag"), "tag");
                    myTrickScore = ParseServiceManager.parseScoreResponse(response.getJSONArray("trick"), "trick");
                    sendNotification(mActivity, AppConstant.BROADCAST_GET_OTHER_PROFILE_STATISTICS);
                } catch (Exception exception) {
                    Log.e("Parse Error", "Profile Status");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public void appendMyVideo(ArrayList<Video> videos) {
        for (Video video : videos) {
            boolean isExist = false;

            for (Video tempVideo : myVideos) {
                if (video.video_id == tempVideo.video_id) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                myVideos.add(video);
            }
        }
    }

    /**
     * Send Broadcast event
     */
    private static void sendNotification(Context context, String event) {
        Intent intent = new Intent(event);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
