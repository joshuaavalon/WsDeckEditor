package com.joshuaavalon.wsdeckeditor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;


public class PreferenceRepository {

    public static boolean getHideNormal(@NonNull final Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.pref_hide_normal), true);
    }

    public static int getShowLimit(@NonNull final Context context) {
        return getSharedPreferences(context).getInt(context.getString(R.string.pref_show_limit), 200);
    }

    public static boolean getAutoClose(@NonNull final Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.pref_auto_close), true);
    }


    private static SharedPreferences getSharedPreferences(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
