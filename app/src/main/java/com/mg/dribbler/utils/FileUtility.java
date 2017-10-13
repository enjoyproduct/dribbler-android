package com.mg.dribbler.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by Administrator on 1/26/2016.
 */
public class FileUtility {

    public static String getFilenameFromPath(String fileLocalPath) {
        return fileLocalPath.substring(fileLocalPath.lastIndexOf("/") + 1);
    }

    public static void deleteFilesInDirectory(String dirPath) {
        File f = new File(dirPath);
        File file[] = f.listFiles();
        for (File df : file) {
            df.delete();

        }
    }

    public static boolean deleteDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();

        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static boolean deleteFile(String PATH) {

        File file = new File(PATH);
        if (file.exists()) {
            file.delete();
            return true;
        } else
            return false;
    }

    public static boolean checkFileExist(String fileName, String PATH) {

        File file = new File(PATH + fileName);
        if (file.exists()) {
            return true;
        } else
            return false;
    }

    // convert bitmap to byte[] from Uri

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
