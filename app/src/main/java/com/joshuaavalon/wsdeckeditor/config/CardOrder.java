package com.joshuaavalon.wsdeckeditor.config;

import android.support.annotation.NonNull;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;

import java.util.Comparator;

public enum CardOrder {
    Serial(new SerialComparator()), Level(new LevelComparator()), Detail(new DetailComparator());
    @NonNull
    private final Comparator<Multiset.Entry<Card>> comparator;

    CardOrder(@NonNull final Comparator<Multiset.Entry<Card>> comparator) {
        this.comparator = comparator;
    }

    @NonNull
    public Comparator<Multiset.Entry<Card>> getComparator() {
        return comparator;
    }

    private static class SerialComparator implements Comparator<Multiset.Entry<Card>> {
        @Override
        public int compare(Multiset.Entry<Card> o1, Multiset.Entry<Card> o2) {
            return o1.getElement().getSerial().compareTo(o2.getElement().getSerial());
        }
    }

    private static class LevelComparator implements Comparator<Multiset.Entry<Card>> {
        @Override
        public int compare(Multiset.Entry<Card> o1, Multiset.Entry<Card> o2) {
            return o1.getElement().getLevel() - o2.getElement().getLevel();
        }
    }

    private static class DetailComparator implements Comparator<Multiset.Entry<Card>> {
        @Override
        public int compare(Multiset.Entry<Card> o1, Multiset.Entry<Card> o2) {
            final Card left = o1.getElement();
            final Card right = o2.getElement();
            return ComparisonChain.start()
                    .compare(left.getType().ordinal(), right.getType().ordinal())
                    .compare(left.getColor().ordinal(), right.getColor().ordinal())
                    .compare(left.getLevel(), right.getLevel())
                    .compare(left.getSerial(), right.getSerial())
                    .result();
        }
    }
}
