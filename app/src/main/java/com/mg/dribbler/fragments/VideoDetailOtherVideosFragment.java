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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.activities.VideoDetailActivity;
import com.mg.dribbler.interfaces.OnLoadMoreListener;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.CommonUtil;
import com.mg.dribbler.utils.GridSpacingItemDecoration;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class VideoDetailOtherVideosFragment extends Fragment {

    private MainActivity mActivity;
    private View contentView;
    private VideoDetailFragment mParentFragment;

    // Recycler View
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;

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

    public VideoDetailOtherVideosFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_TRICK_OTHER_VIDEOS));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        contentView = inflater.inflate(R.layout.fragment_video_detail_other_videos, container, false);
        mParentFragment = (VideoDetailFragment) getParentFragment();

        TextView tvBestVideo = (TextView) contentView.findViewById(R.id.tv_best_user_video);
        tvBestVideo.setText("Best User " + Global.sharedInstance().selectedTrick.trick_title + " Videos");

        // Recycler View of Other Videos
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.rv_user_video);
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

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * Load Videos From Server
     */
    public void loadMore() {
        if (mParentFragment.otherTrickVideosPagination.next_page_url == null) {
            return;
        }

        String endPoint = mParentFragment.otherTrickVideosPagination.next_page_url;

        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                mParentFragment.otherTrickVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                ArrayList<Video> arr = ParseServiceManager.parseVideoArrayResponse(response);
                mParentFragment.othersTrickVideos = Video.appendVideoArray(mParentFragment.othersTrickVideos, arr);
                mRecyclerViewAdapter.notifyDataSetChanged();
                mRecyclerViewAdapter.setLoaded();
            }
        });
    }

    /**
     * Navigate Methods
     */
    private void gotoVideoDetailPage(Video video) {
        Intent intent = new Intent(mActivity, VideoDetailActivity.class);
        intent.putExtra("video", video);
        intent.putExtra("username", video.user.getFullName());
        intent.putExtra("photo", video.user.photoURL);
        startActivity(intent);
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
            MyViewHolder myHolder = (MyViewHolder) holder;

            final Video video;
            video = mParentFragment.othersTrickVideos.get(position);

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
            return mParentFragment.othersTrickVideos.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }

}
