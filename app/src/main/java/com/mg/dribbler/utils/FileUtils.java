package com.mg.dribbler.utils;

/**
 * Created by Hung Hoang Minh on 06/10/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kahn on 11/07/2015.
 */
public class FileUtils {

    private static final String TAG = FileUtils.class.getName();

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * @return Path of image
     * @id
     * @description Write bitmap to file
     */
    public static String writeBitmap2File(Bitmap bmp) {
        FileOutputStream out = null;
        String path = "";
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            File file = File.createTempFile(imageFileName, ".jpg", storageDir);
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            path = file.getAbsolutePath();
        } catch (Exception e) {
            Log.d("writeBitmap2File", "writeBitmap2File", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.d("writeBitmap2File", "writeBitmap2File", e);
            }
        }

        return path;
    }

    public static String getPath(Activity activity, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static File getFileRezie(File imgFileOrig) {
        try {
            if (imgFileOrig == null)
                return null;

            if (!imgFileOrig.exists())
                return null;

            int oneKb = 1 * 1024;
            int limitSize = 100 * oneKb;

            if (imgFileOrig.length() <= limitSize)
                return imgFileOrig;

            ExifInterface exif = new ExifInterface(
                    imgFileOrig.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            File dirTemp = new File(AppConstant.DIR_LOCAL_IMAGE_TEMP);
            if (!dirTemp.exists())
                dirTemp.mkdirs();

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(imgFileOrig);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > AppConstant.IMAGE_MAX_SIZE || o.outWidth > AppConstant.IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.ceil(Math.log(AppConstant.IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            // we save the file, at least until we have made use of it
            File f = new File(AppConstant.DIR_LOCAL_IMAGE_TEMP + File.separator + imgFileOrig.getName());
            f.createNewFile();

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(imgFileOrig);
            Bitmap b = BitmapFactory.decodeStream(fis, null, o2);
            if (orientation > 0)
                b = rotateBitmap(b, orientation);
            fis.close();

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            // compress to the format you want, JPEG, PNG...
            // 70 is the 0-100 quality percentage
            b.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

            // write the bytes in file
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(outStream.toByteArray());

            // remember close de FileOutput
            fo.close();

            b.recycle();

            return f;

        } catch (Exception e) {

            Log.d(TAG, "Exception", e);

        }

        return null;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    public static boolean deleteFile(String dirFile) {
        if (!TextUtils.isEmpty(dirFile)) {
            File file = new File(dirFile);
            if (file.isFile() && file.exists())
                return file.delete();
        }
        return false;
    }

    public static Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    public static File getTempFile() {
        File f = null;
        try {
            f = File.createTempFile("onedollar_tmp_", ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
}
