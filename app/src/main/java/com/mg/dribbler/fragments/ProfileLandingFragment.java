package com.mg.dribbler.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.activities.VideoDetailActivity;
import com.mg.dribbler.interfaces.OnLoadMoreListener;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.CommonUtil;
import com.mg.dribbler.utils.GridSpacingItemDecoration;
import com.mg.dribbler.views.MyCircularImageView;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class ProfileLandingFragment extends Fragment {

    private MainActivity mActivity;
    private ProfileFragment parentFragment;
    private FragmentManager fragmentManager;
    private View contentView;

    private LinearLayout layoutList;
    private RelativeLayout layoutFollowing;
    private TextView tvFollow;
    private ImageView ivMedal;

    // Recycler View
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;

    private User selectedUser;
    TextView tvOverallRanking;
    TextView tvTricksCompletion;
    TextView tvFollower;
    TextView tvVideoCount;
    TextView tvDribbleScore;
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
    public ProfileLandingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        parentFragment = (ProfileFragment) getParentFragment();
        fragmentManager = getFragmentManager();

        if (parentFragment.isMyProfile) {
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_MY_VIDEOS));
        } else {
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_OTHER_VIDEOS));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_profile_landing, container, false);

        // Setting Page
        LinearLayout llSetting = (LinearLayout) contentView.findViewById(R.id.ll_setting);
        llSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.goToProfileSettingPage();
            }
        });

        // Following Button and Following List
        layoutList = (LinearLayout) contentView.findViewById(R.id.ll_follower);
        layoutFollowing = (RelativeLayout) contentView.findViewById(R.id.rl_following);
        tvFollow = (TextView) contentView.findViewById(R.id.tv_follow);

        if (parentFragment.isMyProfile) {
            selectedUser = User.currentUser();
            layoutList.setVisibility(View.VISIBLE);
            layoutFollowing.setVisibility(View.GONE);
        } else {
            selectedUser = parentFragment.user;
            llSetting.setVisibility(View.GONE);
            layoutList.setVisibility(View.GONE);
            layoutFollowing.setVisibility(View.VISIBLE);
        }
        loadOtherUserProfile();

        // Set avatar and full name
        MyCircularImageView imageView = (MyCircularImageView) contentView.findViewById(R.id.iv_avatar);
        TextView textView = (TextView) contentView.findViewById(R.id.tv_name);
        Glide.with(mActivity).load(selectedUser.photoURL).into(imageView);
        textView.setText(selectedUser.getFullName());
        ivMedal = (ImageView) contentView.findViewById(R.id.iv_medal);
        switch (selectedUser.dribble_medal) {
            case 0:
                break;
            case 1:
                ivMedal.setImageResource(R.drawable.profile_medal_bronze);
                break;
            case 2:
                ivMedal.setImageResource(R.drawable.profile_medal_silver);
            case 3:
                ivMedal.setImageResource(R.drawable.profile_medal_gold);
                break;
        }

        contentView.findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.switchFragment(1);
            }
        });
        tvFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followUser(selectedUser);
            }
        });

        // Init Score Panel
        tvOverallRanking   = (TextView) contentView.findViewById(R.id.tv_overall);
        tvTricksCompletion = (TextView) contentView.findViewById(R.id.tv_complete);
        tvFollower         = (TextView) contentView.findViewById(R.id.tv_follower);
        tvVideoCount       = (TextView) contentView.findViewById(R.id.tv_video);
        tvDribbleScore     = (TextView) contentView.findViewById(R.id.tv_score);

        setSocialInfo();

        contentView.findViewById(R.id.ll_follower).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            parentFragment.goToFollowerPage(selectedUser);
            }
        });

        // My Video GridView
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, CommonUtil.convertDpToPixels(10f), true));
        mRecyclerViewAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });

        return contentView;
    }

    void setSocialInfo() {
        tvOverallRanking.setText(String.valueOf(selectedUser.overall_ranking));
        tvTricksCompletion.setText(String.valueOf(selectedUser.trick_completion_count));
        tvFollower.setText(String.valueOf(selectedUser.follower_count));
        tvVideoCount.setText(String.valueOf(selectedUser.video_count));
        tvDribbleScore.setText(String.valueOf(selectedUser.dribble_score));

    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * Load My Videos From Server
     */
    public void loadMore() {
        if (parentFragment.isMyProfile) {
            if (Global.sharedInstance().myVideosPagination.next_page_url == null) {
                return;
            }

            String endPoint = Global.sharedInstance().myVideosPagination.next_page_url;
            WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Global.sharedInstance().myVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                    ArrayList<Video> arr = ParseServiceManager.parseVideoArrayResponse(response);
                    Global.sharedInstance().myVideos.addAll(Video.appendVideoArray(Global.sharedInstance().myVideos, arr));
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    mRecyclerViewAdapter.setLoaded();
                }
            });

        } else {
            if (parentFragment.myVideosPagination.next_page_url == null) {
                return;
            }

            String endPoint = parentFragment.myVideosPagination.next_page_url;
            WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    parentFragment.myVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                    ArrayList<Video> arr = ParseServiceManager.parseVideoArrayResponse(response);
                    parentFragment.myVideos.addAll(Video.appendVideoArray(parentFragment.myVideos, arr));
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    mRecyclerViewAdapter.setLoaded();
                }
            });
        }
    }

    /**
     * RecyclerViewAdapter
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // Load More
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading;
        private int visibleThreshold = 1;
        private int lastVisibleItem, totalItemCount;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public PercentRelativeLayout container;

            public MyViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.iv_thumb);
                container = (PercentRelativeLayout) view.findViewById(R.id.prl_container);
            }
        }

        public RecyclerViewAdapter() {
            final GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = layoutManager.getItemCount();
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }

        public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
            this.mOnLoadMoreListener = mOnLoadMoreListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_video_item, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerViewAdapter.MyViewHolder myHolder = (RecyclerViewAdapter.MyViewHolder) holder;

            final Video video;
            if (parentFragment.isMyProfile) {
                video = Global.sharedInstance().myVideos.get(position);
            } else {
                video = parentFragment.myVideos.get(position);
            }

            Glide.with(mActivity).load(video.thumbnail).into(myHolder.imageView);
            myHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoVideoDetailPage(video);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (parentFragment.isMyProfile) {
                return Global.sharedInstance().myVideos.size();
            } else {
                return parentFragment.myVideos.size();
            }
        }

        public void setLoaded() {
            isLoading = false;
        }
    }

    /**
     * Navigate Methods
     */
    private void gotoVideoDetailPage(Video video) {
        Intent intent = new Intent(mActivity, VideoDetailActivity.class);
        intent.putExtra("video", video);
        if (parentFragment.isMyProfile) {
            intent.putExtra("username", User.currentUser().getFullName());
            intent.putExtra("photo", User.currentUser().photoURL);
        } else {
            intent.putExtra("username", parentFragment.user.getFullName());
            intent.putExtra("photo", parentFragment.user.photoURL);
        }

        startActivity(intent);
    }

    /**
     * Unfollow the user
     */
    private void followUser(User user) {
        String endPoint = String.format(API.FOLLOW_USER, user.userID);
        RequestParams requestParams = new RequestParams();
        int isFollow = selectedUser.isFollowing == true ? 0 : 1;
        requestParams.put("follow_status", isFollow);

        WebServiceManager.postWithToken(mActivity, endPoint, requestParams, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Boolean isFollowing = (response.getInt("follow_status") == 0 ? false : true);
                    selectedUser.isFollowing = isFollowing;
                    if (selectedUser.isFollowing) {
                        tvFollow.setText("Unfollow");
                    } else {
                        tvFollow.setText("Follow");
                    }
                } catch (Exception e) {
                    Log.e("Parse exception", "Profile Landing");
                }
            }
        });
    }

    /**
     * Load Other Profile
     */
    private void loadOtherUserProfile() {
        String endPoint = String.format(API.GET_OTHER_PROFILE, selectedUser.userID);
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    selectedUser = ParseServiceManager.parseOtherUserResponse(response);
                    selectedUser.isFollowing = (0 == response.getInt("isFollowing") ? false : true);
                } catch (Exception e) {
                    Log.e("Profile Landing Parse", "Other User Profile");
                }
                if (selectedUser.isFollowing) {
                    tvFollow.setText("Unfollow");
                } else {
                    tvFollow.setText("Follow");
                }
                setSocialInfo();
            }
        });
    }
}
