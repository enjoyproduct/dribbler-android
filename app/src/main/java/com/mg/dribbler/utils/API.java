package com.mg.dribbler.utils;

public class API {

//    public static String BASE_URL = "http://192.168.3.200/Dribbler/public/api/v1";
    public static String BASE_URL = "http://52.57.120.88/api/v1";

    public static String LOGIN = BASE_URL + "/users/me";
    public static String REGISTER = BASE_URL + "/users/register";
    public static String VERIFY = BASE_URL + "/users/verify";
    public static String FORGOT_PASSWORD = BASE_URL + "/password/forgot";
    public static String GET_TAGS = BASE_URL + "/tags";
    public static String GET_CATEGORIES = BASE_URL + "/categories";
    public static String UNLOCK_CATEGORY = BASE_URL + "/%d/unlock_category";
    public static String GET_OTHER_PROFILE = BASE_URL + "/users/%d/profile";
    public static String GET_TRICK_STATISTICS = BASE_URL + "/tricks/%d/statistics";
    public static String GET_TRICK_USERS = BASE_URL + "/tricks/%d/users?per_page=60";
    public static String GET_TRICK_BY_CATEGORY = BASE_URL + "/tricks/%d/get_tricks_by_category";
    public static String GET_TRICK_MY_VIDEOS = BASE_URL + "/tricks/%d/videos?per_page=60&type=me";
    public static String GET_TRICK_OTHER_VIDEOS = BASE_URL + "/tricks/%d/videos?per_page=60&type=other";
    public static String POST_DRIBBLER = BASE_URL + "/dribblers";
    public static String POST_VIDEO = BASE_URL + "/videos";
    public static String GET_VIDEO = BASE_URL + "/videos?user_id=%d&per_page=60";
    public static String GET_A_VIDEO = BASE_URL + "/videos/%d";
    public static String GET_PROFILE_STATUS = BASE_URL + "/users/%d/status";
    public static String GET_PROFILE_ACHIEVEMENTS = BASE_URL + "/users/%d/get_achievements";
    public static String GET_PROFILE_STATISTIC = BASE_URL + "/users/%d/get_profile_statistic";
    public static String GET_COMMENTS = BASE_URL + "/videos/%d/comments?per_page=%d";
    public static String POST_COMMENT = BASE_URL + "/videos/%d/comments";
    public static String LIKE_VIDEO = BASE_URL + "/videos/%d/like?type=1";
    public static String VIEW_VIDEO = BASE_URL + "/videos/%d/view";
    public static String UNLIKE_VIDEO = BASE_URL + "/videos/%d/like?type=0";
    public static String FOLLOW_USER = BASE_URL + "/users/%d/follower";
    public static String GET_VIDEO_FOLLOWERS = BASE_URL + "/videos/%d/followers";
    public static String GET_FOLLOWER_LIST = BASE_URL + "/users/%d/followers?per_page=100";
    public static String GET_FOLLOWING_LIST = BASE_URL + "/users/%d/followings?per_page=100";
    public static String GET_USERS = BASE_URL + "/users/%d/users?per_page=100";
    public static String UPDATE_MY_PROFILE = BASE_URL + "/users/me/profile";
    public static String GET_FEEDS_GLOBAL = BASE_URL + "/feeds/global?page=1";
    public static String GET_FEEDS_FOLLOWER = BASE_URL + "/feeds/follower?page=1";

}
