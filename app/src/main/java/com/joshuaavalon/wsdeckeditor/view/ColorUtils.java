package com.joshuaavalon.wsdeckeditor.view;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.model.DeckUtils;

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

    public static int getColorValue(@ColorRes final int color) {
        return ContextCompat.getColor(WsApplication.getContext(), color);
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
