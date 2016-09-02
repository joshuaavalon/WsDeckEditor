package com.joshuaavalon.wsdeckeditor.model;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;

public class DeckUtils {

    private static final int DECK_LIMIT = 50;

    public static CharSequence getStatusLabel(@NonNull final Deck deck) {
        final int count = getCount(deck);
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

    public static int getCount(@NonNull final Deck deck) {
        return deck.getList().size();
    }


    public static int getCount(@NonNull final Deck deck, @NonNull final Card.Color color) {
        return Iterables.size(Iterables.filter(deck.getList(), new Predicate<Card>() {
            @Override
            public boolean apply(Card input) {
                return input.getColor() == color;
            }
        }));
    }

    public static Multiset<Card.Color> getColorCount(@NonNull final Deck deck) {
        final Multiset<Card.Color> colorCount =  HashMultiset.create();
        for (Multiset.Entry<Card> entry :  deck.getList().entrySet()) {
            colorCount.add(entry.getElement().getColor(), entry.getCount());
        }
        return colorCount;
    }
}
