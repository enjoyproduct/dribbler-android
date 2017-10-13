package com.mg.dribbler.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.interfaces.OnLoadMoreListener;
import com.mg.dribbler.models.Achievement;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Score;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.CommonUtil;
import com.mg.dribbler.utils.GridSpacingItemDecoration;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class ProfileAchievementFragment extends Fragment {

    private MainActivity mActivity;
    private ProfileFragment parentFragment;
    private View contentView;

    // Recycler View
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;

    private ArrayList<Achievement> achievementArrayList = new ArrayList<>();
    private User selectedUser;
    /**
     * Broadcast observer
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    };


    /**
     * Life Cycle
     */
    public ProfileAchievementFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        parentFragment = (ProfileFragment) getParentFragment();
        if (parentFragment.isMyProfile) {
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_MY_PROFILE_STATISTICS));
        } else {
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_OTHER_PROFILE_STATISTICS));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_profile_archievement, container, false);

        // My Video GridView
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, CommonUtil.convertDpToPixels(10f), true));

        if (parentFragment.isMyProfile) {
            selectedUser = User.currentUser();
        } else {
            selectedUser = parentFragment.user;
        }
        loadProfileAchievements();

        return contentView;
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    public void loadProfileAchievements() {
        String endPoint = String.format(API.GET_PROFILE_ACHIEVEMENTS, selectedUser.userID);
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    achievementArrayList = ParseServiceManager.parseAchievement(response.getJSONArray("data"));
                    mRecyclerViewAdapter.notifyDataSetChanged();
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
    /**
     * RecyclerViewAdapter
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView tvTitle ;
            public ImageView imageView;

            public MyViewHolder(View view) {
                super(view);
                tvTitle = (TextView) view.findViewById(R.id.tv_title);
                imageView = (ImageView) view.findViewById(R.id.iv_trophy);
            }
        }

        public RecyclerViewAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_achievement, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerViewAdapter.MyViewHolder myHolder = (RecyclerViewAdapter.MyViewHolder) holder;

            Achievement achievement = achievementArrayList.get(position);
//            Score score;
//            if (parentFragment.isMyProfile) {
//                score = Global.sharedInstance().myTrickScore.get(position);
//            } else {
//                score = parentFragment.myTrickScore.get(position);
//            }

            myHolder.tvTitle.setText(achievement.title + " " + String.format("%.1f", achievement.score) + "/10");
//            if (achievement.score >= 9) {
//                myHolder.imageView.setImageResource(R.mipmap.icon_achievement);
//            } else {
//                myHolder.imageView.setImageResource(R.mipmap.icon_unachievement);
//            }
            if (achievement.achievement == 0) {
                myHolder.imageView.setImageResource(R.drawable.achievement_grayed);
            } else if (achievement.achievement == 1) {
                myHolder.imageView.setImageResource(R.drawable.achievement_bronze);
            } else if (achievement.achievement == 2) {
                myHolder.imageView.setImageResource(R.drawable.achievement_silver);
            } else if (achievement.achievement == 3) {
                myHolder.imageView.setImageResource(R.drawable.achievement_gold);
            }
        }

        @Override
        public int getItemCount() {
//            if (parentFragment.isMyProfile) {
//                return Global.sharedInstance().myTrickScore.size();
//            } else {
//                return parentFragment.myTrickScore.size();
//            }
            return  achievementArrayList.size();
        }
    }
}
