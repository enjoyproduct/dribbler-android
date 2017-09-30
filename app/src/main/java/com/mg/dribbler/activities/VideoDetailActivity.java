package com.mg.dribbler.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.github.ybq.android.spinkit.SpinKitView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.ConnectionUtil;
import com.mg.dribbler.utils.TimeAgo;
import com.mg.dribbler.utils.TimeUtility;
import com.mg.dribbler.utils.UIUtil;
import com.mg.dribbler.views.MyCircularImageView;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class VideoDetailActivity extends Activity {

    private SpinKitView loader;
    private RelativeLayout rvGoals;
    private LinearLayout llPanel;
    private TextView tvGoals;
    private TextView tvComment;
    LikeButton likeBtn;

    private String mLink;
    private Video video;
    private String userName;
    private String userPhotoURL;

    private MyCircularImageView mAvatarIV;
    private TextView mUserNameTV;
    private TextView mTimeAgoTV;


    /**
     * Life Cycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        // Get parameters from Intent
        Intent intent = getIntent();
        video = (Video) intent.getSerializableExtra("video");
        userName = intent.getStringExtra("username");
        userPhotoURL = intent.getStringExtra("photo");

        // Load Video
        loadVideo();

        // Avatar, Username, Time, loader
        mAvatarIV = (MyCircularImageView) findViewById(R.id.iv_avatar);
        mUserNameTV = (TextView) findViewById(R.id.tv_username);
        mTimeAgoTV = (TextView) findViewById(R.id.tv_time);
        Glide.with(this).load(userPhotoURL).into(mAvatarIV);
        mUserNameTV.setText(userName);
        String ago = new TimeAgo().timeAgo(TimeUtility.getLocalDateFromUTCString(video.created_at));
        mTimeAgoTV.setText(ago);
        loader = (SpinKitView) findViewById(R.id.spin);

        // timer for updating time ago
        final Handler handler = new Handler();
        Timer timer = new Timer(false);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String ago = new TimeAgo().timeAgo(TimeUtility.getLocalDateFromUTCString(video.created_at));
                        mTimeAgoTV.setText(ago);
                    }
                });
            }
        };
        timer.schedule(timerTask, 60000);

        // Instantiate Video Information Panel
        llPanel = (LinearLayout) findViewById(R.id.ll_panel);
        llPanel.setVisibility(View.GONE);

        rvGoals = (RelativeLayout) findViewById(R.id.rl_goals);
        tvGoals = (TextView) findViewById(R.id.tv_goals);
        tvComment = (TextView) findViewById(R.id.tv_comment);
        tvGoals.setText(getLikesString(video.likes));
        tvComment.setText(getCommentsString(video.comments));

        // Init Video View
        final VideoView videoView = (VideoView) findViewById(R.id.video_player);
        // Play Video
        mLink = video.ld_url;
        if (User.currentUser().isHighVideoEnable) {
            if (ConnectionUtil.isWifiEnabled(this)) {
                mLink = video.hd_url;
            }
        }
        try {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            Uri video = Uri.parse(mLink);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);
            videoView.requestFocus();
            videoView.setZOrderOnTop(true);
        } catch (Exception e) {
            UIUtil.showAlertDialog(this, "", "Cannot open video", "OK");
        }
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                loader.setVisibility(View.GONE);
                videoView.start();
                viewVideo();
            }
        });

        // Like Button
        likeBtn = (LikeButton) findViewById(R.id.btn_rate);
        Boolean isLiked = Global.sharedInstance().videoLikeDic.get(Integer.valueOf(video.video_id));
        if (isLiked == null) {
            likeBtn.setLiked(video.isFavorite);
        } else {
            likeBtn.setLiked(isLiked);
        }

        likeBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                likeVideo(true);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                likeVideo(false);
            }
        });

        // Goto Comment Activity
        findViewById(R.id.iv_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            gotoCommentPage(true);
            }
        });

        TextView tvComment = (TextView) findViewById(R.id.tv_comment);
        tvComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCommentPage(false);
            }
        });

        // Share
        findViewById(R.id.iv_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        llPanel.setVisibility(View.GONE);
        loadVideo();
    }

    private void refreshPanel() {
        tvGoals.setText(getLikesString(video.likes));
        tvComment.setText(getCommentsString(video.comments));
        likeBtn.setLiked(video.isFavorite);
        llPanel.setVisibility(View.VISIBLE);
    }

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

    /**
     * Navigate Method
     */
    private void gotoCommentPage(boolean isShowKeyboard) {
        Intent intent = new Intent(VideoDetailActivity.this, CommentActivity.class);
        intent.putExtra("video", video);
        intent.putExtra("isShowKeyboard", isShowKeyboard);
        startActivity(intent);
    }

    /**
     * Share Methods
     */
    private void share() {
        // Create the share Intent
        String shareText = "";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, video.hd_url);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    /**
     * Load Video Information
     */
    private void loadVideo() {
        String endPoint = String.format(API.GET_A_VIDEO, video.video_id);
        WebServiceManager.getWithToken(this, endPoint, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                video = ParseServiceManager.parseVideoResponse(response);
                refreshPanel();
            }
        });
    }

    /**
     * Like & Unlike API
     */
    private void likeVideo(final boolean isLike) {
        String endPoint = String.format(API.LIKE_VIDEO, video.video_id);

        if (!isLike) {
            endPoint = String.format(API.UNLIKE_VIDEO, video.video_id);
            video.likes--;
            tvGoals.setText(getLikesString(video.likes));
        } else {
            video.likes++;
            tvGoals.setText(getLikesString(video.likes));
        }

        WebServiceManager.postWithToken(this, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Global.sharedInstance().videoLikeDic.put(Integer.valueOf(video.video_id), Boolean.valueOf(isLike));
            }
        });
    }

    /**
     * View Video API
     */
    private void viewVideo() {
        String endPoint = String.format(API.VIEW_VIDEO, video.video_id);
        WebServiceManager.postWithToken(this, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
            }
        });
    }
}
