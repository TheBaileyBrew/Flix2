package com.thebaileybrew.flix2;

import android.app.Application;

public class FlixApplication extends Application {

    private static FlixApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static FlixApplication getContext() {
        return mContext;
    }
}
