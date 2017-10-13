package com.mg.dribbler.services;


import android.app.Application;
import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.mg.dribbler.application.DribblerApplication;
import com.mg.dribbler.utils.ConnectionUtil;
import com.mg.dribbler.utils.ErrorUtil;

import org.json.JSONObject;

import cz.msebera.android.httpclient.HttpEntity;

public class WebServiceManager {

    protected static final int WS_TIME_OUT = 30000;
    public static String token = "";

    private static AsyncHttpClient mClient = new AsyncHttpClient();

    static {
        mClient.addHeader(AsyncHttpClient.HEADER_CONTENT_TYPE, "application/json");
        mClient.setTimeout(WS_TIME_OUT);
    }

    /* Get Mode */

    public static void get(Context context, String url, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.get(url, responseHandler);
    }

    public static void getWithToken(Context context, String url, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.addHeader("Authorization", "Bearer " + token);
        mClient.get(url, responseHandler);
    }

    /* Post Mode */

    public static void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.post(context, url, params, responseHandler);
    }

    public static void postWithToken(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.addHeader("Authorization", "Bearer " + token);
        mClient.post(context, url, params, responseHandler);
    }

    public static void postWithToken(Context context, String url, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.addHeader("Authorization", "Bearer " + token);
        mClient.post(url, responseHandler);
    }

    /* Patch Mode */

    public static void patch(Context context, String url, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.addHeader("X-HTTP-Method-Override", "PATCH");
        mClient.put(context, url, entity, "json", responseHandler);
    }

    public static void patchWithToken(Context context, String url, RequestParams entity, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.addHeader("X-HTTP-Method-Override", "PATCH");
        mClient.addHeader("Authorization", "Bearer " + token);
        mClient.post(context, url, entity, responseHandler);
    }

    /* Delete Mode */

    public static void delete(Context context, String url, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.delete(context, url, responseHandler);
    }

    public static void deleteWithToken(Context context, String url, RequestParams entity, AsyncHttpResponseHandler responseHandler) {
        if (!ConnectionUtil.hasInternetConnection(context)) {
            ErrorUtil.showNoInternetConnectionError(context);
        }

        mClient.removeAllHeaders();
        mClient.addHeader("Content-Type", "application/json");
        mClient.delete(context, url, null, entity, responseHandler);
    }

    // Error Helper

    public static String getErrorMesssage(JSONObject errorResponse) {
        String error = "";
        try {
            error = errorResponse.getString("error");
        } catch (Exception exception) {
        }

        return error;
    }
}