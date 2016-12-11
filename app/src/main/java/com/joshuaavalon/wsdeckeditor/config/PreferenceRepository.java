package com.joshuaavalon.wsdeckeditor.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.sdk.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

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
    @NonNull
    private RequestQueue requestQueue;

    private PreferenceRepository(@NonNull final Context context,
                                 @NonNull final SharedPreferences sharedPreferences) {
        requestQueue = Volley.newRequestQueue(context);
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

    public void networkVersion(@NonNull final Response.Listener<Version> listener, @Nullable final Response.ErrorListener errorListener) {
        requestQueue.add(new StringRequest(Request.Method.GET, BuildConfig.versionUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Version version = null;
                try {
                    final JSONObject jsonObject = new JSONObject(response);
                    final String tag = jsonObject.getString("tag_name");
                    Timber.i("GitHub Version: %s", tag);
                    version = new Version(tag);
                } catch (JSONException | IllegalArgumentException e) {
                    Timber.e(e, "GitHub Version Error");
                }
                listener.onResponse(version);
            }
        }, errorListener));
    }

    public void needUpdated(@NonNull final Response.Listener<Boolean> listener, @Nullable final Response.ErrorListener errorListener) {
        networkVersion(new Response.Listener<Version>() {
            @Override
            public void onResponse(Version response) {
                if (response == null) {
                    listener.onResponse(false);
                    return;
                }
                final Version currentVersion = new Version(BuildConfig.VERSION_NAME);
                listener.onResponse(currentVersion.compareTo(response) < 0);
            }
        }, errorListener);
    }
}
