package com.joshuaavalon.wsdeckeditor.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.R;

public class PreferenceRepository {
    private final String KEY_FIRST_TIME;
    private final String KEY_SWIPE_REMOVE;
    private final String KEY_HIDE_NORMAL;
    private final String KEY_SHOW_LIMIT;
    private final String KEY_AUTO_CLOSE;
    private final String KEY_ADD_IF_NOT_EXIST;
    private final String KEY_SORT_ORDER;
    private final String KEY_DECK_ID;
    private final String KEY_QUICK_HISTORY;
    @NonNull
    private final SharedPreferences sharedPreferences;

    private PreferenceRepository(@NonNull final Context context,
                                 @NonNull final SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        KEY_FIRST_TIME = context.getString(R.string.pref_is_first);
        KEY_SWIPE_REMOVE = context.getString(R.string.pref_swipe_remove);
        KEY_HIDE_NORMAL = context.getString(R.string.pref_hide_normal);
        KEY_SHOW_LIMIT = context.getString(R.string.pref_show_limit);
        KEY_AUTO_CLOSE = context.getString(R.string.pref_auto_close);
        KEY_ADD_IF_NOT_EXIST = context.getString(R.string.pref_add_if_not_exist);
        KEY_SORT_ORDER = context.getString(R.string.pref_sort_order);
        KEY_DECK_ID = context.getString(R.string.pref_deck_id);
        KEY_QUICK_HISTORY = context.getString(R.string.pref_quick_search_history);
    }

    public static PreferenceRepository fromDefault(@NonNull final Context context) {
        return PreferenceRepository.from(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static PreferenceRepository from(@NonNull final Context context,
                                            @NonNull final SharedPreferences sharedPreferences) {
        return new PreferenceRepository(context, sharedPreferences);
    }

    public boolean getFirstTime() {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true);
    }

    public void setFirstTime(final boolean bool) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, bool);
        editor.apply();
    }

    public boolean getSwipeRemove() {
        return sharedPreferences.getBoolean(KEY_SWIPE_REMOVE, false);
    }

    public void setSwipeRemove(final boolean bool) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SWIPE_REMOVE, bool);
        editor.apply();
    }

    public boolean getHideNormal() {
        return sharedPreferences.getBoolean(KEY_HIDE_NORMAL, true);
    }

    public int getShowLimit() {
        return Integer.valueOf(sharedPreferences.getString(KEY_SHOW_LIMIT, "200"));
    }

    public boolean getAutoClose() {
        return sharedPreferences.getBoolean(KEY_AUTO_CLOSE, true);
    }

    public boolean getAddIfNotExist() {
        return sharedPreferences.getBoolean(KEY_ADD_IF_NOT_EXIST, false);
    }
    public boolean getEnableQuickSearchHistory() {
        return sharedPreferences.getBoolean(KEY_QUICK_HISTORY, true);
    }

    public CardOrder getSortOrder() {
        return CardOrder.values()[sharedPreferences.getInt(KEY_SORT_ORDER, 0)];
    }

    public void setSortOrder(@NonNull final CardOrder order) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_SORT_ORDER, order.ordinal());
        editor.apply();
    }

    public long getSelectedDeck() {
        return sharedPreferences.getLong(KEY_DECK_ID, -1);
    }

    public void setSelectedDeck(final long id) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_DECK_ID, id);
        editor.apply();
    }
}
