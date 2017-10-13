package com.mg.dribbler.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.fragments.VideoDetailFragment;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.UIUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by KCB on 5/2/2017.
 */

public class APIServiceManager {

    /**
     * get tag list
     */
    public static void getTags(final Context context) {
        WebServiceManager.getWithToken(context, API.GET_TAGS, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Global.sharedInstance().tagArr = ParseServiceManager.parseTagResponse(response);
                sendNotification(context, AppConstant.BROADCAST_GET_TAGS);
            }
        });
    }


    /**
     * get category list
     */
    public static void unlock_category(final Context context, int category_id) {
        String endPoint = String.format(API.UNLOCK_CATEGORY, category_id);
        WebServiceManager.getWithToken(context, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
            }
        });
    }

    /**
     * Post Try_on
     */
    public static void postDribbler(final Context context, RequestParams params) {
        WebServiceManager.postWithToken(context, API.POST_DRIBBLER, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                VideoDetailFragment.myTrickDribblers = ParseServiceManager.parseDribblerResponse(response);
                UIUtil.showLongToast(context, "Your progress have been saved");
                sendNotification(context, AppConstant.BROADCAST_GET_TRICK_STATISTICS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    /**
     * Get My Videos
     */
    public static void getMyVideos(final Context context) {
        String endPoint = String.format(API.GET_VIDEO, User.currentUser().userID);
        WebServiceManager.getWithToken(context, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Global.sharedInstance().myVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                ArrayList<Video> videos = ParseServiceManager.parseVideoArrayResponse(response);
                Global.sharedInstance().appendMyVideo(videos);
                sendNotification(context, AppConstant.BROADCAST_GET_MY_VIDEOS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    /**
     * Get Profile Status
     */
    public static void getProfileStatus(Context context, final JsonHttpResponseHandler handler) {
        String endPoint = String.format(API.GET_PROFILE_STATUS, User.currentUser().userID);
        WebServiceManager.getWithToken(context, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
//                    Global.sharedInstance().myCategoryScore = ParseServiceManager.parseScoreResponse(response.getJSONArray("category"), "category");
//                    Global.sharedInstance().myTagScore = ParseServiceManager.parseScoreResponse(response.getJSONArray("tag"), "tag");
//                    Global.sharedInstance().myTrickScore = ParseServiceManager.parseScoreResponse(response.getJSONArray("trick"), "trick");
                } catch (Exception exception) {
                    Log.e("Parse Error", "Profile Status");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    /**
     * Send Broadcast event
     */
    private static void sendNotification(Context context, String event) {
        Intent intent = new Intent(event);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
