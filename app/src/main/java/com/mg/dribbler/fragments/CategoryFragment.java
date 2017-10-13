package com.mg.dribbler.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.text.Text;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.BaseActivity;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.models.Category;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.CommonUtil;
import com.mg.dribbler.utils.UIUtil;
import com.mg.dribbler.utils.image_downloader.UrlImageViewCallback;
import com.mg.dribbler.utils.image_downloader.UrlRectangleImageViewHelper;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class CategoryFragment extends Fragment {

    private Activity mActivity;
    ListView listView;
    CategoryAdapter categoryAdapter;
    public ArrayList<Category> categoryArr = new ArrayList<>();


    /**
     * Life Cycle
     */
    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        mActivity = getActivity();
        listView = (ListView)view.findViewById(R.id.listView);
        categoryAdapter = new CategoryAdapter();
        listView.setAdapter(categoryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category = categoryArr.get(position);
                if (!category.premium || category.unlocked) {
                    ((MainActivity)mActivity).goToCategoryVideo(category);
                } else {
                    ((MainActivity)mActivity).showUpgradePage();
                }
            }
        });
        getCategories();
        return view;
    }

    /**
     * get category list
     */
    public void getCategories() {
        UIUtil.showProgressDialog(mActivity, "Loading...");
        WebServiceManager.getWithToken(mActivity, API.GET_CATEGORIES, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                UIUtil.dismissProgressDialog(mActivity);
                categoryArr = ParseServiceManager.parseCategoryResponse(response);
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }


    class CategoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return categoryArr.size();
        }

        @Override
        public Object getItem(int position) {
            return categoryArr.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mActivity.getLayoutInflater().inflate(R.layout.reuse_category, null);
            }
            Category category = categoryArr.get(position);

            view.setTag(category);
            ImageView imgView = (ImageView) view.findViewById(R.id.iv_thumb);
            UrlRectangleImageViewHelper.setUrlDrawable(imgView,category.thumbnail_url, R.drawable.tab_background_unselected, new UrlImageViewCallback() {
                @Override
                public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                    if (!loadedFromCache) {
                        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                        scale.setDuration(10);
                        scale.setInterpolator(new OvershootInterpolator());
                        imageView.startAnimation(scale);
                    }
                }
            });
            TextView textView = (TextView) view.findViewById(R.id.tv_title);
            textView.setText(category.category_title);
            ImageView lockImgView = (ImageView) view.findViewById(R.id.iv_lock);

            if (!category.premium || category.unlocked) {
                lockImgView.setBackgroundResource(R.mipmap.available);
            } else {
                lockImgView.setBackgroundResource(R.mipmap.unlock);
            }
            return view;
        }
    }

}
