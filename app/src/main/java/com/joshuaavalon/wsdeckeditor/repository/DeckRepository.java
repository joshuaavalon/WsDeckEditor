package com.joshuaavalon.wsdeckeditor.repository;

import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;

import com.google.common.base.Optional;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.WsApplication;
import com.joshuaavalon.wsdeckeditor.database.DeckDatabaseHelper;
import com.joshuaavalon.wsdeckeditor.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class DeckRepository {

    public static void save(@NonNull final Deck deck) {
        final DeckDatabaseHelper helper = getHelper();
        if (deck.getId() == Deck.NO_ID) {
            deck.setId(helper.setDeck(deck.getName()));
        } else {
            helper.setDeck(deck.getId(), deck.getName());
        }
        helper.deleteDeckRecordById(deck.getId());
        final Multiset<String> serials = deck.getSerialList();
        helper.setDeckRecord(deck.getId(), serials);
    }

    public static void delete(@NonNull final Deck deck) {
        if (deck.getId() == Deck.NO_ID) return;
        final DeckDatabaseHelper helper = getHelper();
        helper.deleteDeckById(deck.getId());
        helper.deleteDeckRecordById(deck.getId());
        deck.setId(Deck.NO_ID);
    }

    @NonNull
    public static List<Deck> getDecks() {
        final List<Deck> decks = new ArrayList<>();
        final DeckDatabaseHelper helper = getHelper();
        final LongSparseArray<String> deckNames = helper.getAllDecks();
        for (int i = 0; i < deckNames.size(); i++) {
            final long deckId = deckNames.keyAt(i);
            final Deck deck = new Deck(deckId, helper.getDeckRecordsById(deckId));
            deck.setName(deckNames.get(deckId));
            decks.add(deck);
        }
        return decks;
    }

    @NonNull
    public static Optional<Deck> getDeckById(final long id) {
        final DeckDatabaseHelper helper = getHelper();
        final Optional<String> deckNameOptional = helper.getDeckNameById(id);
        if (!deckNameOptional.isPresent()) return Optional.absent();
        final Deck deck = new Deck(id, helper.getDeckRecordsById(id));
        deck.setName(deckNameOptional.get());
        return Optional.of(deck);
    }

    @NonNull
    private static DeckDatabaseHelper getHelper() {
        return new DeckDatabaseHelper(WsApplication.getContext());
    }
}
