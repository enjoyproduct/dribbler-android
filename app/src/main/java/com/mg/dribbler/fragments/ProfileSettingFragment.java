package com.mg.dribbler.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.activities.StartActivity;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.SharedPrefUtil;
import com.mg.dribbler.views.MyCircularImageView;

import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileSettingFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    private View contentView;
    private MainActivity mActivity;
    private FragmentManager fragmentManager;

    Switch swPush;
    Switch swFacebook;
    Switch swGoogle;
    Switch swVideo;

    private static final int RC_SIGN_IN = 015;
    private LoginManager mFBLoginManager;
    private CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;


    public ProfileSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_profile_setting, container, false);
        mActivity = (MainActivity) getActivity();
        fragmentManager = getFragmentManager();

        MyCircularImageView imageView = (MyCircularImageView) contentView.findViewById(R.id.iv_avatar);
        Glide.with(mActivity).load(User.currentUser().photoURL).into(imageView);

        contentView.findViewById(R.id.rl_personal_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPersonalInfo();
            }
        });
        contentView.findViewById(R.id.rl_followr_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserList();
            }
        });
        contentView.findViewById(R.id.rl_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHelpAndSupport();
            }
        });
        contentView.findViewById(R.id.rl_data_protection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDataProtection();
            }
        });
        contentView.findViewById(R.id.rl_terms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTermsAndService();
            }
        });
        contentView.findViewById(R.id.ll_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        contentView.findViewById(R.id.ll_delete_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProfile();
            }
        });

        // Instantiate switches
        swPush = (Switch) contentView.findViewById(R.id.sw_push);
        swFacebook = (Switch) contentView.findViewById(R.id.sw_fb);
        swGoogle = (Switch) contentView.findViewById(R.id.sw_gplus);
        swVideo = (Switch) contentView.findViewById(R.id.sw_video);
        swPush.setChecked(User.currentUser().isPushEnable);
//        swFacebook.setChecked(User.currentUser().facebook_id.equals("") ? false : true);
//        swGoogle.setChecked(User.currentUser().google_id.equals("") ? false : true);
        swFacebook.setChecked(User.currentUser().isFacebookEnable);
        swGoogle.setChecked(User.currentUser().isGoogleEnable);
        swVideo.setChecked(User.currentUser().isHighVideoEnable);
        swPush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RequestParams params = new RequestParams();
                params.put("push_enable", isChecked == true ? 1 : 0);
                updateProfile(params);
            }
        });
        swFacebook.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    doFBAuth();
                } else {
                    RequestParams params = new RequestParams();
                    params.put("facebook_id", "");
                    updateProfile(params);
                }
            }
        });
        swGoogle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    doGoogleAuth();
                } else {
                    RequestParams params = new RequestParams();
                    params.put("google_id", "");
                    updateProfile(params);
                }
            }
        });
        swVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RequestParams params = new RequestParams();
                params.put("high_video_enable", isChecked == true ? 1 : 0);
                updateProfile(params);
            }
        });

        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN && resultCode ==  Activity.RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            final GoogleSignInAccount acct = result.getSignInAccount();

            RequestParams params = new RequestParams();
            params.put("google_id", acct.getId());
            updateProfile(params);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void doGoogleAuth() {
        // Instantiate Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .enableAutoManage(mActivity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void doFBAuth() {
        // Instantiate Facebook
        FacebookSdk.sdkInitialize(mActivity.getApplicationContext());
        mFBLoginManager = LoginManager.getInstance();

        mFBLoginManager.logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        mCallbackManager = CallbackManager.Factory.create();
        mFBLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String fbID = loginResult.getAccessToken().getUserId();
                RequestParams params = new RequestParams();
                params.put("facebook_id", fbID);
                updateProfile(params);
            }

            @Override
            public void onCancel() {
                Log.d("facebook-login", "canceled");
            }

            @Override
            public void onError(FacebookException e) {
                Log.d("facebook-login", e.getMessage());
            }
        });
    }

    /**
     * Cell Click Event
     */
    public void onPersonalInfo() {
        ProfileEditFragment fragment = new ProfileEditFragment();
        pushFragment(fragment);
    }

    public void onUserList() {
        UserListFragment fragment = new UserListFragment(User.currentUser());
        pushFragment(fragment);
    }

    public void onHelpAndSupport() {
        FAGFragment fragment = new FAGFragment();
        pushFragment(fragment);
    }

    public void onTermsAndService() {
        TosFragment fragment = new TosFragment(0);
        pushFragment(fragment);
    }

    public void onDataProtection() {
        TosFragment fragment = new TosFragment(1);
        pushFragment(fragment);
    }

    private void pushFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.addToBackStack(fragment.toString());
        transaction.add(R.id.container, fragment).commit();
    }

    public void logout() {
        Global.sharedInstance().init();
        mActivity.finish();
        SharedPrefUtil.saveString(mActivity, AppConstant.PREF_LOGIN_TYPE, "None");
        User.clearCurrentUser();
        Intent intent = new Intent(mActivity, StartActivity.class);
        startActivity(intent);
    }

    public void deleteProfile() {
        mActivity.finish();
        SharedPrefUtil.saveString(mActivity, AppConstant.PREF_LOGIN_TYPE, "None");
        User.clearCurrentUser();
        Intent intent = new Intent(mActivity, StartActivity.class);
        startActivity(intent);
    }

    /**
     * API - Update Profile
     */
    private void updateProfile(RequestParams params) {
        String endPoint = API.UPDATE_MY_PROFILE;
        WebServiceManager.postWithToken(mActivity, endPoint, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                ParseServiceManager.parseUserResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }
}
