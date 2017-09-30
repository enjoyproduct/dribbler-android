package com.mg.dribbler.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.misc.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.fragments.CategoryFragment;
import com.mg.dribbler.fragments.CategoryVideoFragment;
import com.mg.dribbler.fragments.FeedFragment;
import com.mg.dribbler.fragments.PremiumFragment;
import com.mg.dribbler.fragments.ProfileFragment;
import com.mg.dribbler.fragments.TutorialFragment;
import com.mg.dribbler.models.Category;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.APIServiceManager;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.PermissionUtil;
import com.mg.dribbler.utils.SharedPrefUtil;
import com.mg.dribbler.utils.UIUtil;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {

    /**
     * Tutorial Pages
     */
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private CircleIndicator mIndicator;
    private static final int NUM_PAGES = 5;

    /**
     * TabBar and Root Fragments
     */
    private Fragment mCategoryFragment;
    private Fragment mPremiumFragment;
    private Fragment mProfileFragment;
    private Fragment mFeedFragment;

    /**
     * Fragment Manage Helper variables
     */
    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        APIServiceManager.getMyVideos(this);
        APIServiceManager.getProfileStatus(this, new JsonHttpResponseHandler());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtil.checkWriteReadExternalStoragePermission(this);

        parseDeepLinking();

        // Instantiate a ViewPager and a Pager
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mIndicator = (CircleIndicator) findViewById(R.id.indicator);
        mPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mPager);

        // Instantiate TabBar and Root Fragments
        final LinearLayout tab1 = (LinearLayout) findViewById(R.id.llTab1);
        tab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(1);
            }
        });
        final TextView tvTab1 = (TextView) findViewById(R.id.tvTab1);
        final ImageView ivTab1 = (ImageView) findViewById(R.id.ivTab1);
        tab1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    tvTab1.setTextColor(Color.WHITE);
                    ivTab1.setColorFilter(Color.WHITE);
                    tab1.setBackgroundColor(Color.parseColor("#4b4b4b"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    tvTab1.setTextColor(Color.parseColor("#AFAFAF"));
                    ivTab1.setColorFilter(Color.parseColor("#AFAFAF"));
                    tab1.setBackgroundColor(Color.parseColor("#212121"));
                }
                return false;
            }
        });
        final LinearLayout tab2 = (LinearLayout) findViewById(R.id.llTab2);
        tab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(2);
            }
        });
        final TextView tvTab2 = (TextView) findViewById(R.id.tvTab2);
        final ImageView ivTab2 = (ImageView) findViewById(R.id.ivTab2);
        tab2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    tvTab2.setTextColor(Color.WHITE);
                    ivTab2.setColorFilter(Color.WHITE);
                    tab2.setBackgroundColor(Color.parseColor("#4b4b4b"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    tvTab2.setTextColor(Color.parseColor("#AFAFAF"));
                    ivTab2.setColorFilter(Color.parseColor("#AFAFAF"));
                    tab2.setBackgroundColor(Color.parseColor("#212121"));
                }
                return false;
            }
        });
        final LinearLayout tab3 = (LinearLayout) findViewById(R.id.llTab3);
        tab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(3);
            }
        });
        final TextView tvTab3 = (TextView) findViewById(R.id.tvTab3);
        final ImageView ivTab3 = (ImageView) findViewById(R.id.ivTab3);
        tab3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    tvTab3.setTextColor(Color.WHITE);
                    ivTab3.setColorFilter(Color.WHITE);
                    tab3.setBackgroundColor(Color.parseColor("#4b4b4b"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    tvTab3.setTextColor(Color.parseColor("#AFAFAF"));
                    ivTab3.setColorFilter(Color.parseColor("#AFAFAF"));
                    tab3.setBackgroundColor(Color.parseColor("#212121"));
                }
                return false;
            }
        });
        final LinearLayout tab4 = (LinearLayout) findViewById(R.id.llTab4);
        tab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(4);
            }
        });
        final TextView tvTab4 = (TextView) findViewById(R.id.tvTab4);
        final ImageView ivTab4 = (ImageView) findViewById(R.id.ivTab4);
        tab4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    tvTab4.setTextColor(Color.WHITE);
                    ivTab4.setColorFilter(Color.WHITE);
                    tab4.setBackgroundColor(Color.parseColor("#4b4b4b"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    tvTab4.setTextColor(Color.parseColor("#AFAFAF"));
                    ivTab4.setColorFilter(Color.parseColor("#AFAFAF"));
                    tab4.setBackgroundColor(Color.parseColor("#212121"));
                }
                return false;
            }
        });

        // Instantiate root fragments of tab bar
        mCategoryFragment = new CategoryFragment();
        mPremiumFragment = new PremiumFragment();
        mProfileFragment = new ProfileFragment();
        mFeedFragment = new FeedFragment();

        // Instantiate fragment manager
        fragmentManager = getSupportFragmentManager();

        // Show Tutorial Pages
        boolean isShowTutorialPages = SharedPrefUtil.loadBoolean(this, AppConstant.PREF_IS_SHOW_TUTORIALS, false);
        if (isShowTutorialPages) {
            switchFragment(0);
            SharedPrefUtil.saveBoolean(this, AppConstant.PREF_IS_SHOW_TUTORIALS, false);
        } else {
            switchFragment(1);
        }

    }
    void parseDeepLinking() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if (data != null) {
            String video_id = data.getPathSegments().get(0);
            String video_url =  "https://s3.eu-central-1.amazonaws.com/dribbler.org-videos/" + video_id + "/video.mp4";
            playVideo(video_url);
        }
    }

    /**
     * Goto Video Player Activity
     */
    private void playVideo(String link) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("link", link);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getFragments().size() == 1) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    /**
     * Fragment Navigation Helper Methods
     */
    public void switchFragment(int index) {
        hideTutorialPage();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (index == 0) { // Tutorial Page
            mPager.setVisibility(View.VISIBLE);
            mIndicator.setVisibility(View.VISIBLE);
        } else if (index == 1) { // Category Page
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, mCategoryFragment);
            transaction.commit();
        } else if (index == 2) { // Premium Page
            ((PremiumFragment)mPremiumFragment).current_page = 0;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, mPremiumFragment);
            transaction.commit();
        } else if (index == 3) { // Profile Page
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, mProfileFragment);
            transaction.commit();
        } else if (index == 4) { // Feed Page
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, mFeedFragment);
            transaction.commit();
        }
    }

    public void showUpgradePage() {
        ((PremiumFragment)mPremiumFragment).current_page = 1;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, mPremiumFragment);
        transaction.commit();
    }

    public void goToOtherUserProfilePage(User user) {
        ProfileFragment fragment = new ProfileFragment(user);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.addToBackStack(fragment.toString());
        transaction.add(R.id.container, fragment).commit();
    }

    public void goToCategoryVideo(Category category) {
        Fragment fragment = new CategoryVideoFragment(category);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.addToBackStack(fragment.toString());
        transaction.add(R.id.container, fragment).commit();
    }

    private void hideTutorialPage() {
        if (mPager != null) {
            mPager.setVisibility(View.GONE);
            mIndicator.setVisibility(View.GONE);
        }
    }
}
