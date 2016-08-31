package com.joshuaavalon.wsdeckeditor.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.database.DeckDao;
import com.joshuaavalon.wsdeckeditor.database.DeckRecord;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class DeckRepository {
    @Nullable
    private static Deck currentDeck;

    public static void save(@NonNull final Deck deck) {
        DeckDao deckDao;
        if (deck.getId() == Deck.NO_ID) {
            deckDao = new DeckDao(deck.getName());
        } else {
            deckDao = DeckDao.findById(DeckDao.class, deck.getId());
            deckDao.setName(deck.getName());
        }
        final long id = deckDao.save();
        deck.setId(id);
        DeckRecord.deleteAll(DeckRecord.class, "DECK = ?", String.valueOf(id));
        final Multiset<Card> cards = deck.getList();
        for (Card card : cards.elementSet()) {
            final DeckRecord record = new DeckRecord(card.getSerial(), cards.count(card), deckDao);
            record.save();
        }
    }

    @NonNull
    public static List<Deck> getDecks() {
        final List<Deck> decks = new ArrayList<>();
        for (DeckDao deckDao : DeckDao.find(DeckDao.class, null)) {
            final Deck deck = new Deck(deckDao.getId());
            final List<DeckRecord> records = deckDao.getRecords();
            for (DeckRecord record : records) {
                deck.setCount(record.getSerial(), record.getCount());
            }
            decks.add(deck);
        }
        return decks;
    }

    @Nullable
    public static Deck getCurrentDeck() {
        return currentDeck;
    }

    @NonNull
    public static Deck getCurrentDeckOrCreateNewDeck() {
        if (currentDeck == null)
            currentDeck = new Deck();
        return currentDeck;
    }
}
