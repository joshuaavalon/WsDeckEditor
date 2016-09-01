package com.joshuaavalon.wsdeckeditor.model;

import android.support.annotation.NonNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Deck {
    public static final long NO_ID = -1;
    @NonNull
    private final Multiset<String> serialList;
    @NonNull
    private String name;
    private long id;

    public Deck() {
        this(NO_ID);
    }

    public Deck(final long id) {
        this.id = id;
        serialList = HashMultiset.create();
        name = "";
    }

    public Deck(final long id, @NonNull final Multiset<String> serialList) {
        this.id = id;
        this.serialList = serialList;
        name = "";
    }

    public void setSerials(@NonNull final Collection<String> serialList) {
        this.serialList.clear();
        this.serialList.addAll(serialList);
    }

    public void setCards(@NonNull final Collection<Card> deckList) {
        setSerials(Collections2.transform(deckList, new Function<Card, String>() {
            @Override
            public String apply(Card input) {
                return input.getSerial();
            }
        }));
    }

    public void addIfNotExist(@NonNull final String serial) {
        if (!serialList.contains(serial))
            setCount(serial, 1);
    }


    public void addIfNotExist(@NonNull final Card card) {
        addIfNotExist(card.getSerial());
    }

    public void setCount(@NonNull final String serial, final int count) {
        serialList.setCount(serial, count);
    }

    public void setCount(@NonNull final Card card, final int count) {
        setCount(card.getSerial(), count);
    }

    public void remove(@NonNull final String serial) {
        serialList.remove(serial);
    }

    public void removeAll(@NonNull final Collection<String> serial) {
        serialList.removeAll(serial);
    }

    public void clear() {
        serialList.clear();
    }

    public Set<String> getExpansions() {
        final Set<String> expansions = new HashSet<>();
        for (Card card : getList())
            expansions.add(card.getExpansion());
        return expansions;
    }

    public int size() {
        return serialList.size();
    }

    public int size(@NonNull final Predicate<Card> filter) {
        return Iterables.size(Iterables.filter(getList(), filter));
    }

    @NonNull
    public Multiset<Card> getList() {
        ImmutableMultiset.Builder<Card> builder = new ImmutableMultiset.Builder<>();
        for (String serial : serialList.elementSet()) {
            final Optional<Card> cardOptional = CardRepository.getCardBySerial(serial);
            if (cardOptional.isPresent())
                builder.setCount(cardOptional.get(), serialList.count(serial));
        }
        return builder.build();
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Deck && ((Deck) obj).id == id && ((Deck) obj).name.equals(name);
    }
}
