package com.joshuaavalon.wsdeckeditor.sdk;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Deck implements Parcelable {
    public static final long NO_ID = -1;
    @NonNull
    private String name;
    private long id;
    @NonNull
    private final Multiset<Card> cardList;

    public Deck() {
        name = "";
        id = NO_ID;
        cardList = HashMultiset.create();
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull final String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @NonNull
    public Multiset<Card> getCardList() {
        return ImmutableMultiset.copyOf(cardList);
    }

    @NonNull
    public Set<String> getExpansion() {
        final Set<String> expansions = new HashSet<>();
        for (Card card : cardList.elementSet())
            expansions.add(card.getExpansion());
        return expansions;
    }

    @NonNull
    public Multiset<Card.Color> getColor() {
        final Multiset<Card.Color> colors = HashMultiset.create();
        for (Card card : cardList.elementSet())
            colors.add(card.getColor());
        return colors;
    }

    @NonNull
    public Multiset<Card.Trigger> getTrigger() {
        final Multiset<Card.Trigger> triggers = HashMultiset.create();
        for (Card card : cardList.elementSet())
            triggers.add(card.getTrigger());
        return triggers;
    }

    @NonNull
    public Multiset<Card.Type> getType() {
        final Multiset<Card.Type> types = HashMultiset.create();
        for (Card card : cardList.elementSet())
            types.add(card.getType());
        return types;
    }

    @NonNull
    public Multiset<Integer> getLevel() {
        final Multiset<Integer> levels = HashMultiset.create();
        for (Card card : cardList.elementSet())
            levels.add(card.getLevel());
        return levels;
    }

    @NonNull
    public Multiset<Integer> getCost() {
        final Multiset<Integer> costs = HashMultiset.create();
        for (Card card : cardList.elementSet())
            costs.add(card.getCost());
        return costs;
    }

    public void setCardCount(@NonNull final Card card, @IntRange(from = 0) final int count) {
        cardList.setCount(card, count);
    }

    public void removeCard(@NonNull final Card card) {
        cardList.remove(card);
    }

    public void addCard(@NonNull final Card card) {
        cardList.add(card);
    }

    public void addIfNotExist(@NonNull final Card card) {
        if (!cardList.contains(card))
            addCard(card);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(id);
        dest.writeParcelableArray(cardList.toArray(new Card[cardList.size()]), 0);
    }

    protected Deck(Parcel in) {
        name = in.readString();
        id = in.readLong();
        final Card[] cards = (Card[]) in.readParcelableArray(Card.class.getClassLoader());
        cardList = HashMultiset.create(Arrays.asList(cards));
    }

    public static final Creator<Deck> CREATOR = new Creator<Deck>() {
        @Override
        public Deck createFromParcel(Parcel source) {
            return new Deck(source);
        }

        @Override
        public Deck[] newArray(int size) {
            return new Deck[size];
        }
    };
}
