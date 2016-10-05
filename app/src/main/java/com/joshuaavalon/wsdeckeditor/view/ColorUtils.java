package com.joshuaavalon.wsdeckeditor.view;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.sdk.Card;

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
                return R.drawable.highlight_background_yellow;
        }
    }
}
