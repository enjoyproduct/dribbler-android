package com.mg.dribbler.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mg.dribbler.activities.ForgotPasswordActivity;
import com.mg.dribbler.activities.SignupActivity;
import com.mg.dribbler.application.DribblerApplication;


public class UIUtil {

    private static Toast mToast;

    public static void hideKeyboard(Context context, EditText view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showShortToast(String message) {
        if (mToast == null) {
            mToast = Toast.makeText(DribblerApplication.getInstance(), message, Toast.LENGTH_SHORT);
        }
        mToast.setText(message);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void showLongToast(Context context, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        }
        mToast.setText(message);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void spanString(TextView view, String fulltext, String subtext, int color) {
        if (TextUtils.isEmpty(fulltext) || TextUtils.isEmpty(subtext))
            return;
        Spannable str = (Spannable) view.getText();
        int i = fulltext.toLowerCase().indexOf(subtext.toLowerCase());
        if (i > -1)
            str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void spanStringAndUnderline(TextView view, String fulltext, String subtext, int color, View.OnClickListener listener) {
        if (TextUtils.isEmpty(fulltext) || TextUtils.isEmpty(subtext))
            return;
        Spannable str = (Spannable) view.getText();
        int i = fulltext.toLowerCase().indexOf(subtext.toLowerCase());
        if (i > -1)
            str.setSpan(new ColorAndUnderlineSpan(color, listener), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void spanString(TextView view, String fulltext, String subtext, int color, View.OnClickListener listener) {
        if (TextUtils.isEmpty(fulltext) || TextUtils.isEmpty(subtext))
            return;
        Spannable str = (Spannable) view.getText();
        int i = fulltext.toLowerCase().indexOf(subtext.toLowerCase());
        if (i > -1)
            str.setSpan(new ColorSpan(color, listener), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static class ColorAndUnderlineSpan extends ClickableSpan {// extend ClickableSpan

        private int color;
        private View.OnClickListener listener;

        public ColorAndUnderlineSpan(int color, View.OnClickListener listener) {
            this.color = color;
            this.listener = listener;
        }

        @Override
        public void onClick(View widget) {
            if (listener != null)
                listener.onClick(widget);
        }

        public void updateDrawState(TextPaint ds) {// override updateDrawState
            ds.setUnderlineText(true); // set to false to remove underline
            ds.setColor(color);
        }
    }

    public static class ColorSpan extends ClickableSpan {// extend ClickableSpan

        private int color;
        private View.OnClickListener listener;

        public ColorSpan(int color, View.OnClickListener listener) {
            this.color = color;
            this.listener = listener;
        }

        @Override
        public void onClick(View widget) {
            if (listener != null)
                listener.onClick(widget);
        }

        public void updateDrawState(TextPaint ds) {// override updateDrawState
            ds.setColor(color);
            ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
    }

    public static void spanStringBackground(TextView view, String fulltext, String subtext, int color) {
        if (TextUtils.isEmpty(fulltext) || TextUtils.isEmpty(subtext))
            return;
        Spannable str = (Spannable) view.getText();
        int i = fulltext.toLowerCase().indexOf(subtext.toLowerCase());
        if (i > -1)
            str.setSpan(new BackgroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void showMessageDialog(Context context, String title, String message, String btnTitle) {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(btnTitle, null);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public static void showAlertDialog(Context context, String title, String message, String btnTitle, MaterialDialog.SingleButtonCallback callback) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(message)
                .positiveText(btnTitle)
                .onPositive(callback)
                .show();
    }

    public static void showAlertDialog(Context context, String title, String message, String btnTitle) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(message)
                .positiveText(btnTitle)
                .show();
    }

    public static void showAlert(Context context, String title, String message, String positiveButtonTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (title != null && !title.equals("")) {
            builder.setTitle(title);
        }
        if (message != null && !message.equals("")) {
            builder.setMessage(message);
        }
        builder.setCancelable(true);

        builder.setPositiveButton(
                positiveButtonTitle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlert(Context context, String title, String message, String positiveButtonTitle, final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (title != null && !title.equals("")) {
            builder.setTitle(title);
        }
        if (message != null && !message.equals("")) {
            builder.setMessage(message);
        }
        builder.setCancelable(true);

        builder.setPositiveButton(
                positiveButtonTitle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        listener.onClick(dialog, id);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlert(Context context, String title, String message, String positiveButtonTitle, final DialogInterface.OnClickListener positiveListener, String negativeButtonTitle, final DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (title != null && !title.equals("")) {
            builder.setTitle(title);
        }
        if (message != null && !message.equals("")) {
            builder.setMessage(message);
        }
        builder.setCancelable(true);

        builder.setPositiveButton(
                positiveButtonTitle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        positiveListener.onClick(dialog, id);
                    }
                });

        builder.setNegativeButton(
                negativeButtonTitle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        negativeListener.onClick(dialog, id);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showSingleChoiceDialog(Context context, String title,
                                              String[] data, int checkedItem,
                                              DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new Builder(context);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setSingleChoiceItems(data, checkedItem, listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void setDrawable(View view, Drawable drawable) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }


    /**
     * Progress Hud
     */
    private static ProgressDialog dialogProgress;

    public static void dismissProgressDialog(Context mContext) {
        if (dialogProgress != null && dialogProgress.isShowing()) {
            dialogProgress.dismiss();
        }
    }

    public static void showProgressDialog(Context context, String title) {
        if (context != null) {
            dialogProgress = new ProgressDialog(context);
            if(dialogProgress.getWindow()!=null)
                dialogProgress.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dialogProgress.setMessage(title);
            if (!dialogProgress.isShowing()) {
                try {
                    dialogProgress.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            dialogProgress.setIndeterminate(false);
            dialogProgress.setCancelable(true);
        }
    }
}
