package com.mg.dribbler.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommonUtil {

    /**
     * @return Status Bar Height as Pixel Unit
     */
    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = Resources.getSystem().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getWindowsHeightInActivity(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.y;
    }

    public static int getWindowsWidthInActivity(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.x;
    }

    public static int getWindowsHeightInFragment(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.y;
    }

    public static int getWindowsWidthInFragment(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.x;
    }

    /**
     * Unit Converter
     */
    public static int convertPixelToDp(int pixels) {
        int dp = Math.round(pixels / (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

        return dp;
    }

    public static int convertDpToPixels(float dp) {
        return Math.round(dp * (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int convertSpToPixels(float sp) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displaymetrics);
        return px;
    }

    public static int convertDpToSp(float dp) {
        int sp = (int) (convertDpToPixels(dp) / (float) convertSpToPixels(dp));
        return sp;
    }

    /**
     * Date Converter
     */
    public static String formateDate(String format, long date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(date));
    }

    public static String formatDate(String strDate) throws ParseException {
        //2015-08-16T14:39:31Z
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        Date date = simpleDateFormat.parse(strDate);
        String newstring = new SimpleDateFormat("MM/dd/yyyy").format(date);
        return newstring;
    }

    public static long formatLongDate(String strDate) throws ParseException {
        //2015-08-16T14:39:31Z
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        Date date = simpleDateFormat.parse(strDate);
        return date.getTime();
    }

    public static long formatLongDate1(String strDate) throws ParseException {
        //2015-08-16T14:39:31Z
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        Date date = simpleDateFormat.parse(strDate);
        return date.getTime();
    }

    public static String convertDate(long dateInMilliseconds, String dateFormat) {
        return DateFormat.format(dateFormat, dateInMilliseconds).toString();
    }

    public static String convertDateUTCToTime(String strDate) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(strDate);
            SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
            df2.setTimeZone(TimeZone.getDefault());
            String formattedDate = df2.format(date);
            return formattedDate;
        } catch (Exception ex) {
            Log.d("CommonUtil", "CommonUtil", ex);
        }
        return "";
    }

    //11/18/2015
    public static String convertDateUTCToDate(String strDate) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(strDate);
            SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
            df2.setTimeZone(TimeZone.getDefault());
            String formattedDate = df2.format(date);
            return formattedDate;
        } catch (Exception ex) {
            Log.d("CommonUtil", "CommonUtil", ex);
        }
        return "";
    }

    public static String getTimeCurrentUTC() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return simpleDateFormat.format(Calendar.getInstance().getTime());
        } catch (Exception ex) {
            Log.d("CommonUtil", "CommonUtil", ex);
        }
        return "";
    }

    public static boolean isUrlValid(String source) {
        try {
            new URL(source);
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void showKeyHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method checks if the app is in background or not
     *
     * @param context
     * @return
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}
