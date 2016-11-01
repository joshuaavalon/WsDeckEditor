package com.joshuaavalon.wsdeckeditor.sdk.util;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.joshuaavalon.wsdeckeditor.sdk.Deck;

public class DeckUtils {
    private static final int DECK_LIMIT = 50;

    public static CharSequence getStatusLabel(@NonNull final Deck deck) {
        final int count = deck.getCardList().size();
        final String countLabel = count > 99 ? "99+" : String.valueOf(count);
        final SpannableString countLabelSpan = new SpannableString(countLabel);
        if (count != DECK_LIMIT)
            countLabelSpan.setSpan(new ForegroundColorSpan(
                    (count > DECK_LIMIT) ? Color.RED : Color.GRAY), 0, countLabel.length(), 0);
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(countLabelSpan);
        builder.append(" / ");
        builder.append(String.valueOf(DECK_LIMIT));
        return builder;
    }
}