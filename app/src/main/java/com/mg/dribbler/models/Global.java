package com.mg.dribbler.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dell17 on 5/16/2016.
 */
public class Global {

    public ArrayList<Tag> tagArr = new ArrayList<>();

    // Selected Trick
    public Trick selectedTrick;

    // My Profile
    public ArrayList<Video> myVideos = new ArrayList<>();
    public Pagination myVideosPagination;
//    public ArrayList<Score> myCategoryScore = new ArrayList<>();
//    public ArrayList<Score> myTrickScore = new ArrayList<>();
//    public ArrayList<Score> myTagScore = new ArrayList<>();

    // Feed and Follower
    public ArrayList<User> followers = new ArrayList<>();

    // Like List
    public HashMap<Integer, Boolean> videoLikeDic = new HashMap<>();


    // Singleton
    static Global instance;

    /**
     * Life Cycle
     */
    private Global() {
    }

    public static Global sharedInstance() {
        if (instance == null) {
            instance = new Global();
        }

        return instance;
    }
    public static void init() {
        instance = null;
    }
    public void appendMyVideo(ArrayList<Video> videos) {
        for (Video video : videos) {
            boolean isExist = false;

            for (Video tempVideo : myVideos) {
                if (video.video_id == tempVideo.video_id) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                myVideos.add(video);
            }
        }
    }
}
