package com.mg.dribbler.services;

import android.util.Log;

import com.mg.dribbler.models.Achievement;
import com.mg.dribbler.models.Category;
import com.mg.dribbler.models.Comment;
import com.mg.dribbler.models.Dribbler;
import com.mg.dribbler.models.Pagination;
import com.mg.dribbler.models.Score;
import com.mg.dribbler.models.Tag;
import com.mg.dribbler.models.Trick;
import com.mg.dribbler.models.TrickDescription;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Admin on 4/9/2017.
 */

public class ParseServiceManager {

    /**
     * Parse User Response
     */
    public static void parseUserResponse(JSONObject jsonObject) {
        try {
            User me = User.currentUser();
            me.userID = jsonObject.getInt("id");
            me.first_name = jsonObject.getString("first_name");
            me.last_name = jsonObject.getString("last_name");
            me.email = jsonObject.getString("email");
            me.gender = jsonObject.getInt("gender");
            me.birthday = jsonObject.getString("birthday");
            me.subscribe = jsonObject.getInt("subscribe");
            me.isVerified = (0 == jsonObject.getInt("verified") ? false : true);
            me.dribble_score = jsonObject.getInt("dribble_score");
            me.overall_ranking = jsonObject.getInt("overall_ranking");
            me.trick_completion_count = jsonObject.getInt("trick_completion_count");
            me.video_count = jsonObject.getInt("video_count");
            me.follower_count = jsonObject.getInt("follower_count");
            me.following_count = jsonObject.getInt("following_count");
            me.facebook_id = jsonObject.getString("facebook_id");
            me.google_id = jsonObject.getString("google_id");
            me.isPushEnable = (0 == jsonObject.getInt("push_enable") ? false : true);
            me.isHighVideoEnable = (0 == jsonObject.getInt("high_video_enable") ? false : true);
            me.isFacebookEnable = (0 == jsonObject.getInt("fb_enable") ? false : true);
            me.isGoogleEnable = (0 == jsonObject.getInt("google_enable") ? false : true);
            me.photoURL = jsonObject.getString("photo");
            me.dribble_medal = jsonObject.getInt("dribble_medal");
            if (jsonObject.has("token")) {
                WebServiceManager.token = jsonObject.getString("token");
            }
        } catch (Exception exception) {
            Log.e("Parse Exception", "Parse UserResponse");
        }
    }
    public static User parseOtherUserResponse(JSONObject jsonObject) {
        try {
            User user = new User();
            user.userID = jsonObject.getInt("id");
            user.first_name = jsonObject.getString("first_name");
            user.last_name = jsonObject.getString("last_name");
            user.email = jsonObject.getString("email");
            user.gender = jsonObject.getInt("gender");
            user.birthday = jsonObject.getString("birthday");
            user.subscribe = jsonObject.getInt("subscribe");
            user.isVerified = (0 == jsonObject.getInt("verified") ? false : true);
            user.dribble_score = jsonObject.getInt("dribble_score");
            user.overall_ranking = jsonObject.getInt("overall_ranking");
            user.trick_completion_count = jsonObject.getInt("trick_completion_count");
            user.video_count = jsonObject.getInt("video_count");
            user.follower_count = jsonObject.getInt("follower_count");
            user.following_count = jsonObject.getInt("following_count");
            user.facebook_id = jsonObject.getString("facebook_id");
            user.google_id = jsonObject.getString("google_id");
            user.isPushEnable = (0 == jsonObject.getInt("push_enable") ? false : true);
            user.isHighVideoEnable = (0 == jsonObject.getInt("high_video_enable") ? false : true);
            user.isFacebookEnable = (0 == jsonObject.getInt("fb_enable") ? false : true);
            user.isGoogleEnable = (0 == jsonObject.getInt("google_enable") ? false : true);
            user.photoURL = jsonObject.getString("photo");
            user.dribble_medal = jsonObject.getInt("dribble_medal");

            return user;
        } catch (Exception exception) {
            Log.e("Parse Exception", "Parse UserResponse");
        }
        return  null;
    }
    /**
     * Parse Pagination
     */
    public static Pagination parsePaginationInfo(JSONObject response) {
        Pagination pagination = new Pagination();

        try {
            pagination.total = response.getInt("total");
            pagination.per_page = response.getInt("per_page");
            if (!response.isNull("next_page_url")) {
                pagination.next_page_url = response.getString("next_page_url");
            }
            if (!response.isNull("prev_page_url")) {
                pagination.prev_page_url = response.getString("prev_page_url");
            }
            pagination.current_page = response.getInt("current_page");
            pagination.from = response.getInt("from");
            pagination.to = response.getInt("to");
        } catch (Exception exception) {
            Log.e("Parse Error", "pagination");
        }

        return pagination;
    }

