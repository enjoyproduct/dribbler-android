package com.mg.dribbler.application;

import android.app.Application;
import android.content.Context;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


public class DribblerApplication extends Application {

    private static DribblerApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static DribblerApplication getInstance() {
        return mInstance;
    }
}
