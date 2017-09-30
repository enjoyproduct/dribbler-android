package com.mg.dribbler.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by Admin on 5/5/2017.
 */

public class PermissionUtil {

    /**
     * Permission Methods
     */
    private final static int PERMISSION_REQUEST_CODE_FOR_PERMISSION = 201;

    public static void checkPermissions(Activity activity) {
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraAccessPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int recordVideoPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAPTURE_VIDEO_OUTPUT);

        ArrayList<String> arrPermissionRequests = new ArrayList<>();
        if (readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            arrPermissionRequests.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            arrPermissionRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (cameraAccessPermission != PackageManager.PERMISSION_GRANTED) {
            arrPermissionRequests.add(Manifest.permission.CAMERA);
        }
        if (recordVideoPermission != PackageManager.PERMISSION_GRANTED) {
            arrPermissionRequests.add(Manifest.permission.CAPTURE_VIDEO_OUTPUT);
        }

        if (!arrPermissionRequests.isEmpty()) {
            ActivityCompat.requestPermissions(activity, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), PERMISSION_REQUEST_CODE_FOR_PERMISSION);
        }
    }

    public static void checkWriteReadExternalStoragePermission(Activity activity) {
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        ArrayList<String> arrPermissionRequests = new ArrayList<>();
        if (readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            arrPermissionRequests.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            arrPermissionRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!arrPermissionRequests.isEmpty()) {
            ActivityCompat.requestPermissions(activity, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), PERMISSION_REQUEST_CODE_FOR_PERMISSION);
        }
    }
}
