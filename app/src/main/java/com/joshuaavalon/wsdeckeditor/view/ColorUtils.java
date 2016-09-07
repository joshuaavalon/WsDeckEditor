package com.joshuaavalon.wsdeckeditor.view;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.model.Deck;
import com.joshuaavalon.wsdeckeditor.model.DeckUtils;

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

    public static void setColorView(@NonNull final Deck deck,
                                    @NonNull final View view) {

        final Multiset<Card.Color> colorCount = DeckUtils.getColorCount(deck);
        for (Card.Color color : Card.Color.values()) {
            final View colorView = getColorView(color, view);
            if (colorView == null) continue;
            final LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(colorView.getLayoutParams());
            params.weight = colorCount.count(color);
            colorView.setLayoutParams(params);
            colorView.setBackgroundResource(color.getColorResId());
        }
    }

    @Nullable
    private static View getColorView(@NonNull final Card.Color color, @NonNull final View view) {
        switch (color) {
            case Yellow:
                return view.findViewById(R.id.yellow_bar);
            case Green:
                return view.findViewById(R.id.green_bar);
            case Red:
                return view.findViewById(R.id.red_bar);
            case Blue:
                return view.findViewById(R.id.blue_bar);
            default:
                return null;
        }
    }
}
