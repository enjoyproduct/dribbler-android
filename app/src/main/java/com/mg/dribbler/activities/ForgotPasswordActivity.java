package com.mg.dribbler.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mg.dribbler.R;
import com.mg.dribbler.services.WebServiceManager;
import com.mg.dribbler.utils.API;
import com.mg.dribbler.utils.ErrorUtil;
import com.mg.dribbler.utils.UIUtil;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * A login screen that offers login via email/password.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        etEmail = (EditText) findViewById(R.id.et_email);
        btnSendEmail = (Button) findViewById(R.id.btn_send);
        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.hideKeyboard(ForgotPasswordActivity.this, etEmail);
                onForgotPassword();
            }
        });
    }


    private void onForgotPassword() {
        if (!checkValidation()) {
            return;
        }

        RequestParams params = new RequestParams();
        params.put("email", etEmail.getText().toString());

        // Call API
        UIUtil.showProgressDialog(this, "");
        WebServiceManager.post(this, API.FORGOT_PASSWORD, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                UIUtil.dismissProgressDialog(ForgotPasswordActivity.this);
                UIUtil.showAlertDialog(ForgotPasswordActivity.this, "Success", "Have sent email for reset password", "OK", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                });
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                UIUtil.dismissProgressDialog(ForgotPasswordActivity.this);
                String error = WebServiceManager.getErrorMesssage(errorResponse);
                UIUtil.showAlertDialog(ForgotPasswordActivity.this, "Error", error, "OK");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                UIUtil.dismissProgressDialog(ForgotPasswordActivity.this);
                ErrorUtil.showInternalServerError(ForgotPasswordActivity.this);
            }
        });
    }

    private boolean checkValidation() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            UIUtil.showToast(this, getString(R.string.msg_err_not_enough_info));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            UIUtil.showToast(this, "Invalid email");
            return false;
        }

        return true;
    }
}

