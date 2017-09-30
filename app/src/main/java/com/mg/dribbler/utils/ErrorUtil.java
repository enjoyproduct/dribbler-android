package com.mg.dribbler.utils;

import android.content.Context;

public class ErrorUtil {

    /**
     * HTTP Error Message
     */
    public static void showInternalServerError(Context context) {
        UIUtil.showAlertDialog(context, "Something went wrong", "", "OK");
    }

    public static void showNoInternetConnectionError(Context context) {
        UIUtil.showAlertDialog(context, "No Internet Connection", "", "OK");
    }
}
