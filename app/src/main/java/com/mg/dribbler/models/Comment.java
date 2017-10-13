package com.mg.dribbler.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Admin on 5/13/2017.
 */

public class Comment implements Serializable {
    public int comment_id;
    public int commentator_id;
    public int video_id;
    public String message;
    public int likes;
    public int replies;
    public String created_at;
    public String updated_at;

    public User user;

    public static ArrayList<Comment> append(ArrayList<Comment> originalArr, ArrayList<Comment> newArr) {
        ArrayList<Comment> joinedArr = new ArrayList<>(originalArr);

        for (Comment comment : newArr) {
            boolean isExist = false;

            for (int i = 0; i < originalArr.size(); i++) {
                Comment temp = originalArr.get(originalArr.size() - i - 1);
                if (temp.comment_id == comment.comment_id) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                joinedArr.add(comment);
            }
        }

        return joinedArr;
    }
}
