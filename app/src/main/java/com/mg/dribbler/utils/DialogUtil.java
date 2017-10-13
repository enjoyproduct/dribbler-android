package com.mg.dribbler.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Tag;
import com.mg.dribbler.models.Trick;
import com.mg.dribbler.services.APIServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


/**
 * Created by KCB on 27/04/17.
 */

public class DialogUtil {

    public static Dialog mDialog;

    public static void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public static boolean isShowing() {
        if (mDialog != null)
            return mDialog.isShowing();
        return false;
    }

    public static void showDialog() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    public static void showTrickTryOnDialog(final Context context) {
        dismissDialog();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mDialog = new Dialog(context, R.style.CustomDialog);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(true);

        View layout = inflater.inflate(R.layout.dialog_trick_try, null);
        final NumberPicker picker = (NumberPicker) layout.findViewById(R.id.number_picker);
        layout.findViewById(R.id.tv_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int try_on = picker.getValue();
                RequestParams params = new RequestParams();
                params.put("try_on", try_on);
                params.put("trick_id", Global.sharedInstance().selectedTrick.trick_id);
                APIServiceManager.postDribbler(context, params);

                dismissDialog();
            }
        });
        layout.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });

        mDialog.addContentView(layout, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
        showDialog();
    }
}
