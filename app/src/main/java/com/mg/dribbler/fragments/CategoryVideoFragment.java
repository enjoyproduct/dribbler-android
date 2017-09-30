package com.mg.dribbler.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.activities.VideoPlayerActivity;
import com.mg.dribbler.dialogs.TagFilterDialog;
import com.mg.dribbler.interfaces.TagFilterDialogListener;
import com.mg.dribbler.models.Category;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Tag;
import com.mg.dribbler.models.Trick;
import com.mg.dribbler.services.APIServiceManager;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.ConnectionUtil;
import com.mg.dribbler.utils.UIUtil;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class CategoryVideoFragment extends Fragment {

    private MainActivity mActivity;
    private FragmentManager fragmentManager;
    private View contentView;

    public Category mCategory;

    /* Trick List */
    private ListView listView;
    private CategoryVideoAdapter categoryVideoAdapter;
    private ArrayList<Trick> mFilterTrickArray = new ArrayList<>();

    /* Tags List */
    private RecyclerView mRecyclerView;
    private HorizontalAdapter mRecyclerAdapter;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Tag> mFilterTagArray = new ArrayList<>(Global.sharedInstance().tagArr.subList(0,4));
    private boolean isApplyFilter = false;

    /**
     * Life Cycle
     */
    public CategoryVideoFragment(Category category) {
        mCategory = category;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_category_video, container, false);
        mActivity = (MainActivity) getActivity();
        fragmentManager = getFragmentManager();

        // Initialize ListView
        listView = (ListView) contentView.findViewById(R.id.listView);

        categoryVideoAdapter = new CategoryVideoAdapter();
        listView.setAdapter(categoryVideoAdapter);

        // Initialize Horizontal RecyclerView For Tag Filter
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.my_recycler_view);
        mRecyclerAdapter = new HorizontalAdapter();
        mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        getTricks();
        return contentView;
    }
    /**
     * get trick list
     */
    public void getTricks() {
        UIUtil.showProgressDialog(mActivity, "Loading...");
        String endPoint = String.format(API.GET_TRICK_BY_CATEGORY, mCategory.category_id);
        WebServiceManager.getWithToken(mActivity, endPoint, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                UIUtil.dismissProgressDialog(mActivity);
                mFilterTrickArray = ParseServiceManager.parseTrickResponse(response);
                categoryVideoAdapter.notifyDataSetChanged();
            }
        });
    }
    /**
     * Private Methods
     */
    private void goToVideoDetailPage() {
        Fragment fragment = new VideoDetailFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.addToBackStack(fragment.toString());
        transaction.add(R.id.container, fragment).commit();
    }

    private void playVideo(String link) {
        Intent intent = new Intent(mActivity, VideoPlayerActivity.class);
        intent.putExtra("link", link);
        startActivity(intent);
    }

    private void showTagFilterDlg() {
        TagFilterDialog dialog = new TagFilterDialog(mActivity, mFilterTagArray, new TagFilterDialogListener() {
            @Override
            public void filteredTags(ArrayList<Tag> selectedTags) {
                mFilterTagArray = selectedTags;
                fetchWithSelectedTags();
            }
        });
        dialog.show();
    }

    private void fetchWithSelectedTags() {
        mFilterTrickArray = new ArrayList<>();

        // Filter Trick List
        for (Trick trick : mCategory.tricks) {
            outerLoop:
            for (Tag tag : mFilterTagArray) {
                for (String tagStr : trick.tagArr) {
                    if (tagStr.equals(tag.tag_name)) {
                        mFilterTrickArray.add(trick);
                        break outerLoop;
                    }
                }
            }
        }

        // Reload Tag and Trick List
        mRecyclerAdapter.notifyDataSetChanged();
        categoryVideoAdapter.notifyDataSetChanged();
    }


    /**
     * ListView Adapter for tricks
     */
    public class CategoryVideoAdapter extends BaseAdapter {

        LayoutInflater inflater;
        //String[] thumbBGColorArray = {"#111111", "#e36b6c", "#b8e7b3", "#b4cee7", "#e8dcb4"};

        public CategoryVideoAdapter() {
            this.inflater = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            return mFilterTrickArray.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.row_category_video, null);
            final Trick trick = mFilterTrickArray.get(position);

            // Set Background Color of Thumbnail
            RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.rl_left);
            relativeLayout.setBackgroundColor(Color.parseColor("#111111"));
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ConnectionUtil.isWifiEnabled(mActivity)) {
                        playVideo(trick.hd_video_url);
                    } else {
                        playVideo(trick.ld_video_url);
                    }
                }
            });
            convertView.findViewById(R.id.rl_panel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Trick trick = mFilterTrickArray.get(position);
                    Global.sharedInstance().selectedTrick = mFilterTrickArray.get(position);
                    goToVideoDetailPage();
                }
            });

            TextView tv = (TextView) convertView.findViewById(R.id.tv_title);
            tv.setText(trick.trick_title.toUpperCase());
            ImageView iv = (ImageView) convertView.findViewById(R.id.iv_thumb);
            Glide.with(mActivity).load(trick.thumbnail_url).into(iv);

            // Set Tags List
            RecyclerView recyclerView = (RecyclerView) convertView.findViewById(R.id.recycler_view_tag);
            SubTagAdapter adapter = new SubTagAdapter(trick.tagArr);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            return convertView;
        }
    }

    /**
     * Horizontal Recycler View Adapter
     */
    public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTag;
            public RecyclerView recyclerView;

            public MyViewHolder(View view) {
                super(view);
                tvTag = (TextView) view.findViewById(R.id.tv_tag);
                recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_tag);
            }
        }

        public HorizontalAdapter() {
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category_tag_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            if (position < mFilterTagArray.size()) {
                Tag tag = mFilterTagArray.get(position);
                holder.tvTag.setBackgroundResource(R.drawable.shape_tag_round);
                holder.tvTag.setText(tag.tag_name);
            } else { // More Tag
                holder.tvTag.setBackgroundResource(R.mipmap.icon_more);
                holder.tvTag.setText("      ");
            }

            holder.tvTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTagFilterDlg();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFilterTagArray.size() + 1;
        }
    }

    /**
     * Horizontal Tag List Adapter
     */
    public class SubTagAdapter extends RecyclerView.Adapter<SubTagAdapter.MyViewHolder> {

        ArrayList<String> tagArr;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTag;

            public MyViewHolder(View view) {
                super(view);
                tvTag = (TextView) view.findViewById(R.id.tv_tag);
            }
        }

        public SubTagAdapter(ArrayList<String> tags) {
            this.tagArr = tags;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_trick_tag, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final String tag = tagArr.get(position);
            holder.tvTag.setText(tag);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isApplyFilter) {
                        mFilterTagArray = new ArrayList<>();
                        isApplyFilter = true;
                    }

                    mFilterTagArray = Tag.appendWithoutDuplication(mFilterTagArray, Tag.fetchTagWithName(Global.sharedInstance().tagArr, tag));
                    fetchWithSelectedTags();
                }
            });
        }

        @Override
        public int getItemCount() {
            return tagArr.size();
        }
    }
}
