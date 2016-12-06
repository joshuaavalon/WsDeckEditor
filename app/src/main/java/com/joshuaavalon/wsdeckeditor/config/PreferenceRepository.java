package com.joshuaavalon.wsdeckeditor.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.exclude.CardOrder;

import butterknife.BindString;

public class PreferenceRepository {
    @BindString(R.string.pref_is_first)
    String KEY_FIRST_TIME;
    @BindString(R.string.pref_swipe_remove)
    String KEY_SWIPE_REMOVE;
    @BindString(R.string.pref_hide_normal)
    String KEY_HIDE_NORMAL;
    @BindString(R.string.pref_show_limit)
    String KEY_SHOW_LIMIT;
    @BindString(R.string.pref_auto_close)
    String KEY_AUTO_CLOSE;
    @BindString(R.string.pref_add_if_not_exist)
    String KEY_ADD_IF_NOT_EXIST;
    @BindString(R.string.pref_sort_order)
    String KEY_SORT_ORDER;
    @BindString(R.string.pref_deck_id)
    String KEY_DECK_ID;
    @NonNull
    private final SharedPreferences sharedPreferences;

    private PreferenceRepository(@NonNull final SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setFirstTime(final boolean bool) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, bool);
        editor.apply();
    }

    public boolean getFirstTime() {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true);
    }

    public void setSwipeRemove(final boolean bool) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SWIPE_REMOVE, bool);
        editor.apply();
    }

    public boolean getSwipeRemove() {
        return sharedPreferences.getBoolean(KEY_SWIPE_REMOVE, false);
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

    public static PreferenceRepository fromDefault(@NonNull final Context context) {
        return PreferenceRepository.from(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static PreferenceRepository from(@NonNull final SharedPreferences sharedPreferences) {
        return new PreferenceRepository(sharedPreferences);
    }
}
