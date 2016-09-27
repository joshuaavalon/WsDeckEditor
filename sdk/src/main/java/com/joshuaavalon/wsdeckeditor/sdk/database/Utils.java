package com.joshuaavalon.wsdeckeditor.sdk.database;


import android.net.Uri;
import android.support.annotation.NonNull;

class Utils {
    public static String getImageNameFromUrl(@NonNull final String url) {
        return Uri.parse(url).getLastPathSegment();
    }
}
