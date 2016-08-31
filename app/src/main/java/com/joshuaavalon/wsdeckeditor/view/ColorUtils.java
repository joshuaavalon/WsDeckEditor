package com.joshuaavalon.wsdeckeditor.view;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Card;

public class ColorUtils {
    @ColorRes
    public static int getColor(@NonNull final Card.Color color) {
        switch (color) {
            case Yellow:
                return R.color.cardYellow;
            case Green:
                return R.color.cardGreen;
            case Red:
                return R.color.cardRed;
            case Blue:
                return R.color.cardBlue;
            default:
                return R.color.cardYellow;
        }
    }
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
