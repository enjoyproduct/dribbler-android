package com.mg.dribbler.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.text.Line;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.interfaces.OnLoadMoreListener;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Video;
import com.mg.dribbler.utils.CommonUtil;
import com.mg.dribbler.utils.GridSpacingItemDecoration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FAGFragment extends Fragment {

    private MainActivity mActivity;
    private View contentView;

    // Recycler View
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private List<String> mQuestions;
    private List<String> mAnswers;

    public FAGFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_fag, container, false);
        mActivity = (MainActivity) getActivity();

        // load FAQ assets
        mQuestions = Arrays.asList(getResources().getStringArray(R.array.fag_question));
        mAnswers = Arrays.asList(getResources().getStringArray(R.array.fag_answer));

        // Instantiate FAQ Recycler view
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));

        return contentView;
    }

    /**
     * RecyclerViewAdapter
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private int selectedRow = -1;
        private boolean isExpended = false;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView tvTitle;
            public TextView tvContent;
            public LinearLayout llContent;
            public RelativeLayout rlSection;

            public MyViewHolder(View view) {
                super(view);
                tvTitle = (TextView) view.findViewById(R.id.tv_title);
                tvContent = (TextView) view.findViewById(R.id.tv_content);
                llContent = (LinearLayout) view.findViewById(R.id.ll_content);
                rlSection = (RelativeLayout) view.findViewById(R.id.rl_section);
            }
        }

        public RecyclerViewAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_fag, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final MyViewHolder myHolder = (MyViewHolder) holder;

            String question = mQuestions.get(position);
            String answer = mAnswers.get(position);

            myHolder.tvTitle.setText(question);
            myHolder.tvContent.setText(answer);
            if (position == selectedRow) {
                if (isExpended) {
                    myHolder.llContent.setVisibility(View.VISIBLE);
                } else {
                    myHolder.llContent.setVisibility(View.GONE);
                }
            } else {
                myHolder.llContent.setVisibility(View.GONE);
            }

            myHolder.rlSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position == selectedRow) {
                        isExpended = !isExpended;
                    } else {
                        selectedRow = position;
                        isExpended = true;
                    }
                    mRecyclerViewAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
           return mQuestions.size();
        }
    }
}
