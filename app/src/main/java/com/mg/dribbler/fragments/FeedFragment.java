package com.mg.dribbler.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.CommentActivity;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.activities.VideoPlayerActivity;
import com.mg.dribbler.dialogs.TagFilterDialog;
import com.mg.dribbler.interfaces.TagFilterDialogListener;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Pagination;
import com.mg.dribbler.models.Tag;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.TimeAgo;
import com.mg.dribbler.utils.TimeUtility;
import com.mg.dribbler.views.MyCircularImageView;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class FeedFragment extends Fragment {

    private View contentView;
    private FragmentManager fragmentManager;
    private MainActivity mActivity;

    /* Tab Bar */
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private boolean isSelectedGlobal = true;
    private SearchView searchView;

    /* Feed List */
//    private LinearLayout llFeed;
    private RecyclerView mRecyclerView;
    private FeedListAdapter mFeedListAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean loading = true;

    /* Feed TagList */
//    private ArrayList<Tag> mFilterTagArray = new ArrayList<>(Global.sharedInstance().tagArr.subList(0,4));
    private ArrayList<Tag> mFilterTagArray = new ArrayList<>();
    private String searchQuery = "";

    Pagination mPagination;
    ArrayList<Video> mFeeds = new ArrayList<>();

    /**
     * Life Cycle
     */
    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_feed, container, false);
        mActivity = (MainActivity) getActivity();
        fragmentManager = getFragmentManager();

        // Init Tab
        tabLayout = (TabLayout) contentView.findViewById(R.id.tab_bar);
        tabLayout.addTab(tabLayout.newTab().setText("Global"),true);
        tabLayout.addTab(tabLayout.newTab().setText("Follower"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        isSelectedGlobal = true;
                        break;
                    case 1:
                        isSelectedGlobal = false;
                        break;
                }

                loadFeeds();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Load feeds from server
        progressBar = (ProgressBar) contentView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Initialize Feed RecyclerView
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        mFeedListAdapter = new FeedListAdapter();
        mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mFeedListAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFeeds();
                //refreshContent();
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { //check for scroll down
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount - 3) {
                            loading = false;
                            loadMore();
                        }
                    }
                }
            }
        });

        searchView = (SearchView) contentView.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                loadWithQuery();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    searchQuery = "";
                    loadWithQuery();
                }
                return false;
            }
        });
        // Initialize Follower Recycler View
        return contentView;
    }

    @Override
    public void onResume() {
        if (Global.sharedInstance().tagArr.size() > 0) {
            mFilterTagArray = new ArrayList<>(Global.sharedInstance().tagArr.subList(0,4));
        } else {
            mFilterTagArray = new ArrayList<>(Global.sharedInstance().tagArr);
        }
        loadFeeds();
        super.onResume();
    }

    private void showTagFilterDlg() {
        TagFilterDialog dialog = new TagFilterDialog(mActivity, mFilterTagArray, new TagFilterDialogListener() {
            @Override
            public void filteredTags(ArrayList<Tag> selectedTags) {
                mFilterTagArray = selectedTags;
                loadWithTag();
            }
        });
        dialog.show();
    }


    /**
     * Feed Recycler View Adapter
     */
    private final int CARD_TYPE_TAG = 0;
    private final int CARD_TYPE_FEED = 1;

    public class FeedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public MyCircularImageView ivAvatar;
            public TextView tvName;
            public TextView tvTimeAgo;
            public TextView tvGoals;
            public TextView tvComments;
            public ImageView ivComment;
            public ImageView ivShare;
            public ImageView ivThumb;
            public ImageView ivPlay;
            public LikeButton btnLike;

            public RecyclerView tagRecyclerView;
            public SubTagAdapter subTagAdapter;

            public MyViewHolder(View view) {
                super(view);
                ivAvatar = (MyCircularImageView) view.findViewById(R.id.iv_avatar);
                tvName = (TextView) view.findViewById(R.id.tv_username);
                tvTimeAgo = (TextView) view.findViewById(R.id.tv_time);
                tvGoals = (TextView) view.findViewById(R.id.tv_goals);
                tvComments = (TextView) view.findViewById(R.id.tv_comments);
                ivComment = (ImageView) view.findViewById(R.id.iv_comment);
                ivShare = (ImageView) view.findViewById(R.id.iv_share);
                ivThumb = (ImageView) view.findViewById(R.id.iv_thumb);
                ivPlay = (ImageView) view.findViewById(R.id.iv_play);
                btnLike = (LikeButton) view.findViewById(R.id.btn_rate); // Like Button

                tagRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
                tagRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
                subTagAdapter = new SubTagAdapter(new ArrayList<String>());
                tagRecyclerView.setAdapter(subTagAdapter);
            }
        }

        public class TagViewHolder extends RecyclerView.ViewHolder {

            public HorizontalTagAdapter tagAdapter;
            public RecyclerView tagRecyclerView;

            public TagViewHolder(View view) {
                super(view);
                tagRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_follower);
                tagRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
                tagAdapter = new HorizontalTagAdapter();
                tagRecyclerView.setAdapter(tagAdapter);
            }
        }

        public FeedListAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == CARD_TYPE_TAG) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_feed_tag, parent, false);
                return new TagViewHolder(itemView);
            }

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_feed, parent, false);
            return new FeedFragment.FeedListAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final Video video = mFeeds.get(position - 1);
                final MyViewHolder myViewHolder = (MyViewHolder) holder;

                myViewHolder.tvName.setText(video.user.getFullName());
                String ago = new TimeAgo().timeAgo(TimeUtility.getLocalDateFromUTCString(video.created_at));
                myViewHolder.tvTimeAgo.setText(ago);
                Glide.with(mActivity).load(video.user.photoURL).into(myViewHolder.ivAvatar);
                Glide.with(mActivity).load(video.thumbnail).into(myViewHolder.ivThumb);
                myViewHolder.tvGoals.setText(getLikesString(video.likes));
                myViewHolder.tvComments.setText(getCommentsString(video.comments));
                myViewHolder.tvComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoCommentPage(false, video);
                    }
                });
                myViewHolder.ivShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        share(video);
                    }
                });
                myViewHolder.ivComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoCommentPage(true, video);
                    }
                });
                // when tap in thumbnail
                myViewHolder.ivThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo(video.hd_url);
                    }
                });

                // Like Button
                Boolean isLiked = Global.sharedInstance().videoLikeDic.get(Integer.valueOf(video.video_id));
                if (isLiked == null) {
                    myViewHolder.btnLike.setLiked(video.isFavorite);
                } else {
                    myViewHolder.btnLike.setLiked(isLiked);
                }

                myViewHolder.btnLike.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        likeVideo(true, video, myViewHolder.tvGoals);
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        likeVideo(false, video, myViewHolder.tvGoals);
                    }
                });

            } else if (holder instanceof TagViewHolder) {

            }
        }

        @Override
        public int getItemCount() {
            return mFeeds.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? CARD_TYPE_TAG : CARD_TYPE_FEED;
        }
    }

    /**
     * Horizontal Recycler View Adapter
     */
    public class HorizontalTagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTag;
            public RecyclerView recyclerView;

            public MyViewHolder(View view) {
                super(view);
                tvTag = (TextView) view.findViewById(R.id.tv_tag);
                recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_tag);
            }
        }

        public HorizontalTagAdapter() {
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category_tag_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            MyViewHolder myHolder = (MyViewHolder) holder;
            if (position < mFilterTagArray.size()) {
                Tag tag = mFilterTagArray.get(position);
                myHolder.tvTag.setBackgroundResource(R.drawable.shape_tag_round);
                myHolder.tvTag.setText(tag.tag_name);
            } else { // More Tag
                myHolder.tvTag.setBackgroundResource(R.mipmap.icon_more);
                myHolder.tvTag.setText("      ");
            }

            myHolder.tvTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position < mFilterTagArray.size()) {

                    } else {
                        showTagFilterDlg();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFilterTagArray.size() + 1;
        }
    }




    /**
     * Horizontal Tag List Adapter
     */
    public class SubTagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<String> tagArr;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTag;

            public MyViewHolder(View view) {
                super(view);
                tvTag = (TextView) view.findViewById(R.id.tv_tag);
            }
        }

        public SubTagAdapter(ArrayList<String> tags) {
            this.tagArr = tags;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_trick_tag, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final String tag = tagArr.get(position);
            MyViewHolder myHolder = (MyViewHolder) holder;

            myHolder.tvTag.setText(tag);
            myHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return tagArr.size();
        }
    }

    /**
     * Load Feeds from server
     */
    private void loadFeeds() {
        String endPoint = (isSelectedGlobal == true ? API.GET_FEEDS_GLOBAL : API.GET_FEEDS_FOLLOWER);
        endPoint = endPoint + "&query=" + searchQuery + "&tag_ids=" + getTagIDString();
        mFeeds.clear();
        mFeedListAdapter.notifyDataSetChanged();
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                mPagination = ParseServiceManager.parsePaginationInfo(response);
                mFeeds = ParseServiceManager.parseFeedArrayResponse(response);
                mFeedListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    void loadWithQuery() {
        loadFeeds();
    }
    void loadWithTag() {
        loadFeeds();
        mFeedListAdapter = new FeedListAdapter();
        mRecyclerView.setAdapter(mFeedListAdapter);
    }
    String getTagIDString() {
        if (mFilterTagArray.size() > 0) {
            String str = "";
            for (Tag tag : mFilterTagArray) {
                str = str + String.valueOf(tag.tag_id) + ",";
            }
            String tagIDString = str.substring(0, str.length() - 1);
            return tagIDString;
        } else {
            return "";
        }
    }
    private void loadMore() {
        String endPoint = mPagination.next_page_url;
        if (endPoint == null) {
            return;
        }

        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                mPagination = ParseServiceManager.parsePaginationInfo(response);
                mFeeds = ParseServiceManager.parseFeedArrayResponse(response);
                mFeedListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Share Methods
     */
    private void share(Video video) {
        // Create the share Intent
//        String shareText = "https://dribbler.com/videos?video_id=" + String.valueOf(video.video_id);
        String shareText = "http://52.57.120.88/deeplink?video_id=" + String.valueOf(video.video_id);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    /**
     * Goto Comment Activity
     */
    private void gotoCommentPage(boolean isShowKeyboard, Video video) {
        Intent intent = new Intent(mActivity, CommentActivity.class);
        intent.putExtra("video", video);
        intent.putExtra("isShowKeyboard", isShowKeyboard);
        startActivity(intent);
    }

    /**
     * Goto Video Player Activity
     */
    private void playVideo(String link) {
        Intent intent = new Intent(mActivity, VideoPlayerActivity.class);
        intent.putExtra("link", link);
        startActivity(intent);
    }

    /**
     * Like & Unlike API
     */
    private void likeVideo(final boolean isLike, final Video video, TextView tvGoals) {
        String endPoint = String.format(API.LIKE_VIDEO, video.video_id);

        if (!isLike) {
            endPoint = String.format(API.UNLIKE_VIDEO, video.video_id);
            video.likes--;
            tvGoals.setText(getLikesString(video.likes));
        } else {
            video.likes++;
            tvGoals.setText(getLikesString(video.likes));
        }

        WebServiceManager.postWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Global.sharedInstance().videoLikeDic.put(Integer.valueOf(video.video_id), Boolean.valueOf(isLike));
            }
        });
    }

    /**
     * Helper Methods
     */
    private String getCommentsString(int count) {
        String comment = getResources().getString(R.string.comment);
        if (count == 0) {
            return getString(R.string.empty) + " " + comment;
        } else if (count < 1000) {
            return String.format("%d %s", count, comment);
        } else if (count < 1000000) {
            return String.format("%.2fk %s", ((float)count) / 1000, comment);
        }

        return "";
    }

    private String getLikesString(int count) {
        String comment = getResources().getString(R.string.goals);
        if (count < 1000) {
            return String.format("%d %s", count, comment);
        } else if (count < 1000000) {
            return String.format("+%.2fk %s", ((float)count) / 1000, comment);
        }

        return "";
    }




}