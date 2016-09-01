package com.joshuaavalon.wsdeckeditor.repository;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.model.Card;

public class PreferenceRepository {
    private static final String SORT_ORDER_KEY = "sortOrder";

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
}
