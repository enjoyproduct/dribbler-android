package com.mg.dribbler.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.interfaces.TagFilterDialogListener;
import com.mg.dribbler.models.Tag;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.views.MyCircularImageView;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import cz.msebera.android.httpclient.Header;


public class VideoFollowerDialog extends Dialog {

    private Context context;
    private ArrayList<User> mFollowerArray;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;

    public VideoFollowerDialog(Context context, ArrayList<User> followers) {
        super(context, R.style.CustomDialog);
        this.context = context;
        this.mFollowerArray = followers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_video_follower);

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    /**
     * RecyclerViewAdapter
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView tvName ;
            public MyCircularImageView ivAvatar;
            public CircularProgressButton button;

            public MyViewHolder(View view) {
                super(view);
                tvName = (TextView) findViewById(R.id.tv_name);
                ivAvatar = (MyCircularImageView) view.findViewById(R.id.iv_avatar);
                button = (CircularProgressButton) view.findViewById(R.id.btn_unfollow);
            }
        }

        public RecyclerViewAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_follower, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerViewAdapter.MyViewHolder myHolder = (RecyclerViewAdapter.MyViewHolder) holder;
            final User user = mFollowerArray.get(position);
            myHolder.tvName.setText(user.first_name + "" + user.last_name);
            Glide.with(context).load(user.photoURL).into(myHolder.ivAvatar);
            myHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestParams requestParams = new RequestParams();
                    requestParams.put("follow_status", 1);
                    String endPoint = String.format(API.FOLLOW_USER, user.userID);
                    WebServiceManager.postWithToken(context, endPoint, requestParams, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFollowerArray.size();
        }
    }
}
