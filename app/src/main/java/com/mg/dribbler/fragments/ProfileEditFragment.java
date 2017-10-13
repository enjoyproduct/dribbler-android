package com.mg.dribbler.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.activities.SignupActivity;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.SelectDateFragment;
import com.mg.dribbler.utils.UIUtil;
import com.mg.dribbler.views.MyCircularImageView;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;


public class ProfileEditFragment extends Fragment {

    private View contentView;
    private FragmentManager fragmentManager;
    private MainActivity mActivity;

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private TextView tvGender;
    private TextView tvBirthday;
    private EditText etCurrentPW;
    private EditText etNewPW;
    private EditText etConfirmPW;
    private MyCircularImageView ivAvatar;
    private String avatarPath;
    private Button btnSave;
    private int gender;


    public ProfileEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contentView = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        mActivity = (MainActivity) getActivity();
        fragmentManager = getFragmentManager();

        User me = User.currentUser();
        etFirstName = (EditText) contentView.findViewById(R.id.et_firstname);
        etFirstName.setText(me.first_name);
        etLastName = (EditText) contentView.findViewById(R.id.et_lastname);
        etLastName.setText(me.last_name);
        etEmail = (EditText) contentView.findViewById(R.id.et_email);
        etEmail.setText(me.email);
        tvGender = (TextView) contentView.findViewById(R.id.tv_gender);
        tvGender.setText(me.gender == 0 ? getResources().getStringArray(R.array.Genders)[0] : getResources().getStringArray(R.array.Genders)[1]);
        gender = me.gender;
        tvGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(mActivity)
                        .title("Select Gender")
                        .items(R.array.Genders)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                tvGender.setText(text);
                                gender = which;
                                return true;
                            }
                        })
                        .positiveText(R.string.choose)
                        .show();
            }
        });
        tvBirthday = (TextView) contentView.findViewById(R.id.tv_birthday);
        tvBirthday.setText(me.birthday);
        tvBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectDateFragment selectDateFragment = new SelectDateFragment(tvBirthday);
                selectDateFragment.show(fragmentManager, "Birthday");
            }
        });
        etCurrentPW = (EditText) contentView.findViewById(R.id.et_current_password);
        etNewPW = (EditText) contentView.findViewById(R.id.et_new_password);
        etConfirmPW = (EditText) contentView.findViewById(R.id.et_confirm_password);
        ivAvatar = (MyCircularImageView) contentView.findViewById(R.id.iv_avatar);
        Glide.with(mActivity).load(me.photoURL).into(ivAvatar);
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImageDialog.build(new PickSetup())
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                if (r.getError() == null) {
                                    ivAvatar.setImageBitmap(r.getBitmap());
                                    avatarPath = r.getPath();
                                }
                            }
                        }).show(fragmentManager);
            }
        });
        btnSave = (Button) contentView.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        return contentView;
    }

    private boolean checkValidation() {
        String email = etEmail.getText().toString().trim();
        String password = etConfirmPW.getText().toString();
        String newPassword = etNewPW.getText().toString();
        String confirmPassword = etConfirmPW.getText().toString();
        String firstname = etFirstName.getText().toString();
        String lastname = etLastName.getText().toString();
        String gender = tvGender.getText().toString();
        String birthday = tvBirthday.getText().toString();

        if (TextUtils.isEmpty(firstname)) {
            UIUtil.showToast(mActivity, "First name is required");
            return false;
        }
        if (TextUtils.isEmpty(lastname)) {
            UIUtil.showToast(mActivity, "Last name is required");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            UIUtil.showToast(mActivity, "email is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            UIUtil.showToast(mActivity, "Invalid email");
            return false;
        }
        if (!newPassword.equals("")) {
            if (TextUtils.isEmpty(password)) {
                UIUtil.showToast(mActivity, "Password is required");
                return false;
            }
            if (newPassword.length() < 5) {
                UIUtil.showToast(mActivity, "Password must be at least 5 characters");
                return false;
            }
            if (!newPassword.equals(confirmPassword)) {
                UIUtil.showToast(mActivity, "New Password is not match");
                return false;
            }
        }
        if (TextUtils.isEmpty(gender)) {
            UIUtil.showToast(mActivity, "Gender is required");
            return false;
        }
        if (TextUtils.isEmpty(birthday)) {
            UIUtil.showToast(mActivity, "Birthday is required");
            return false;
        }

        return true;
    }

    private void updateProfile() {
        if (!checkValidation()) {
            return;
        }

        RequestParams params = new RequestParams();

        try {
            params.put("email", etEmail.getText().toString());
            params.put("password", etCurrentPW.getText().toString());
            params.put("first_name", etFirstName.getText().toString());
            params.put("last_name", etLastName.getText().toString());
            params.put("gender", gender);
            params.put("birthday", tvBirthday.getText().toString());
            if (avatarPath != null) {
                params.put("photo", new File(avatarPath));
            }
        } catch (Exception exception) {
            UIUtil.showToast(mActivity, "Can't open avatar file");
            return;
        }

        // Call API
        UIUtil.showProgressDialog(mActivity, "Updating Profile...");
        WebServiceManager.postWithToken(mActivity, API.UPDATE_MY_PROFILE, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                UIUtil.dismissProgressDialog(mActivity);
                ParseServiceManager.parseUserResponse(response);
                UIUtil.showToast(mActivity, "Profile is updated successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
}
