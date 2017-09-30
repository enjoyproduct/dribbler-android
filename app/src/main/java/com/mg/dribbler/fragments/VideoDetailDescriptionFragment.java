package com.mg.dribbler.fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.TrickDescription;
import com.mg.dribbler.services.APIServiceManager;
import com.mg.dribbler.utils.BitmapUtility;
import com.mg.dribbler.utils.DialogUtil;
import com.mg.dribbler.utils.FileUtility;
import com.mg.dribbler.utils.MediaPickUtil;
import com.mg.dribbler.utils.UIUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class VideoDetailDescriptionFragment extends Fragment {

    private MainActivity mActivity;
    private View contentView;
    private VideoDetailFragment mParentFragment;

    /* Description List */
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private MediaPickUtil mPickUtil;

    private ArrayList<TrickDescription> mDescriptionArr = new ArrayList<>();

    /**
     * Life Cycle
     */
    public VideoDetailDescriptionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_video_detail_description, container, false);
        mActivity = (MainActivity) getActivity();
        mParentFragment = (VideoDetailFragment) getParentFragment();
        mDescriptionArr = Global.sharedInstance().selectedTrick.descriptions;

        TextView tvTitle = (TextView) contentView.findViewById(R.id.tv_title);
        tvTitle.setText(Global.sharedInstance().selectedTrick.trick_title);

        // Try on
        contentView.findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showTrickTryOnDialog(getContext());
            }
        });
        // Record Video
        contentView.findViewById(R.id.tv_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
                    @Override
                    public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                        mPickUtil = new MediaPickUtil(VideoDetailDescriptionFragment.this);
                        if (index == 0) { // Camera
                            mPickUtil.captureVideoFromCamera();
                            dialog.dismiss();
                        } else { // Gallery
                            mPickUtil.selectVideoFromGallery();
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

        // List View
        mListView = (ListView) contentView.findViewById(R.id.listView);
        mAdapter = new ListViewAdapter();
        mListView.setAdapter(mAdapter);

        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedVideoUri = null;
        String thumbPath = "";

        if (requestCode == MediaPickUtil.TAKE_VIDEO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            selectedVideoUri = mPickUtil.fileUri;
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mPickUtil.videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
            if (thumbnail == null) {
                UIUtil.showToast(mActivity, "Failed to Create thumbnail");
                return;
            }
            //crop thumbnail
            Bitmap cropBitmap = BitmapUtility.cropBitmapCenter(thumbnail);
            //save cropped thumbnail
            thumbPath = BitmapUtility.saveBitmap(cropBitmap, MediaPickUtil.MEDIA_PATH, "trick_thumb");
            //adjust and save thumbnail
            Bitmap bitmap = BitmapUtility.adjustBitmap(thumbPath);
            thumbPath = BitmapUtility.saveBitmap(bitmap, MediaPickUtil.MEDIA_PATH + "dribbler", FileUtility.getFilenameFromPath(thumbPath));

        } else if (requestCode == MediaPickUtil.TAKE_VIDEO_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedVideoUri = data.getData();
            String videoPath = MediaPickUtil.getVideoPath(mActivity, selectedVideoUri);

            if (videoPath == null && videoPath.length() == 0) {
                UIUtil.showToast(mActivity, "Failed to get video");
                return;
            }

            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
            //crop thumbnail
            Bitmap cropBitmap = BitmapUtility.cropBitmapCenter(thumbnail);
            //save cropped thumbnail
            thumbPath = BitmapUtility.saveBitmap(cropBitmap, MediaPickUtil.MEDIA_PATH, "trick_thumb");

            //adjust and save thumbnail again
            Bitmap bitmap = BitmapUtility.adjustBitmap(thumbPath);
            thumbPath = BitmapUtility.saveBitmap(bitmap, MediaPickUtil.MEDIA_PATH + "dribbler", FileUtility.getFilenameFromPath(thumbPath));
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

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                UIUtil.dismissProgressDialog(mActivity);
                UIUtil.showToast(mActivity, "Failed to upload video");
            }
        });
    }

    /**
     * ListView Adapter for tricks
     */
    public class ListViewAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public ListViewAdapter() {
            this.inflater = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            return mDescriptionArr.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.row_video_description_item, null);
            TrickDescription description = mDescriptionArr.get(position);

            TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            tvTitle.setText(description.title);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            Glide.with(mActivity).load(description.thumbnail).into(imageView);
            TextView tvDescription = (TextView) convertView.findViewById(R.id.tv_description);
            tvDescription.setText(description.description);

            return convertView;
        }
    }

}
