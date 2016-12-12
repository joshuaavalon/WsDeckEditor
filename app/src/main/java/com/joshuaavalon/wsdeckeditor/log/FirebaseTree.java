package com.joshuaavalon.wsdeckeditor.log;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import timber.log.Timber;

public class FirebaseTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if(priority != Log.ERROR && priority != Log.ASSERT)
            return;
        FirebaseCrash.report(new Exception(String.format("[%s] %s", tag, message)));
    }
}
