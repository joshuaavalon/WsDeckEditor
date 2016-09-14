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
    private static final String SHOW_LIMIT_KEY =
            WsApplication.getContext().getString(R.string.pref_show_limit);
    private static final String POP_BACK_KEY =
            WsApplication.getContext().getString(R.string.pref_pop_back);

    public static Card.SortOrder getSortOrder() {
        return Card.SortOrder.fromInt(getSharedPreferences()
                .getInt(SORT_ORDER_KEY, Card.SortOrder.Detail.toInt()));
    }

    public static void setSortOrder(Card.SortOrder order) {
        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt(SORT_ORDER_KEY, order.toInt());
        editor.apply();
    }

    public static boolean getAutoSave() {
        return getSharedPreferences().getBoolean(AUTO_SAVE_KEY, true);
    }

    public static boolean getHideNormal() {
        return getSharedPreferences().getBoolean(HIDE_NORMAL_KEY, true);
    }

    public static int getShowLimit() {
        return Integer.valueOf(getSharedPreferences().getString(SHOW_LIMIT_KEY,
                WsApplication.getContext().getString(R.string.default_show_limit)));
    }

    public static boolean getAutoClose() {
        return getSharedPreferences().getBoolean(AUTO_CLOSE_KEY, true);
    }

    public static boolean getPopBack() {
        return getSharedPreferences().getBoolean(POP_BACK_KEY, true);
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(WsApplication.getContext());
    }
}
