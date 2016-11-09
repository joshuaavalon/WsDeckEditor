package com.joshuaavalon.wsdeckeditor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;


public class PreferenceRepository {
    public static void setFirstTime(@NonNull final Context context, final boolean bool) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(context.getString(R.string.pref_is_first), bool);
        editor.apply();
    }

    public static boolean getFirstTime(@NonNull final Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.pref_is_first), true);
    }

    public static void setSwipeRemove(@NonNull final Context context, final boolean bool) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(context.getString(R.string.pref_swipe_remove), bool);
        editor.apply();
    }

    public static boolean getSwipeRemove(@NonNull final Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.pref_swipe_remove), false);
    }

    public static boolean getHideNormal(@NonNull final Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.pref_hide_normal), true);
    }

    public static int getShowLimit(@NonNull final Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(context.getString(R.string.pref_show_limit), "200"));
    }

    public static boolean getAutoClose(@NonNull final Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.pref_auto_close), true);
    }

    public static boolean getAddIfNotExist(@NonNull final Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.pref_add_if_not_exist), false);
    }

    private static SharedPreferences getSharedPreferences(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static CardOrder getSortOrder(@NonNull final Context context) {
        return CardOrder.values()[getSharedPreferences(context).getInt(context.getString(R.string.pref_sort_order), 0)];
    }

    public static void setSortOrder(@NonNull final Context context, @NonNull final CardOrder order) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(context.getString(R.string.pref_sort_order), order.ordinal());
        editor.apply();
    }

    public static long getSelectedDeck(@NonNull final Context context) {
        return getSharedPreferences(context).getLong(context.getString(R.string.pref_deck_id), -1);
    }

    public static void setSelectedDeck(@NonNull final Context context, final long id) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(context.getString(R.string.pref_deck_id), id);
        editor.apply();
    }
}
