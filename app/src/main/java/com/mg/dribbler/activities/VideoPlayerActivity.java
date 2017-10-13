package com.mg.dribbler.activities;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.mg.dribbler.R;


public class VideoPlayerActivity extends AppCompatActivity {

    private SpinKitView mProgressBar;
    private String mLink;

    /**
     * Life Cycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mProgressBar = (SpinKitView) findViewById(R.id.spin);

        Intent intent = getIntent();
        mLink = intent.getStringExtra("link");
        VideoView videoView = (VideoView) findViewById(R.id.video_player);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mProgressBar.setVisibility(View.GONE);
            }
        });

        try {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            Uri video = Uri.parse(mLink);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);
            videoView.start();
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(this, "Cannot open video", Toast.LENGTH_SHORT).show();
        }
    }
}
