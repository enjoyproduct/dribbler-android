package com.mg.dribbler.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.models.Dribbler;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Pagination;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.UIUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import me.relex.circleindicator.CircleIndicator;


public class VideoDetailFragment extends Fragment {

    private MainActivity mActivity;
    private View contentView;

    /**
     * View Pager
     */
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private CircleIndicator mIndicator;
    private static final int NUM_PAGES = 6;

    /**
     * Variables for ViewPages
     */
    public ArrayList<User> trickUsers = new ArrayList<>();
    public ArrayList<Video> othersTrickVideos = new ArrayList<>();
    public ArrayList<Video> myTrickVideos = new ArrayList<>();
    public static ArrayList<Dribbler> myTrickDribblers = new ArrayList<>();

    public Pagination myTrickVideosPagination;
    public Pagination otherTrickVideosPagination;
    public Pagination trickUsersPagination;


    /**
     * Life Cycle
     */
    public VideoDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        // Load Trick Data
        loadMyTrickStatistics();
        loadMyTrickVideo();
        loadOtherTrickVideo();
        loadBestUserOfTrick();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contentView = inflater.inflate(R.layout.fragment_video_detail, container, false);

        // Instantiate View Pager
        mPager = (ViewPager) contentView.findViewById(R.id.view_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mIndicator = (CircleIndicator) contentView.findViewById(R.id.indicator);
        mPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mPager);

        return contentView;
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
                    fragment = new VideoDetailLandingFragment();
                    break;
                case 1:
                    fragment = new VideoDetailDescriptionFragment();
                    break;
                case 2:
                    fragment = new VideoDetailStatisticsFragment();
                    break;
                case 3:
                    fragment = new VideoDetailMyVideosFragment();
                    break;
                case 4:
                    fragment = new VideoDetailOtherVideosFragment();
                    break;
                case 5:
                    fragment = new VideoDetailBestUsersFragment();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }



    /**
     * APIs
     */

    public void loadMyTrickStatistics() {
        String endPoint = String.format(API.GET_TRICK_STATISTICS, Global.sharedInstance().selectedTrick.trick_id);
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                myTrickDribblers = ParseServiceManager.parseDribblerResponse(response);
                sendNotification(AppConstant.BROADCAST_GET_TRICK_STATISTICS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public void loadMyTrickVideo() {
        String endPoint = String.format(API.GET_TRICK_MY_VIDEOS, Global.sharedInstance().selectedTrick.trick_id);

        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                myTrickVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                myTrickVideos = ParseServiceManager.parseVideoArrayResponse(response);
                sendNotification(AppConstant.BROADCAST_GET_TRICK_MY_VIDEOS);
            }
        });
    }

    public void loadOtherTrickVideo() {
        String endPoint = String.format(API.GET_TRICK_OTHER_VIDEOS, Global.sharedInstance().selectedTrick.trick_id);

        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                otherTrickVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                othersTrickVideos = ParseServiceManager.parseVideoArrayResponse(response);
                sendNotification(AppConstant.BROADCAST_GET_TRICK_OTHER_VIDEOS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public void loadBestUserOfTrick() {
        String endPoint = String.format(API.GET_TRICK_USERS, Global.sharedInstance().selectedTrick.trick_id);

        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                trickUsersPagination = ParseServiceManager.parsePaginationInfo(response);
                trickUsers = ParseServiceManager.parseOtherUserResposne(response);
                sendNotification(AppConstant.BROADCAST_GET_TRICK_BEST_USERS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    /**
     * Post Video
     */
    public void postVideo(Context context, RequestParams params, final JsonHttpResponseHandler handler) {
        WebServiceManager.postWithToken(context, API.POST_VIDEO, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                handler.onSuccess(statusCode, headers, response);
                Video video = ParseServiceManager.parseVideoResponse(response);
                myTrickVideos.add(video);
                Global.sharedInstance().myVideos.add(video);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                handler.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                handler.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    /**
     * Send Broadcast event
     */
    private void sendNotification(String event) {
        Intent intent = new Intent(event);
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
    }
}
