package com.joshuaavalon.wsdeckeditor.util;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;

import timber.log.Timber;

public class ColorUtils {
    @DrawableRes
    public static int getBackgroundDrawable(@NonNull final Card.Color color) {
        switch (color) {
            case Yellow:
                return R.drawable.highlight_background_yellow;
            case Green:
                return R.drawable.highlight_background_green;
            case Red:
                return R.drawable.highlight_background_red;
            case Blue:
                return R.drawable.highlight_background_blue;
            default:
                Timber.e(new IllegalArgumentException(), "Unknown color: %s", color.name());
                return R.drawable.highlight_background_yellow;
        }
    }
}
