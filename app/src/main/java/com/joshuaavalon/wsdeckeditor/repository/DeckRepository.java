package com.joshuaavalon.wsdeckeditor.repository;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.database.DeckDao;
import com.joshuaavalon.wsdeckeditor.database.DeckRecord;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class DeckRepository {

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

    public static void delete(@NonNull final Deck deck) {
        if (deck.getId() == Deck.NO_ID) return;
        final DeckDao deckDao = DeckDao.findById(DeckDao.class, deck.getId());
        deckDao.delete();
        DeckRecord.deleteAll(DeckRecord.class, "DECK = ?", String.valueOf(deck.getId()));
        deck.setId(Deck.NO_ID);
    }

    @NonNull
    public static List<Deck> getDecks() {
        final List<Deck> decks = new ArrayList<>();
        for (DeckDao deckDao : DeckDao.find(DeckDao.class, null)) {
            decks.add(toDeck(deckDao));
        }
        return decks;
    }

    private static Deck toDeck(@NonNull DeckDao deckDao) {
        final Deck deck = new Deck(deckDao.getId());
        deck.setName(deckDao.getName());
        final List<DeckRecord> records = deckDao.getRecords();
        for (DeckRecord record : records) {
            deck.setCount(record.getSerial(), record.getCount());
        }
        return deck;
    }

    @NonNull
    public static Optional<Deck> getDeckById(final long id) {
        final DeckDao deckDao = DeckDao.findById(DeckDao.class, id);
        if (deckDao == null)
            return Optional.absent();
        else
            return Optional.of(toDeck(deckDao));
    }
}
