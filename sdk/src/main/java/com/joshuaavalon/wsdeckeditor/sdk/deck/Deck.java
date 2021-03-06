package com.joshuaavalon.wsdeckeditor.sdk.deck;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;

import java.util.HashSet;
import java.util.Set;

public class Deck {
    public static final long NO_ID = -1;
    @NonNull
    private final Multiset<Card> cardList;
    @NonNull
    private String name;
    private long id;
    @Nullable
    private String cover;

    public Deck() {
        name = "";
        id = NO_ID;
        cardList = HashMultiset.create();
        cover = null;
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

    @Nullable
    public String getCover() {
        return cover;
    }

    public void setCover(@Nullable final String cover) {
        this.cover = cover;
    }

    @NonNull
    public Multiset<Card> getCardList() {
        return ImmutableMultiset.copyOf(cardList);
    }

    public void setCardList(@NonNull final Multiset<Card> cardList) {
        this.cardList.clear();
        this.cardList.addAll(cardList);
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
        for (Multiset.Entry<Card> entry : cardList.entrySet())
            colors.add(entry.getElement().getColor(), entry.getCount());
        return colors;
    }

    @NonNull
    public Multiset<Card.Trigger> getTrigger() {
        final Multiset<Card.Trigger> triggers = HashMultiset.create();
        for (Multiset.Entry<Card> entry : cardList.entrySet())
            triggers.add(entry.getElement().getTrigger(), entry.getCount());
        return triggers;
    }

    @NonNull
    public Multiset<Card.Type> getType() {
        final Multiset<Card.Type> types = HashMultiset.create();
        for (Multiset.Entry<Card> entry : cardList.entrySet())
            types.add(entry.getElement().getType(), entry.getCount());
        return types;
    }

    @NonNull
    public Multiset<Integer> getLevel() {
        final Multiset<Integer> levels = HashMultiset.create();
        for (Multiset.Entry<Card> entry : cardList.entrySet())
            levels.add(entry.getElement().getLevel(), entry.getCount());
        return levels;
    }

    @NonNull
    public Multiset<Integer> getCost() {
        final Multiset<Integer> costs = HashMultiset.create();
        for (Multiset.Entry<Card> entry : cardList.entrySet())
            costs.add(entry.getElement().getCost(), entry.getCount());
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

    @NonNull
    public DeckMeta meta() {
        return new DeckMeta(id, name, cover);
    }
}
