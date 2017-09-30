package com.mg.dribbler.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.activities.VideoDetailActivity;
import com.mg.dribbler.interfaces.OnLoadMoreListener;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.User;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.services.APIServiceManager;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.BitmapUtility;
import com.mg.dribbler.utils.CommonUtil;
import com.mg.dribbler.utils.DeviceUtility;
import com.mg.dribbler.utils.DialogUtil;
import com.mg.dribbler.utils.FileUtility;
import com.mg.dribbler.utils.GridSpacingItemDecoration;
import com.mg.dribbler.utils.UIUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class VideoDetailMyVideosFragment extends Fragment {

    private MainActivity mActivity;
    private View contentView;
    private VideoDetailFragment mParentFragment;

    // Recycler View
    RecyclerViewAdapter mRecyclerViewAdapter;
    RecyclerView mRecyclerView;

    /**
     * Broadcast observer
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    };


    /**
     * Life Cycle
     */
    public VideoDetailMyVideosFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_TRICK_MY_VIDEOS));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contentView = inflater.inflate(R.layout.fragment_video_detail_my_videos, container, false);
        mActivity = (MainActivity) getActivity();
        mParentFragment = (VideoDetailFragment) getParentFragment();

        TextView tvMyVideo = (TextView) contentView.findViewById(R.id.tv_my_video);
        tvMyVideo.setText("Your " + Global.sharedInstance().selectedTrick.trick_title + " Videos");

        // My Video GridView
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.rv_ur_video);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, CommonUtil.convertDpToPixels(10f), true));
        mRecyclerViewAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });

        // Record Button
        contentView.findViewById(R.id.btn_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {;
                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
                    @Override
                    public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                        if (index == 0) { // Camera
                            captureVideoFromCamera();
                            dialog.dismiss();
                        } else { // Gallery
                            selectVideoFromGallery();
                            dialog.dismiss();
                        }
                    }
                });
                adapter.add(new MaterialSimpleListItem.Builder(mActivity)
                        .content("camera")
                        .icon(R.mipmap.camera_colored)
                        .backgroundColor(Color.WHITE)
                        .build());
                adapter.add(new MaterialSimpleListItem.Builder(mActivity)
                        .content("gallery")
                        .icon(R.mipmap.gallery_colored)
                        .backgroundColor(Color.WHITE)
                        .build());
                new MaterialDialog.Builder(mActivity)
                        .title("Choose video from")
                        .titleColor(getResources().getColor(R.color.text_color_grey))
                        .adapter(adapter, null)
                        .positiveText("Cancel")
                        .show();
            }
        });

        return contentView;
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedVideoUri = null;
            String thumbPath = "";

            if (requestCode == activity_result_video_from_camera) {
                selectedVideoUri = fileUri;
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                if (thumbnail == null) {
                    UIUtil.showToast(mActivity, "Failed to Create thumbnail");
                    return;
                }
                //crop thumbnail
                Bitmap cropBitmap = BitmapUtility.cropBitmapCenter(thumbnail);
                //save cropped thumbnail
                thumbPath = BitmapUtility.saveBitmap(cropBitmap, MEDIA_PATH, "trick_thumb");
                //adjust and save thumbnail
                Bitmap bitmap = BitmapUtility.adjustBitmap(thumbPath);
                thumbPath = BitmapUtility.saveBitmap(bitmap, MEDIA_PATH + "dribbler", FileUtility.getFilenameFromPath(thumbPath));

            } else if (requestCode == activity_result_video_from_gallery) {
                selectedVideoUri = data.getData();
                videoPath = getVideoPath(selectedVideoUri);

                if (videoPath == null && videoPath.length() == 0) {
                    UIUtil.showToast(mActivity, "Failed to get video");
                    return;
                }

                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                //crop thumbnail
                Bitmap cropBitmap = BitmapUtility.cropBitmapCenter(thumbnail);
                //save cropped thumbnail
                thumbPath = BitmapUtility.saveBitmap(cropBitmap, MEDIA_PATH, "trick_thumb");

                //adjust and save thumbnail again
                Bitmap bitmap = BitmapUtility.adjustBitmap(thumbPath);
                thumbPath = BitmapUtility.saveBitmap(bitmap, MEDIA_PATH + "dribbler", FileUtility.getFilenameFromPath(thumbPath));
            }

            //upload video to server
            if (selectedVideoUri != null) {
                RequestParams params = new RequestParams();
                params.put("trick_id", Global.sharedInstance().selectedTrick.trick_id);

                try {
                    InputStream fileInputStream = mActivity.getContentResolver().openInputStream(selectedVideoUri);
                    File img = new File(thumbPath);

                    params.put("thumbnail", img);
                    params.put("video", fileInputStream);

                    uploadVideo(params);
                } catch (Exception exception) {
                    UIUtil.showToast(mActivity, "Failed to open file");
                }
            }
        }
    }

    /**
     * Load My Videos From Server
     */
    public void loadMore() {
        if (mParentFragment.myTrickVideosPagination.next_page_url == null) {
            return;
        }

        String endPoint = mParentFragment.myTrickVideosPagination.next_page_url;

        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                mParentFragment.myTrickVideosPagination = ParseServiceManager.parsePaginationInfo(response);
                ArrayList<Video> arr = ParseServiceManager.parseVideoArrayResponse(response);
                mParentFragment.myTrickVideos = Video.appendVideoArray(mParentFragment.myTrickVideos, arr);
                mRecyclerViewAdapter.notifyDataSetChanged();
                mRecyclerViewAdapter.setLoaded();
            }
        });
    }

    /**
     * RecyclerViewAdapter
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // Load More
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading;
        private int visibleThreshold = 1;
        private int lastVisibleItem, totalItemCount;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public PercentRelativeLayout container;

            public MyViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.iv_thumb);
                container = (PercentRelativeLayout) view.findViewById(R.id.prl_container);
            }
        }

        public RecyclerViewAdapter() {
            final GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = layoutManager.getItemCount();
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }

        public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
            this.mOnLoadMoreListener = mOnLoadMoreListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_video_item, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myHolder = (MyViewHolder) holder;

            final Video video;
            video = mParentFragment.myTrickVideos.get(position);

            Glide.with(mActivity).load(video.thumbnail).into(myHolder.imageView);
            myHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoVideoDetailPage(video);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mParentFragment.myTrickVideos.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }


    /**
     * Upload Video file
     */
    public void uploadVideo(RequestParams params) {

        UIUtil.showProgressDialog(mActivity, "Uploading...");
        mParentFragment.postVideo(mActivity, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                UIUtil.dismissProgressDialog(mActivity);
                mRecyclerViewAdapter.notifyDataSetChanged();
                UIUtil.showAlert(mActivity, null, "Uploaded video successfully!", "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DialogUtil.showTrickTryOnDialog(mActivity);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                UIUtil.dismissProgressDialog(mActivity);
                UIUtil.showToast(mActivity, "Failed to upload video");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                UIUtil.dismissProgressDialog(mActivity);
                UIUtil.showToast(mActivity, "Failed to upload video");
            }
        });
    }

    /**
     * Navigate Methods
     */
    private void gotoVideoDetailPage(Video video) {
        Intent intent = new Intent(mActivity, VideoDetailActivity.class);
        intent.putExtra("video", video);
        intent.putExtra("username", User.currentUser().getFullName());
        intent.putExtra("photo", User.currentUser().photoURL);
        startActivity(intent);
    }


    /*********************************
     * Collect Media (Video) Methods
     *********************************
     */

    private Uri fileUri;
    public static String MEDIA_PATH = Environment.getExternalStorageDirectory().toString() + "/";

    private static final int MEDIA_TYPE_VIDEO = 2;
    private static final int activity_result_video_from_gallery = 100;
    private static final int activity_result_video_from_camera = 101;
    private static final String VIDEO_FILE_PREFIX = "dribbler_trick";
    private static final String VIDEO_FILE_SUFFIX = ".mp4";

    private String videoPath;


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
        startActivityForResult(intent, activity_result_video_from_gallery);
    }

    public void captureVideoFromCamera() {
        // create new Intent with with Standard Intent action that can be
        // sent to have the camera application capture an video and return it.
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // create a file to save the video
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        videoPath = fileUri.getPath();
        // set the image file name
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        // set max time limit
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 300);
        ///set max size limit
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, DeviceUtility.getFreeRamSize(mActivity) * 1024 * 1024 / 1);
        // start the Video Capture Intent
        startActivityForResult(intent, activity_result_video_from_camera);
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
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
                Toast.makeText(mActivity, "Failed to create directory Dribbler.", Toast.LENGTH_LONG).show();
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

    public String getVideoPath(Uri uri) {
        String path = "";
        try {
            Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = mActivity.getContentResolver().query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
