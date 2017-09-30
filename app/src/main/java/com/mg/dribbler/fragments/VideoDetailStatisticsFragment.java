package com.mg.dribbler.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.models.Dribbler;
import com.mg.dribbler.utils.AppConstant;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.util.ArrayList;


public class VideoDetailStatisticsFragment extends Fragment {

    private MainActivity mActivity;
    private VideoDetailFragment parentFragment;
    private View contentView;

    // Widget
    private ColorfulRingProgressView mPieChart;
    private CombinedChart mCombinedChart;
    //private BarChart mCombinedChart;
    private TextView mCenterText;

    /**
     * Broadcast observer
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Instantiate charts
            initializeLineChart();
            initializePieChart();
        }
    };


    /**
     * Life Cycle
     */
    public VideoDetailStatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, new IntentFilter(AppConstant.BROADCAST_GET_TRICK_STATISTICS));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contentView = inflater.inflate(R.layout.fragment_video_detail_statistics, container, false);
        mActivity = (MainActivity) getActivity();
        parentFragment = (VideoDetailFragment) getParentFragment();

        mCenterText = (TextView) contentView.findViewById(R.id.tv_center);
        mCombinedChart = (CombinedChart) contentView.findViewById(R.id.bar_chart);
        mPieChart = (ColorfulRingProgressView) contentView.findViewById(R.id.pie_chart);

        // Instantiate charts
        initializeLineChart();
        initializePieChart();

        return contentView;
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * Chart Setting Methods
     */
    private void initializeLineChart() {
        mCombinedChart.getDescription().setEnabled(false);
        mCombinedChart.setBackgroundColor(Color.WHITE);
        mCombinedChart.setDrawBarShadow(false);
        mCombinedChart.setDrawValueAboveBar(true);
        mCombinedChart.setViewPortOffsets(10, 0, 10, 0);

        mCombinedChart.getXAxis().setEnabled(false);
        mCombinedChart.getAxisLeft().setEnabled(false);
        mCombinedChart.getAxisRight().setEnabled(false);
        mCombinedChart.getLegend().setEnabled(false);

        mCombinedChart.setScaleXEnabled(false);
        mCombinedChart.setScaleYEnabled(false);
        mCombinedChart.setTouchEnabled(false);

        XAxis xAxis = mCombinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setGranularity(1f);

        CombinedData data = new CombinedData();

        data.setData(generateLineData());
        data.setData(generateBarData());

        mCombinedChart.getXAxis().setAxisMaximum(data.getXMax() + 0.5f);
        mCombinedChart.setData(data);
        mCombinedChart.invalidate(); // refresh
    }

    private void initializePieChart() {
        String centerStr;
        float avg;

        if (parentFragment.myTrickDribblers.size() > 0) {
            int i = 0;
            avg = 0;
            for (Dribbler dribbler : parentFragment.myTrickDribblers) {
                i++;
                avg += dribbler.try_on;
            }
            avg = avg / i;
            centerStr = String.format("%.1f / 10", avg);
        } else {
            centerStr = "5.5 / 10";
            avg = 5.5f;
        }

        mCenterText.setText(centerStr);
        mPieChart.setPercent(avg * 10);
    }

    private BarData generateBarData() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        if (parentFragment.myTrickDribblers.size() > 0) {
            for (int i = 0; i < parentFragment.myTrickDribblers.size(); i++) {
                Dribbler dribbler = parentFragment.myTrickDribblers.get(i);
                entries.add(new BarEntry(i, dribbler.try_on, String.format("%d/10", dribbler.try_on)));
            }
        } else {
            entries.add(new BarEntry(1f, 1, "1/10"));
            entries.add(new BarEntry(2f, 2, "2/10"));
            entries.add(new BarEntry(3f, 3, "3/10"));
            entries.add(new BarEntry(4f, 4, "4/10"));
            entries.add(new BarEntry(5f, 5, "5/10"));
            entries.add(new BarEntry(6f, 6, "6/10"));
            entries.add(new BarEntry(7f, 7, "7/10"));
            entries.add(new BarEntry(8f, 8, "8/10"));
            entries.add(new BarEntry(9f, 9, "9/10"));
            entries.add(new BarEntry(10f, 10, "10/10"));
        }

        BarDataSet set = new BarDataSet(entries, null);
        set.setColor(Color.parseColor("#599e5f"));
        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (entry.getData() == null) {
                    return "";
                }
                return entry.getData().toString();
            }
        });

        BarData data = new BarData(set);
        data.setBarWidth(0.7f);

        return data;
    }

    private LineData generateLineData() {

        LineData data = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        if (parentFragment.myTrickDribblers.size() > 0) {
            for (int i = 0; i < parentFragment.myTrickDribblers.size(); i++) {
                Dribbler dribbler = parentFragment.myTrickDribblers.get(i);
                entries.add(new Entry(i, dribbler.try_on + 1));
            }
        } else {
            entries.add(new Entry(1f, 2, "1/10"));
            entries.add(new Entry(2f, 3, "2/10"));
            entries.add(new Entry(3f, 4, "3/10"));
            entries.add(new Entry(4f, 5, "4/10"));
            entries.add(new Entry(5f, 6, "5/10"));
            entries.add(new Entry(6f, 7, "6/10"));
            entries.add(new Entry(7f, 8, "7/10"));
            entries.add(new Entry(8f, 9, "8/10"));
            entries.add(new Entry(9f, 10, "9/10"));
            entries.add(new Entry(10f, 11, "10/10"));
        }

        LineDataSet set = new LineDataSet(entries, "");
        set.setColor(Color.TRANSPARENT);
        set.setCircleColor(Color.parseColor("#ff6d6e"));
        set.setDrawCircleHole(false);
        set.setLineWidth(0f);
        set.setCircleRadius(5f);
        set.setFillColor(Color.parseColor("#f6fdf6"));
        set.setValueTextColor(Color.rgb(240, 238, 70));

        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        });
        data.addDataSet(set);

        return data;
    }

    protected BubbleData generateBubbleData() {

        BubbleData bd = new BubbleData();

        ArrayList<BubbleEntry> entries = new ArrayList<BubbleEntry>();

        if (parentFragment.myTrickDribblers.size() > 0) {
            for (int i = 0; i < parentFragment.myTrickDribblers.size(); i++) {
                Dribbler dribbler = parentFragment.myTrickDribblers.get(i);
                entries.add(new BubbleEntry(i + 0.5f, dribbler.try_on + 1, 0));
            }
        } else {
            entries.add(new BubbleEntry(1f, 1, 0));
            entries.add(new BubbleEntry(2f, 2, 0));
            entries.add(new BubbleEntry(3f, 3, 0));
            entries.add(new BubbleEntry(4f, 4, 0));
            entries.add(new BubbleEntry(5f, 5, 0));
            entries.add(new BubbleEntry(6f, 6, 0));
            entries.add(new BubbleEntry(7f, 7, 0));
            entries.add(new BubbleEntry(8f, 8, 0));
            entries.add(new BubbleEntry(9f, 9, 0));
            entries.add(new BubbleEntry(10f, 10, 0));
        }

        BubbleDataSet set = new BubbleDataSet(entries, "");
        set.setColors(Color.TRANSPARENT);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.TRANSPARENT);
        set.setHighlightCircleWidth(1.5f);
        set.setDrawValues(true);
        bd.addDataSet(set);

        return bd;
    }
}