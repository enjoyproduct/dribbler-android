package com.mg.dribbler.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by Admin on 5/5/2017.
 */

public class MediaPickUtil {

    private Fragment mFragment;

    public Uri fileUri;

    public static String MEDIA_PATH = Environment.getExternalStorageDirectory().toString() + "/";

    private static final int MEDIA_TYPE_VIDEO = 2;
    private static final String VIDEO_FILE_PREFIX = "dribbler_trick";
    private static final String VIDEO_FILE_SUFFIX = ".mp4";

    public static final int TAKE_VIDEO_FROM_GALLERY = 100;
    public static final int TAKE_VIDEO_FROM_CAMERA = 101;

    public String photoPath, videoPath, thumbPath;


    public MediaPickUtil(Fragment fragment) {
        this.mFragment = fragment;
    }


    /**
     * Public Methods
     */
    public void selectVideoFromGallery() {
        Intent intent;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        }
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return-data", true);
        mFragment.startActivityForResult(intent, TAKE_VIDEO_FROM_GALLERY);
    }

    public void captureVideoFromCamera() {
        // create new Intent with with Standard Intent action that can be
        // sent to have the camera application capture an video and return it.
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // create a file to save the video
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        initMediaPath();
        videoPath = fileUri.getPath();
        // set the image file name
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        // set max time limit
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
        ///set max size limit
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, DeviceUtility.getFreeRamSize(mFragment.getActivity()) * 1024 * 1024 / 1);
        // start the Video Capture Intent
        mFragment.startActivityForResult(intent, TAKE_VIDEO_FROM_CAMERA);
    }

    public static String getVideoPath(Context context, Uri uri) {
        String path = "";
        try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = context.getContentResolver().query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private void initMediaPath() {
        photoPath = "";
        videoPath = "";
        thumbPath = "";
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {
        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Dribbler");
        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(mFragment.getActivity(), "Failed to create directory Dribbler.", Toast.LENGTH_LONG).show();
                Log.d("MyCameraVideo", "Failed to create directory Dribbler.");
                return null;
            }
        }

        // For unique file name appending current timeStamp with file name
        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());
        File mediaFile;
        if (type == MEDIA_TYPE_VIDEO) {
            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + VIDEO_FILE_PREFIX + timeStamp + VIDEO_FILE_SUFFIX);
        } else {
            return null;
        }
        return mediaFile;
    }

}
