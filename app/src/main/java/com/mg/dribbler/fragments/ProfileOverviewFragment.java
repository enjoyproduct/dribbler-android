package com.mg.dribbler.fragments;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Score;
import com.mg.dribbler.models.TrickDescription;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class ProfileOverviewFragment extends Fragment {

    private MainActivity mActivity;
    private ProfileFragment parentFragment;
    private View contentView;
    private RadarChart mChart;

    /* Description List */
    private GridView mGridView;
    private ListViewAdapter mAdapter;

    public ArrayList<Score> myCategoryScore;
    public ArrayList<Score> myTagScore;
    private User selectedUser;
    /**
     * Broadcast observer
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
            setData();
        }
    };


    /**
     * Life Cycle
     */
    public ProfileOverviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        parentFragment = (ProfileFragment) getParentFragment();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_OTHER_PROFILE_STATISTICS));
        myCategoryScore = new ArrayList<>();
        myTagScore = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_profile_overview, container, false);

        // Chart
        mChart = (RadarChart) contentView.findViewById(R.id.radar_chart);
        initChart();

        // list view
        mGridView = (GridView) contentView.findViewById(R.id.gridView);
        mAdapter = new ListViewAdapter();
        mGridView.setAdapter(mAdapter);
        if (parentFragment.isMyProfile) {
            selectedUser = User.currentUser();
        } else {
            selectedUser = parentFragment.user;
        }
//        loadProfileStatus();
        loadProfileOverView();
        return contentView;
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * Load Profile Status
     */
    public void loadProfileOverView() {
        String endPoint = String.format(API.GET_PROFILE_STATISTIC, selectedUser.userID);
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    myCategoryScore = ParseServiceManager.parseCategoryStatistic(response.getJSONArray("overview"));
                    myTagScore = ParseServiceManager.parseTagStatistic(response.getJSONArray("tag_statistic"));
                    sendNotification(mActivity, AppConstant.BROADCAST_GET_OTHER_PROFILE_STATISTICS);
                } catch (Exception exception) {
                    Log.e("Parse Error", "Profile Status");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    /**
     * Send Broadcast event
     */
    private static void sendNotification(Context context, String event) {
        Intent intent = new Intent(event);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
//            if (parentFragment.isMyProfile) {
//                return Global.sharedInstance().myTagScore.size();
//            } else {
//                return parentFragment.myTagScore.size();
//            }
            return myTagScore.size();
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
            convertView = inflater.inflate(R.layout.row_tag, null);
            Score score = myTagScore.get(position);

            TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            TextView tvScore = (TextView) convertView.findViewById(R.id.tv_score);
            ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            tvTitle.setText(score.title);
            tvScore.setText(Double.toString(score.score) + "/10");
            progressBar.setMax(100);
            progressBar.setProgress((int)(score.score.floatValue() * 10.0f));

            return convertView;
        }
    }

    private void initChart() {
        //mChart.setBackgroundColor(Color.parseColor("#f4f4f4"));
        mChart.setWebLineWidth(1f);
        mChart.setWebColor(Color.LTGRAY);
        mChart.setWebLineWidthInner(1f);
        mChart.setWebColorInner(Color.LTGRAY);
        mChart.setWebAlpha(100);

        mChart.setRotationEnabled(false);
        mChart.setClickable(false);
        mChart.getLegend().setEnabled(false);
        mChart.getDescription().setEnabled(false);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        setData();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(9f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);
        xAxis.setTextColor(Color.parseColor("#b8b8b8"));


        YAxis yAxis = mChart.getYAxis();
        yAxis.setLabelCount(3, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(10f);
        yAxis.setGridColor(Color.parseColor("#c1c1c1"));
        yAxis.setDrawLabels(false);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.WHITE);
    }

    public void setData() {
        ArrayList<RadarEntry> entries1 = new ArrayList<RadarEntry>();
        final ArrayList<String> arrTitles = new ArrayList();
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of the chart.
        for (int i = 0; i < myCategoryScore.size(); i ++) {
            Score score = myCategoryScore.get(i);
            float val = score.score.floatValue();
            entries1.add(new RadarEntry(val, score.title));

            arrTitles.add(score.title + " \n" + String.valueOf(score.score));
        }
        RadarDataSet set1 = new RadarDataSet(entries1, "");
        set1.setColor(Color.rgb(103, 110, 129));
        set1.setFillColor(Color.rgb(103, 110, 129));
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(3f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);
        set1.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return myCategoryScore.get(dataSetIndex).title;
            }
        });
        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);

        RadarData data = new RadarData(sets);
        data.setValueTextSize(12f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        XAxis xAxis = mChart.getXAxis();
        if (myCategoryScore.size() > 2) {
            xAxis.setValueFormatter(new IAxisValueFormatter() {


                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return arrTitles.get((int) value % arrTitles.size());
                }
            });
        }
        mChart.setData(data);
        mChart.invalidate();
    }

}
