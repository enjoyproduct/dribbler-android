package com.mg.dribbler.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.enums.SignupType;
import com.mg.dribbler.models.User;
import com.mg.dribbler.services.APIServiceManager;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.BitmapUtility;
import com.mg.dribbler.utils.ErrorUtil;
import com.mg.dribbler.utils.MediaPickUtil;
import com.mg.dribbler.utils.PermissionUtil;
import com.mg.dribbler.utils.SelectDateFragment;
import com.mg.dribbler.utils.SharedPrefUtil;
import com.mg.dribbler.utils.UIUtil;
import com.mg.dribbler.views.MyCircularImageView;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

public class SignupActivity extends AppCompatActivity {

    private SignupType signupType;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvBirthday;
    private TextView tvGender;
    private Button btnRegister;
    private MyCircularImageView ivPhoto;
    private String avatarPath;
    private int gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        PermissionUtil.checkPermissions(this);

        signupType = (SignupType) getIntent().getExtras().get("SignUpType");

        etFirstName = (EditText) findViewById(R.id.et_firstname);
        etLastName = (EditText) findViewById(R.id.et_lastname);
        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        ivPhoto = (MyCircularImageView) findViewById(R.id.iv_photo);
        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImageDialog.build(new PickSetup())
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                if (r.getError() == null) {
                                    ivPhoto.setImageBitmap(r.getBitmap());
                                    avatarPath = r.getPath();
                                }
                            }
                        }).show(getSupportFragmentManager());
            }
        });
        tvBirthday = (TextView) findViewById(R.id.tv_birthday);
        tvBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectDateFragment selectDateFragment = new SelectDateFragment(tvBirthday);
                selectDateFragment.show(getSupportFragmentManager(), "Birthday");
            }
        });
        tvGender = (TextView) findViewById(R.id.tv_gender);
        tvGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(SignupActivity.this)
                        .title("Select Gender")
                        .items(R.array.Genders)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                gender = which;
                                tvGender.setText(text);
                                return true;
                            }
                        })
                        .positiveText(R.string.choose)
                        .show();
            }
        });

        initUI();
    }

    private void register() {
        if (!checkValidation()) {
            return;
        }

        RequestParams params = new RequestParams();
        try {
            params.put("email", etEmail.getText().toString());
            params.put("password", etPassword.getText().toString());
            params.put("first_name", etFirstName.getText().toString());
            params.put("last_name", etLastName.getText().toString());
            params.put("gender", gender);
            params.put("birthday", tvBirthday.getText().toString());
            params.put("photo", new File(avatarPath));
        } catch (Exception exception) {
            UIUtil.showToast(this, "Can't open avatar file");
            return;
        }

        if (signupType == SignupType.FacebookSignup) {
            params.put("facebook_id", User.currentUser().facebook_id);
        } else if (signupType == SignupType.GoogleSingup) {
            params.put("google_id", User.currentUser().google_id);
        }

        // Call API
        UIUtil.showProgressDialog(this, "Register...");
        WebServiceManager.post(this, API.REGISTER, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                ParseServiceManager.parseUserResponse(response);
                APIServiceManager.getTags(SignupActivity.this);
                UIUtil.dismissProgressDialog(SignupActivity.this);
                SharedPrefUtil.saveBoolean(SignupActivity.this, AppConstant.PREF_IS_SHOW_TUTORIALS, true);

                // Save login status
                SharedPrefUtil.saveString(SignupActivity.this, AppConstant.PREF_LOGIN_TYPE, "Basic");
                SharedPrefUtil.saveString(SignupActivity.this, AppConstant.PREF_CREDENTIAL_EMAIL, etEmail.getText().toString());
                SharedPrefUtil.saveString(SignupActivity.this, AppConstant.PREF_CREDENTIAL_PASSWORD, etPassword.getText().toString());

                if (User.currentUser().isVerified) {
                    goMainActivity();
                } else {
                    UIUtil.showAlertDialog(SignupActivity.this, "Welcome", "We have sent email to verify your email. After verify email, log in the app.", "OK", new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                UIUtil.dismissProgressDialog(SignupActivity.this);
                String error = WebServiceManager.getErrorMesssage(errorResponse);
                UIUtil.showAlertDialog(SignupActivity.this, error, "", "OK");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                UIUtil.dismissProgressDialog(SignupActivity.this);
                ErrorUtil.showInternalServerError(SignupActivity.this);
            }
        });
    }

    private boolean checkValidation() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String firstname = etFirstName.getText().toString();
        String lastname = etLastName.getText().toString();
        String gender = tvGender.getText().toString();
        String birthday = tvBirthday.getText().toString();

        if (TextUtils.isEmpty(firstname)) {
            UIUtil.showToast(this, "First name is required");
            return false;
        }
        if (TextUtils.isEmpty(lastname)) {
            UIUtil.showToast(this, "Last name is required");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            UIUtil.showToast(this, "email is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            UIUtil.showToast(this, "Invalid email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            UIUtil.showToast(this, "Password is required");
            return false;
        }
        if (password.length() < 5) {
            UIUtil.showToast(this, "Password must be at least 5 characters");
            return false;
        }
        if (TextUtils.isEmpty(gender)) {
            UIUtil.showToast(this, "Gender is required");
            return false;
        }
        if (TextUtils.isEmpty(birthday)) {
            UIUtil.showToast(this, "Birthday is required");
            return false;
        }
        if (avatarPath == null) {
            UIUtil.showToast(this, "Avatar is required");
            return false;
        }

        return true;
    }

    private void goMainActivity() {
        // Save status to show tutorial pages
        SharedPrefUtil.saveBoolean(this, AppConstant.PREF_IS_SHOW_TUTORIALS, true);
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void initUI() {
        if (signupType != SignupType.CommonSignup) {
            etFirstName.setText(User.currentUser().first_name);
            etLastName.setText(User.currentUser().last_name);
            etEmail.setText(User.currentUser().email);
            tvGender.setText(User.currentUser().gender == 0 ? "Male" : "Female");
            tvBirthday.setText(User.currentUser().birthday);
            if (User.currentUser().photoURL != null) {
                //Glide.with(this).load(User.currentUser().photoURL).into(ivPhoto);
                Glide.with(getApplicationContext())
                     .load(User.currentUser().photoURL)
                     .asBitmap()
                     .into(new SimpleTarget<Bitmap>(100, 100) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                ivPhoto.setImageBitmap(resource);
                                avatarPath =  BitmapUtility.saveBitmap(resource, MediaPickUtil.MEDIA_PATH, "dribbler_avatar");
                            }
                     });
            }
            UIUtil.showAlert(this, "Dribbler", "Complete your data", "OK");
        }
    }
}
