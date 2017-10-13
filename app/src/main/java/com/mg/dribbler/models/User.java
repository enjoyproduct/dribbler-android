package com.mg.dribbler.models;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Admin on 4/8/2017.
 */

public class User implements Serializable {

    public int userID;
    public boolean isSubscribe;
    public int subscribe;
    public boolean isVerified;
    public boolean isFollowing;
    public String photoURL;
    public String first_name;
    public String last_name;
    public int gender;
    public String birthday;
    public String email;
    public String facebook_id;
    public String google_id;
    public int overall_ranking;
    public int video_count;
    public int trick_completion_count;
    public int follower_count;
    public int following_count;
    public int dribble_score;
    public int dribble_medal;
    public boolean isPushEnable;
    public boolean isHighVideoEnable;
    public boolean isFacebookEnable;
    public boolean isGoogleEnable;

    private static User currentUser;

    /**
     * SingleTone
     */
    public static User currentUser() {
        if (currentUser == null) {
            currentUser = new User();
        }

        return currentUser;
    }

    public static void clearCurrentUser() {
        currentUser = new User();
    }

    public String getFullName() {
        return first_name + " " + last_name;
    }

    /**
     * Helper Methods for appending arrayList without duplication
     */
    public static ArrayList<User> appendUserArray(ArrayList<User> originalArr, ArrayList<User> newArr) {
        ArrayList<User> joinedArr = new ArrayList<>(originalArr);

        for (User user : newArr) {
            boolean isExist = false;

            for (int i = 0; i < originalArr.size(); i++) {
                User temp = originalArr.get(originalArr.size() - i - 1);
                if (temp.userID == user.userID) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                joinedArr.add(user);
            }
        }

        return joinedArr;
    }
}
