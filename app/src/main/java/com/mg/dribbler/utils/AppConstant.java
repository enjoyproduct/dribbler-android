package com.mg.dribbler.utils;

import android.os.Environment;
import java.io.File;

public class AppConstant {

    /**
     * Broadcast Event
     */
    public static final String BROADCAST_GET_TAGS = "BROADCAST_GET_TAGS";
    public static final String BROADCAST_GET_MY_VIDEOS = "BROADCAST_GET_MY_VIDEOS";
    public static final String BROADCAST_GET_OTHER_VIDEOS = "BROADCAST_GET_OTHER_VIDEOS";
    public static final String BROADCAST_GET_MY_PROFILE_STATISTICS = "BROADCAST_GET_MY_PROFILE_STATISTICS";
    public static final String BROADCAST_GET_OTHER_PROFILE_STATISTICS = "BROADCAST_GET_OTHER_PROFILE_STATISTICS";
    public static final String BROADCAST_GET_TRICK_STATISTICS = "BROADCAST_GET_TRICK_STATISTICS";
    public static final String BROADCAST_GET_TRICK_MY_VIDEOS = "BROADCAST_GET_TRICK_MY_VIDEOS";
    public static final String BROADCAST_GET_TRICK_OTHER_VIDEOS = "BROADCAST_GET_TRICK_OTHER_VIDEOS";
    public static final String BROADCAST_GET_TRICK_BEST_USERS = "BROADCAST_GET_TRICK_BEST_USERS";


    /**
     * shared preferences
     */
    public static final String PREF_IS_SHOW_TUTORIALS = "PREF_LOGIN_STATUS";
    public static final String PREF_LOGIN_TYPE = "PREF_LOGIN_TYPE";
    public static final String PREF_FACEBOOK_ID = "PREF_FACEBOOK_ID";
    public static final String PREF_GOOGLE_ID = "PREF_GOOGLE_ID";
    public static final String PREF_CREDENTIAL_EMAIL = "PREF_CREDENTIAL_EMAIL";
    public static final String PREF_CREDENTIAL_PASSWORD = "PREF_CREDENTIAL_PASSWORD";


    /**
     * Pagination Size
     */
    public static final int PAGE_SIZE_COMMENTS = 10;


    /**
     * File Utility
     */
    public static final String DIR_LOCAL_IMAGE = Environment.getExternalStorageDirectory() + File.separator + "OneDollar";
    public static final String DIR_LOCAL_IMAGE_TEMP = DIR_LOCAL_IMAGE + File.separator + "temp";
    public static final int IMAGE_MAX_SIZE = 851;
}
