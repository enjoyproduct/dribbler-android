package com.mg.dribbler.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


public class DribblerApplication extends Application {

    private static DribblerApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        // Handle Exception
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        mInstance = this;
        // Initialize the SDK before executing any other operations,
        MultiDex.install(this);
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
