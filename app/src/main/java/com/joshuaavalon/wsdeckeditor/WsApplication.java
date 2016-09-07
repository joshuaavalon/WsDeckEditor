package com.joshuaavalon.wsdeckeditor;

import android.app.Application;
import android.content.Context;

import com.orm.SugarContext;

public class WsApplication extends Application {
    public static final String QR_SCHEME = "wsde";
    private static WsApplication instance;

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
        instance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
        instance = null;
    }

}