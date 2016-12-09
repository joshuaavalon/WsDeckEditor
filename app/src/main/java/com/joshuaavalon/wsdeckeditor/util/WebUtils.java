package com.joshuaavalon.wsdeckeditor.util;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.view.View;

import com.joshuaavalon.wsdeckeditor.R;

public final class WebUtils {
    public static void launchUrl(@NonNull final Context context, @NonNull final String url) {
        final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.setStartAnimations(context, R.anim.enter_from_right, R.anim.exit_to_left);
        final Uri uri = Uri.parse(url);
        final CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, uri);
    }

    public static Preference.OnPreferenceClickListener launchUrlFromPreference(@NonNull final Context context, @NonNull final String url) {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                launchUrl(context, url);
                return true;
            }
        };
    }

    public static View.OnClickListener launchUrlFromClick(@NonNull final Context context, @NonNull final String url) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchUrl(context, url);
            }
        };
    }
}
