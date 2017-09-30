package com.mg.dribbler.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.services.APIServiceManager;
import com.mg.dribbler.services.ParseServiceManager;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.AppConstant;
import com.mg.dribbler.utils.SharedPrefUtil;
import com.mg.dribbler.utils.UIUtil;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SigninAcitivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvForgot;
    private String mEmail, mPassword, verifyCode = "";
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);
        tvForgot = (TextView) findViewById(R.id.tv_forgot);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninAcitivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }


    private void login() {
        if (!checkValidation()) {
            return;
        }

        RequestParams params = new RequestParams();
        params.put("email", mEmail);
        params.put("password", mPassword);

        // Call API
        UIUtil.showProgressDialog(this, "Login...");
        WebServiceManager.post(this, API.LOGIN, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                ParseServiceManager.parseUserResponse(response);
                APIServiceManager.getTags(SigninAcitivity.this);
                UIUtil.dismissProgressDialog(SigninAcitivity.this);
                goMainActivity();

                // Save login status
                SharedPrefUtil.saveString(SigninAcitivity.this, AppConstant.PREF_LOGIN_TYPE, "Basic");
                SharedPrefUtil.saveString(SigninAcitivity.this, AppConstant.PREF_CREDENTIAL_EMAIL, mEmail);
                SharedPrefUtil.saveString(SigninAcitivity.this, AppConstant.PREF_CREDENTIAL_PASSWORD, mPassword);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                UIUtil.dismissProgressDialog(SigninAcitivity.this);
                String error = WebServiceManager.getErrorMesssage(errorResponse);
                UIUtil.showAlertDialog(SigninAcitivity.this, error, "", "OK");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                UIUtil.dismissProgressDialog(SigninAcitivity.this);
                if (statusCode == 401) {//user is not verified yet
                    inputVerifyCode();
                }
            }
        });
    }

    private boolean checkValidation() {
        mEmail = etEmail.getText().toString().trim();
        mPassword = etPassword.getText().toString();

        if (TextUtils.isEmpty(mEmail) || TextUtils.isEmpty(mPassword)) {
            UIUtil.showToast(this, getString(R.string.msg_err_not_enough_info));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            UIUtil.showToast(this, "Invalid email");
            return false;
        }

        return true;
    }

    private void goMainActivity() {
        Intent intent = new Intent(SigninAcitivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void inputVerifyCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please insert verify code");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);
        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verifyCode = input.getText().toString();
                verify();
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(true);
        builder.show();
    }
    private void verify() {
        if (!checkValidation()) {
            return;
        }

        RequestParams params = new RequestParams();
        params.put("email", mEmail);
        params.put("confirmation_code", verifyCode);

        // Call API
        UIUtil.showProgressDialog(this, "Verifying...");
        WebServiceManager.post(this, API.VERIFY, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                UIUtil.dismissProgressDialog(SigninAcitivity.this);
                UIUtil.showAlertDialog(SigninAcitivity.this, "Dribbler", "Verification success! Please login.", "OK");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                UIUtil.dismissProgressDialog(SigninAcitivity.this);
                String error = WebServiceManager.getErrorMesssage(errorResponse);
                UIUtil.showAlertDialog(SigninAcitivity.this, error, "", "OK");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                UIUtil.dismissProgressDialog(SigninAcitivity.this);
            }
        });
    }
}
