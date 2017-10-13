package com.mg.dribbler.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.interfaces.OnLoadMoreListener;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.views.MyCircularImageView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class VideoDetailBestUsersFragment extends Fragment {

    private MainActivity mActivity;
    private View contentView;
    private VideoDetailFragment mParentFragment;

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
    public VideoDetailBestUsersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_TRICK_BEST_USERS));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contentView = inflater.inflate(R.layout.fragment_video_detail_best_users, container, false);
        mActivity = (MainActivity) getActivity();
        mParentFragment = (VideoDetailFragment) getParentFragment();

        ((TextView)contentView.findViewById(R.id.tv_title)).setText(Global.sharedInstance().selectedTrick.trick_title + " Ranking");
        // Initialize ListView
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
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
     * Load More Video From Server
     */
    private void loadMore() {
        if (mParentFragment.trickUsersPagination.next_page_url == null) {
            return;
        }

        String endPoint = mParentFragment.trickUsersPagination.next_page_url;

        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                mParentFragment.trickUsersPagination = ParseServiceManager.parsePaginationInfo(response);
                ArrayList<User> arr = ParseServiceManager.parseOtherUserResposne(response);
                mParentFragment.trickUsers = User.appendUserArray(mParentFragment.trickUsers, arr);
                mRecyclerViewAdapter.notifyDataSetChanged();
                mRecyclerViewAdapter.setLoaded();
            }
        });
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

            public TextView tvNumber;
            public TextView tvUserName;
            public TextView scoreView;
            public MyCircularImageView imageView;

            public MyViewHolder(View view) {
                super(view);
                tvNumber = (TextView) view.findViewById(R.id.tv_number);
                tvUserName = (TextView) view.findViewById(R.id.tv_name);
                scoreView = (TextView) view.findViewById(R.id.tv_score);
                imageView = (MyCircularImageView) view.findViewById(R.id.iv_avatar);
            }
        }

        public RecyclerViewAdapter() {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_trick_user, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myHolder = (MyViewHolder) holder;

            final User user = mParentFragment.trickUsers.get(position);

            myHolder.tvNumber.setText("#" + String.valueOf(position + 1));
            myHolder.tvUserName.setText(user.first_name + " " + user.last_name);
            myHolder.scoreView.setText("Dribbler Score: " + user.dribble_score);
            Glide.with(mActivity).load(user.photoURL).into(myHolder.imageView);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user.userID == User.currentUser().userID) {
                        mActivity.switchFragment(3); // Goto My Profile Page
                    } else {
                        mActivity.goToOtherUserProfilePage(user);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mParentFragment.trickUsers.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }

}
