package com.mg.dribbler.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.interfaces.OnLoadMoreListener;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Pagination;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.views.MyCircularImageView;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserListFragment extends Fragment {

    private View contentView;
    private MainActivity mActivity;
    private User user;

    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;
    private UserRecyclerViewAdapter mRecyclerAdapter;
    private ArrayList<User> mUsers = new ArrayList<>();
    private Pagination pagination;

    private SearchView searchView;
    private String searchQuery = "";
    
    public UserListFragment() {
        // Required empty public constructor
    }

    public UserListFragment(User user) {
        // Required empty public constructor
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contentView = inflater.inflate(R.layout.fragment_user_list, container, false);
        mActivity = (MainActivity) getActivity();

        // Instantiate Progress Bar
        progressBar = (ProgressBar) contentView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Instantiate Follower List Recycler View
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerAdapter = new UserRecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        searchView = (SearchView) contentView.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                loadUserList();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    searchQuery = "";
                    loadUserList();
                }
                return false;
            }
        });
        loadUserList();
        return contentView;
    }

    /**
     * Unfollow the user
     */
    private void unfollowUser(User user, final Button button, final int position) {
//        button.startAnimation();

        String endPoint = String.format(API.FOLLOW_USER, user.userID);
        RequestParams requestParams = new RequestParams();
        requestParams.put("follow_status", 0);

        WebServiceManager.postWithToken(mActivity, endPoint, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    mUsers.get(position).isFollowing = false;
                    mRecyclerAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("FollowerList", "mUsers array out");
                }
            }
        });
    }
    /**
     * Follow the user
     */
    private void followUser(User user, final Button button, final int position) {
//        button.startAnimation();

        String endPoint = String.format(API.FOLLOW_USER, user.userID);
        RequestParams requestParams = new RequestParams();
        requestParams.put("follow_status", 1);

        WebServiceManager.postWithToken(mActivity, endPoint, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    mUsers.get(position).isFollowing = true;
                    mRecyclerAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("FollowerList", "mUsers array out");
                }
            }
        });
    }
    /**
     * Load Following List From Server
     */
    private void loadUserList() {
        progressBar.setVisibility(View.VISIBLE);
        
        String endPoint = String.format(API.GET_USERS, user.userID);
        endPoint = endPoint + "&query=" + searchQuery;
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                progressBar.setVisibility(View.GONE);
                pagination = ParseServiceManager.parsePaginationInfo(response);
                mUsers = ParseServiceManager.parseOtherUserResposne(response);
                mRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Load My Videos From Server
     */
    public void loadMore() {
        if (pagination.next_page_url == null) {
            return;
        }

        String endPoint = pagination.next_page_url;
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Global.sharedInstance().myVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                ArrayList<User> arr = ParseServiceManager.parseOtherUserResposne(response);
                mUsers.addAll(User.appendUserArray(mUsers, arr));
                mRecyclerAdapter.notifyDataSetChanged();
                mRecyclerAdapter.setLoaded();
            }
        });
    }

    /**
     * RecyclerViewAdapter
     */
    class UserRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // Load More
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading;
        private int visibleThreshold = 1;
        private int lastVisibleItem, totalItemCount;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public MyCircularImageView imageView;
            public TextView tvName;
            public Button button;

            public MyViewHolder(View view) {
                super(view);
                imageView = (MyCircularImageView) view.findViewById(R.id.iv_avatar);
                tvName = (TextView) view.findViewById(R.id.tv_name);
                button = (Button) view.findViewById(R.id.btn_unfollow);
            }
        }

        public UserRecyclerViewAdapter() {
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_follower, parent, false);
            return new UserRecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final UserRecyclerViewAdapter.MyViewHolder myHolder = (UserRecyclerViewAdapter.MyViewHolder) holder;

            final User user = mUsers.get(position);

            Glide.with(mActivity).load(user.photoURL).into(myHolder.imageView);
            myHolder.tvName.setText(user.getFullName());
            myHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user.isFollowing) {
                        unfollowUser(user, myHolder.button, position);
                    } else {
                        followUser(user, myHolder.button, position);
                    }
                }
            });
            if (!user.isFollowing) {
                myHolder.button.setText("FOLLOW");
            } else {
                myHolder.button.setText("UNFOLLOW");
            }
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }
}
