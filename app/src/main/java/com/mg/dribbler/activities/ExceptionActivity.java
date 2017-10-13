package com.mg.dribbler.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.mg.dribbler.R;


public class ExceptionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception);

        Bundle extras = getIntent().getExtras();
        String error = extras.getString("error");

        TextView textView = (TextView) findViewById(R.id.tvError);
        assert textView != null;
        textView.setText(error);
    }
    
}
