package com.joshuaavalon.wsdeckeditor;

import android.app.Application;
import android.content.Context;

public class WsApplication extends Application {
    public static final String QR_SCHEME = "wsde";
    public static final int SINGLE_CARD_LIMIT = 50;
    public static final int CARD_TYPE_LIMIT = 60;
    private static WsApplication instance;

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

}