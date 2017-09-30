package com.mg.dribbler.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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
import com.mg.dribbler.application.ExceptionHandler;
import com.mg.dribbler.enums.SignupType;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.APIServiceManager;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.SharedPrefUtil;
import com.mg.dribbler.utils.UIUtil;
import org.json.JSONObject;
import java.util.Arrays;
import cz.msebera.android.httpclient.Header;

public class StartActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 005;
    private LoginManager mFBLoginManager;
    private CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;


    /**
     * Life Cycle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Handle Exception
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        // Check Previous Login Status and Login
        String loginStatus = SharedPrefUtil.loadString(StartActivity.this, AppConstant.PREF_LOGIN_TYPE, "None");
        if (!loginStatus.equals("None")) {
            RequestParams params = new RequestParams();
            if (loginStatus.equals("Facebook")) {
                String facebook_id = SharedPrefUtil.loadString(StartActivity.this, AppConstant.PREF_FACEBOOK_ID, "");
                params.put("facebook_id", facebook_id);
            } else if (loginStatus.equals("Google")) {
                String facebook_id = SharedPrefUtil.loadString(StartActivity.this, AppConstant.PREF_GOOGLE_ID, "");
                params.put("google_id", facebook_id);
            } else if (loginStatus.equals("Basic")) {
                String email = SharedPrefUtil.loadString(StartActivity.this, AppConstant.PREF_CREDENTIAL_EMAIL, "");
                String password = SharedPrefUtil.loadString(StartActivity.this, AppConstant.PREF_CREDENTIAL_PASSWORD, "");
                params.put("email", email);
                params.put("password", password);
            }

            autoLogin(params);
        }

        // Instantiate Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Instantiate Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        mFBLoginManager = LoginManager.getInstance();

        RelativeLayout googleBtn = (RelativeLayout) findViewById(R.id.rl_google);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGoogleLogin();
            }
        });

        RelativeLayout fbBtn = (RelativeLayout) findViewById(R.id.rl_facebook);
        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFacebookLogin();
            }
        });

        TextView loginTV = (TextView) findViewById(R.id.tvLogin);
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goLoginActivity();
            }
        });

        TextView registerTV = (TextView) findViewById(R.id.tvRegister);
        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSignUpActivity(SignupType.CommonSignup);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    /**
     * Private Methods
     */

    private void doGoogleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void doFacebookLogin() {
        if (AccessToken.getCurrentAccessToken() == null) {
            mFBLoginManager.logInWithReadPermissions(StartActivity.this, Arrays.asList("email", "public_profile"));
            mCallbackManager = CallbackManager.Factory.create();
            mFBLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFBSignInResult(loginResult.getAccessToken());
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
        } else {
            handleFBSignInResult(AccessToken.getCurrentAccessToken());
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();

            RequestParams params = new RequestParams();
            params.put("google_id", acct.getId());
            params.put("email", acct.getEmail());

            // Call API
            UIUtil.showProgressDialog(this, "Login...");
            WebServiceManager.post(this, API.LOGIN, params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    ParseServiceManager.parseUserResponse(response);
                    APIServiceManager.getTags(StartActivity.this);
                    UIUtil.dismissProgressDialog(StartActivity.this);
                    goMainActivity();

                    // Save login status
                    SharedPrefUtil.saveString(StartActivity.this, AppConstant.PREF_LOGIN_TYPE, "Google");
                    SharedPrefUtil.saveString(StartActivity.this, AppConstant.PREF_GOOGLE_ID, acct.getId());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    UIUtil.dismissProgressDialog(StartActivity.this);
                    collectGoogleProfile(acct);
                    goSignUpActivity(SignupType.GoogleSingup);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    UIUtil.dismissProgressDialog(StartActivity.this);
                }
            });
        }
    }

    private void handleFBSignInResult(final AccessToken token) {
        RequestParams params = new RequestParams();
        params.put("facebook_id", token.getUserId());

        // Call API
        UIUtil.showProgressDialog(this, "Login...");
        WebServiceManager.post(this, API.LOGIN, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                ParseServiceManager.parseUserResponse(response);
                APIServiceManager.getTags(StartActivity.this);
                UIUtil.dismissProgressDialog(StartActivity.this);
                goMainActivity();

                // Save login status
                SharedPrefUtil.saveString(StartActivity.this, AppConstant.PREF_LOGIN_TYPE, "Facebook");
                SharedPrefUtil.saveString(StartActivity.this, AppConstant.PREF_FACEBOOK_ID, token.getUserId());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                UIUtil.dismissProgressDialog(StartActivity.this);
                collectFBProfile(token);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                UIUtil.dismissProgressDialog(StartActivity.this);
            }
        });
    }

    private void collectFBProfile(final AccessToken token) {
        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            User me = User.currentUser();
                            me.email = object.getString("email");
                            me.first_name = object.getString("first_name");
                            me.last_name = object.getString("last_name");
                            me.facebook_id = token.getUserId();
                            me.gender = object.getString("gender").equals("male") ? 0 : 1;
                            me.photoURL = "https://graph.facebook.com/" + token.getUserId() + "/picture?type=normal";
                        } catch (Exception exception) {
                        }

                        goSignUpActivity(SignupType.FacebookSignup);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email, gender, birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void collectGoogleProfile(GoogleSignInAccount account) {
        User user = User.currentUser();
        user.google_id = account.getId();
        user.first_name = account.getGivenName();
        user.last_name = account.getFamilyName();
        user.email = account.getEmail();
        user.photoURL = (account.getPhotoUrl() == null ? "" : account.getPhotoUrl().toString());
    }

    private void autoLogin(RequestParams params) {
        UIUtil.showProgressDialog(this, "Login...");
        WebServiceManager.post(this, API.LOGIN, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                UIUtil.dismissProgressDialog(StartActivity.this);
                ParseServiceManager.parseUserResponse(response);
                APIServiceManager.getTags(StartActivity.this);
                goMainActivity();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                UIUtil.dismissProgressDialog(StartActivity.this);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                UIUtil.dismissProgressDialog(StartActivity.this);
            }
        });
    }

    /**
     * Navigation Methods
     */

    private void goLoginActivity() {
        Intent intent = new Intent(StartActivity.this, SigninAcitivity.class);
        startActivity(intent);
        finish();
    }

    private void goSignUpActivity(SignupType type) {
        Intent intent = new Intent(StartActivity.this, SignupActivity.class);
        intent.putExtra("SignUpType", type);
        startActivity(intent);
        finish();
    }

    private void goMainActivity() {
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
