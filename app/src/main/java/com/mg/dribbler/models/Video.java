package com.mg.dribbler.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;


public class Video implements Serializable {
    public int video_id;
    public int user_id;
    public int trick_id;
    public String thumbnail;
    public String ld_url;
    public String hd_url;
    public int likes;
    public int views;
    public int comments;
    public boolean isFavorite;
    public String created_at;

    public User user;


    /** Helper Methods for ArrayList **/

    public static ArrayList<Video> appendVideoArray(ArrayList<Video> originalArr, ArrayList<Video> newArr) {
        ArrayList<Video> joinedArr = new ArrayList<>(originalArr);

        for (Video video : newArr) {
            boolean isExist = false;

            for (int i = 0; i < originalArr.size(); i++) {
                Video temp = originalArr.get(originalArr.size() - i - 1);
                if (temp.video_id == video.video_id) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                joinedArr.add(video);
            }
        }

        return joinedArr;
    }
}
