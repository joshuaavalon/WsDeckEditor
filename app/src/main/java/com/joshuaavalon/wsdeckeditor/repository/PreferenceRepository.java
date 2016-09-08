package com.joshuaavalon.wsdeckeditor.repository;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.model.Card;

public class PreferenceRepository {
    private static final String SORT_ORDER_KEY = "sortOrder";
    private static final String AUTO_SAVE_KEY =
            WsApplication.getContext().getString(R.string.pref_auto_save);
    private static final String AUTO_CLOSE_KEY =
            WsApplication.getContext().getString(R.string.pref_auto_close);
    private static final String HIDE_NORMAL_KEY =
            WsApplication.getContext().getString(R.string.pref_hide_normal);

    public static Card.SortOrder getSortOrder() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WsApplication.getContext());
        return Card.SortOrder.fromInt(preferences.getInt(SORT_ORDER_KEY, Card.SortOrder.Detail.toInt()));
    }

    public static void setSortOrder(Card.SortOrder order) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WsApplication.getContext());
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SORT_ORDER_KEY, order.toInt());
        editor.apply();
    }

    public static boolean getAutoSave() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WsApplication.getContext());
        return preferences.getBoolean(AUTO_SAVE_KEY, true);
    }

    public static boolean getHideNormal() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WsApplication.getContext());
        return preferences.getBoolean(HIDE_NORMAL_KEY, true);
    }

    public static boolean getAutoClose() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WsApplication.getContext());
        return preferences.getBoolean(AUTO_CLOSE_KEY, true);
    }
}
