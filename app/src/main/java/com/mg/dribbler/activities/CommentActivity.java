package com.mg.dribbler.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.interfaces.OnLoadMoreListener;
import com.mg.dribbler.models.Comment;
import com.mg.dribbler.models.Pagination;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.TimeAgo;
import com.mg.dribbler.utils.TimeUtility;
import com.mg.dribbler.utils.UIUtil;
import com.mg.dribbler.views.MyCircularImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class CommentActivity extends AppCompatActivity {

    /* Comment List */
    private RecyclerView mRecyclerView;
    private CommentActivity.CommentListAdapter mRecyclerAdapter;

    private Video mVideo;
    private ArrayList<Comment> mComments = new ArrayList<>();

    private ImageButton btnSend;
    private EditText etComment;
    private ProgressBar progressBar;

    private Pagination pagination;


    /**
     * Life Cycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        mVideo = (Video) getIntent().getSerializableExtra("video");
        boolean isShowKeyboard = getIntent().getBooleanExtra("isShowKeyboard", true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        loadComments();

        etComment = (EditText) findViewById(R.id.et_comment);
        if (isShowKeyboard) {
            etComment.requestFocus();
        } else {
            etComment.clearFocus();
        }
        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    btnSend.setEnabled(false);
                } else {
                    btnSend.setEnabled(true);
                }
            }
        });
        btnSend = (ImageButton) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(etComment.getText().toString());
                etComment.setText("");
            }
        });

        // Initialize RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerAdapter = new CommentListAdapter();
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMoreComments();
            }
        });
    }

    /**
     * Post new comment
     */
    private void postComment(String message) {
        RequestParams params = new RequestParams();
        params.put("commentator_id", User.currentUser().userID);
        params.put("message", message);

        String endPoint = String.format(API.POST_COMMENT, mVideo.video_id);
        WebServiceManager.postWithToken(this, endPoint, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Comment comment = parseComment(response);
                mComments.add(0, comment);
                mRecyclerAdapter.notifyItemInserted(0);
                mRecyclerView.scrollToPosition(0);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    /**
     * load comments from server
     */
    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);

        String endPoint = String.format(API.GET_COMMENTS, mVideo.video_id, AppConstant.PAGE_SIZE_COMMENTS);
        WebServiceManager.getWithToken(this, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    pagination = ParseServiceManager.parsePaginationInfo(response);
                    mComments = parseComments(response.getJSONArray("data"));
                } catch (Exception exception) {
                    Log.e("Parse Error", "comments");
                }
                mRecyclerAdapter.notifyDataSetChanged();
                mRecyclerAdapter.setLoaded();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadMoreComments() {
        if (pagination.next_page_url == null) {
            return;
        }

        String endPoint = pagination.next_page_url;

        // Insert Load More.
        mComments.add(null);
        mRecyclerAdapter.notifyItemInserted(mComments.size() - 1);

        WebServiceManager.getWithToken(this, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    pagination = ParseServiceManager.parsePaginationInfo(response);
                    ArrayList<Comment> comments = parseComments(response.getJSONArray("data"));
                    mComments = Comment.append(mComments, comments);
                } catch (Exception exception) {
                    Log.e("Parse Error", "comments");
                }
                mRecyclerAdapter.notifyDataSetChanged();
                mRecyclerAdapter.setLoaded();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                //Remove loading item
                mComments.remove(mComments.size() - 1);
                mRecyclerAdapter.notifyItemRemoved(mComments.size());
            }
        });
    }

    /**
     * Parse Methods
     */
    private ArrayList<Comment> parseComments(JSONArray jsonArray) {
        ArrayList<Comment> comments = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                Comment comment = parseComment(obj);
                comments.add(comment);
            } catch (Exception exception) {
                Log.e("Parse Error", "Comment");
            }
        }

        return comments;
    }

    private Comment parseComment(JSONObject obj) {
        Comment comment = new Comment();
        try {
            comment.created_at = obj.getString("created_at");
            comment.comment_id = obj.getInt("comment_id");
            comment.commentator_id = obj.getInt("commentator_id");
            comment.video_id = obj.getInt("video_id");
            comment.message = obj.getString("message");
            comment.likes = obj.getInt("likes");
            comment.replies = obj.getInt("replies");
            comment.updated_at = obj.getString("updated_at");

            User user = new User();
            JSONObject userObj = obj.getJSONObject("commentator");
            user.userID = userObj.getInt("id");
            user.photoURL = userObj.getString("photo");
            user.first_name = userObj.getString("first_name");
            user.last_name = userObj.getString("last_name");

            comment.user = user;
        } catch (Exception exception) {
            Log.e("Parse Error", "Comment");
        }

        return comment;
    }

    /**
     * Comment Recycler View Adapter
     */
    public class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;

        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading;
        private int visibleThreshold = 1;
        private int lastVisibleItem, totalItemCount;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public MyCircularImageView ivAvatar;
            public TextView tvUserName;
            public TextView tvTime;
            public TextView tvMessage;

            public MyViewHolder(View view) {
                super(view);
                ivAvatar = (MyCircularImageView) view.findViewById(R.id.iv_avatar);
                tvUserName = (TextView) view.findViewById(R.id.tv_username);
                tvTime = (TextView) view.findViewById(R.id.tv_time);
                tvMessage = (TextView) view.findViewById(R.id.tv_message);
            }
        }

        public CommentListAdapter() {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

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
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(CommentActivity.this).inflate(R.layout.row_comment, parent, false);
                return new CommentActivity.CommentListAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View itemView = LayoutInflater.from(CommentActivity.this).inflate(R.layout.reuse_loading_item, parent, false);
                return new LoadingViewHolder(itemView);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final Comment comment = mComments.get(position);
                final MyViewHolder myViewHolder = (MyViewHolder) holder;

                Glide.with(CommentActivity.this).load(comment.user.photoURL).into(myViewHolder.ivAvatar);
                myViewHolder.tvUserName.setText(comment.user.first_name + " " + comment.user.last_name);
                String ago = new TimeAgo().timeAgo(TimeUtility.getLocalDateFromUTCString(comment.created_at));
                myViewHolder.tvTime.setText(ago);
                myViewHolder.tvMessage.setText(comment.message);

            } else if (holder instanceof LoadingViewHolder) { // Loading View
                final LoadingViewHolder viewHolder = (LoadingViewHolder) holder;
                viewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mComments.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }

    // Loading More ViewHolder
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }
}