    /**
     * Category Info
     */
    public static ArrayList<Category> parseCategoryResponse(JSONArray response) {
        ArrayList<Category> categories = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = (JSONObject) response.get(i);
                Category category = new Category();
                category.category_id = obj.getInt("category_id");
                category.category_title = obj.getString("category_title");
                category.thumbnail_url = obj.getString("thumbnail");
                category.price = obj.getDouble("price");
                category.premium = (0 != obj.getInt("lock"));
                category.unlocked = (0 != obj.getInt("unlocked"));

                categories.add(category);

            } catch (Exception exception) {
                Log.e("Parse Error", "Categories");
            }
        }

        return categories;
    }
    /**
     * Trick Info
     */
    public static ArrayList<Trick> parseTrickResponse(JSONArray trickArr) {
        ArrayList<Trick> tricks = new ArrayList<>();

        for (int j = 0; j < trickArr.length(); j++) {
            JSONObject trickObj = null;
            try {
                trickObj = (JSONObject) trickArr.get(j);
                Trick trick = new Trick();
                trick.trick_id = trickObj.getInt("trick_id");
                trick.category_id = trickObj.getInt("category_id");
                trick.trick_title = trickObj.getString("trick_title");
                trick.trick_tags = trickObj.getString("trick_tags");
                trick.tagArr = new ArrayList<>(Arrays.asList(trickObj.getString("trick_tags").split("\\s*,\\s*")));
                trick.thumbnail_url = trickObj.getString("trick_thumbnail");
                trick.ld_video_url = trickObj.getString("ld_video_url");
                trick.hd_video_url = trickObj.getString("hd_video_url");

                String jsonStr = trickObj.getString("trick_description");
                JSONArray descriptionArr = new JSONArray(jsonStr);
                for (int k = 0; k < descriptionArr.length(); k++) {
                    JSONObject descriptionObj = (JSONObject) descriptionArr.get(k);
                    TrickDescription description = new TrickDescription();
                    description.title = descriptionObj.getString("title");
                    description.thumbnail = descriptionObj.getString("thumbnail");
                    description.description = descriptionObj.getString("description");
                    trick.descriptions.add(description);
                }

                tricks.add(trick);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return tricks;
    }
    /**
     * Parse Tag
     */
    public static ArrayList<Tag> parseTagResponse(JSONArray response) {
        ArrayList<Tag> tagArr = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = (JSONObject) response.get(i);
                Tag tag = new Tag();
                tag.tag_id = obj.getInt("tag_id");
                tag.tag_name = obj.getString("tag_name");

                tagArr.add(tag);
            } catch (Exception exception) {
                Log.e("Parse Error", "Tags");
            }
        }

        return tagArr;
    }

    /**
     * Parse Feed
     */
    public static ArrayList<Video> parseFeedArrayResponse(JSONObject response) {
        ArrayList<Video> videos = new ArrayList<>();

        try {
            JSONArray array = response.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Video video = parseFeedResponse(obj);
                videos.add(video);
            }

        } catch (Exception exception) {
            Log.e("Parse Error", "Video");
        }

        return videos;
    }
    public static Video parseFeedResponse(JSONObject obj) {
        Video video = new Video();
        try {
            video.video_id = obj.getInt("video_id");
            video.user_id = obj.getInt("user_id");
            video.trick_id = obj.getInt("trick_id");
            video.thumbnail = obj.getString("thumbnail");
            video.hd_url = obj.getString("hd_url");
            video.ld_url = obj.getString("ld_url");
            video.created_at = obj.getString("created_at");
            video.likes = obj.getInt("likes");
            video.views = obj.getInt("views");
            video.comments = obj.getInt("comments");
            video.isFavorite = (0 != obj.getInt("favorite"));

            User user = new User();
            user.first_name =  obj.getString("first_name");
            user.last_name =  obj.getString("last_name");
            user.photoURL =  obj.getString("photo");
            user.userID = obj.getInt("user_id");
            video.user = user;

        } catch (Exception exception) {
            Log.e("Parse Error", "Video");
        }

        return video;
    }
    /**
     * Parse Video
     */
    public static ArrayList<Video> parseVideoArrayResponse(JSONObject response) {
        ArrayList<Video> videos = new ArrayList<>();

        try {
            JSONArray array = response.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Video video = parseVideoResponse(obj);
                videos.add(video);
            }

        } catch (Exception exception) {
            Log.e("Parse Error", "Video");
        }

        return videos;
    }


    public static Video parseVideoResponse(JSONObject obj) {
        Video video = new Video();
        try {
            video.video_id = obj.getInt("video_id");
            video.user_id = obj.getInt("user_id");
            video.trick_id = obj.getInt("trick_id");
            video.thumbnail = obj.getString("thumbnail");
            video.hd_url = obj.getString("hd_url");
            video.ld_url = obj.getString("ld_url");
            video.created_at = obj.getString("created_at");
            video.likes = obj.getInt("likes");
            video.views = obj.getInt("views");
            video.comments = obj.getInt("comments");
            video.isFavorite = (0 != obj.getInt("favorite"));

            if (!obj.isNull("user")) {
                User user = new User();
                JSONObject userObj = obj.getJSONObject("user");
                user.first_name =  userObj.getString("first_name");
                user.last_name =  userObj.getString("last_name");
                user.photoURL =  userObj.getString("photo");
                user.userID = userObj.getInt("id");
                video.user = user;
            }

        } catch (Exception exception) {
            Log.e("Parse Error", "Video");
        }

        return video;
    }

    /**
     * Parse other Users
     */
    public static ArrayList<User> parseOtherUserResposne(JSONObject response) {
        ArrayList<User> users = new ArrayList<>();

        try {
            JSONArray array = response.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                User user = new User();
                user.userID = obj.getInt("id");
                user.first_name = obj.getString("first_name");
                user.last_name = obj.getString("last_name");
                user.photoURL = obj.getString("photo");
                if (obj.has("isFollowing")) {
                    user.isFollowing = (0 != obj.getInt("isFollowing"));
                }
                if (obj.has("follow_status")) {
                    int status = obj.getInt("follow_status");
                    user.isFollowing = (status != 0);
                }
                if (obj.has("following_count")) {
                    user.following_count = obj.getInt("following_count");
                }
                if (obj.has("follower_count")) {
                    user.follower_count = obj.getInt("follower_count");
                }
                if (obj.has("overall_ranking")) {
                    user.overall_ranking = obj.getInt("overall_ranking");
                }
                if (obj.has("video_count")) {
                    user.video_count = obj.getInt("video_count");
                }
                if (obj.has("dribble_score")) {
                    user.dribble_score = obj.getInt("dribble_score");
                }
                if (obj.has("dribble_medal")) {
                    user.dribble_medal = obj.getInt("dribble_medal");
                }
                if (obj.has("trick_completion_count")) {
                    user.trick_completion_count = obj.getInt("trick_completion_count");
                }
                users.add(user);
            }
        } catch (Exception exception) {
            Log.e("Parse Error", "Another Users");
        }

        return users;
    }

    /**
     * Parse Dribbler
     */
    public static ArrayList<Dribbler> parseDribblerResponse(JSONArray response) {
        ArrayList<Dribbler> dribblers = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Dribbler dribbler = new Dribbler();
                dribbler.trick_id = obj.getInt("trick_id");
                dribbler.dribbler_id = obj.getInt("dribbler_id");
                dribbler.try_on = obj.getInt("try_on");

                dribblers.add(dribbler);
            } catch (Exception exception) {
                Log.e("Parse Error", "Another Users");
            }
        }

        return dribblers;
    }

    /**
     * Parse Tag Score
     */
    public static ArrayList<Score> parseScoreResponse(JSONArray response, String type) {
        ArrayList<Score> scores = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Score score = new Score();

                if (type == "category") {
                    score.id = obj.getInt("category_id");
                    score.title = obj.getString("category_title");
                } else if (type == "tag") {
                    score.id = obj.getInt("tag_id");
                    score.title = obj.getString("tag_name");
                } else if (type == "trick") {
                    score.id = obj.getInt("trick_id");
                    score.title = obj.getString("trick_title");
                }

                if (obj.isNull("score")) {
                    score.score = 0.0;
                } else {
                    score.score = obj.getDouble("score");
                }

                scores.add(score);
            } catch (Exception exception) {

            }
        }

        return scores;
    }
    public static ArrayList<Achievement> parseAchievement(JSONArray response) {
        ArrayList<Achievement> achievements = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Achievement achievement = new Achievement();
                achievement.achievement = obj.getInt("achievement");
                achievement.score = obj.getDouble("average");
                JSONObject trick = obj.getJSONObject("trick");
                achievement.title = trick.getString("trick_title");

                achievements.add(achievement);
            } catch (Exception exception) {

            }
        }

        return achievements;
    }
    public static ArrayList<Score> parseTagStatistic(JSONArray response) {
        ArrayList<Score> arrayList = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Score score = new Score();

                score.score = obj.getDouble("average");
                score.title = obj.getJSONObject("tag").getString("tag_name");
                score.id = obj.getJSONObject("tag").getInt("tag_id");
                arrayList.add(score);
            } catch (Exception exception) {

            }
        }
        return arrayList;
    }
    public static ArrayList<Score> parseCategoryStatistic(JSONArray response) {
        ArrayList<Score> arrayList = new ArrayList<>();
        for (int i = 0; i < response.length(); i ++) {
            try {
                JSONObject object = response.getJSONObject(i);
                Score score = new Score();
                score.title = object.getString("title");
                score.score = object.getDouble("score");
                arrayList.add(score);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }
    /**
     * Parse Comments
     */
    public static ArrayList<Comment> parseCommentResponse(JSONArray response) {
        ArrayList<Comment> comments = new ArrayList<>();

        return comments;
    }

}
