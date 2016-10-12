package com.joshuaavalon.wsdeckeditor.sdk.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.util.AbstractDeck;
import com.joshuaavalon.wsdeckeditor.sdk.util.DeckRecord;

import java.util.ArrayList;
import java.util.List;

public class DeckRepository {
    public static Loader<Cursor> newDecksLoader(@NonNull final Context context) {
        return new CursorLoader(context, DeckProvider.DECK_CONTENT_URI, null, null, null, DeckDatabase.Field.Id);
    }

    public static Loader<Cursor> newDeckLoader(@NonNull final Context context, final long id) {
        return new CursorLoader(context, ContentUris.withAppendedId(DeckProvider.DECK_CONTENT_URI, id)
                , null, null, null, null);
    }

    public static Loader<Cursor> newDeckRecordLoader(@NonNull final Context context, final long id) {
        return new CursorLoader(context, ContentUris.withAppendedId(DeckProvider.DECK_RECORD_CONTENT_URI, id)
                , new String[]{DeckDatabase.Field.Serial, DeckDatabase.Field.Count}, null, null, null);
    }

    public static void createDeck(@NonNull final Context context, @NonNull final Deck deck) {
        final ContentResolver contentResolver = context.getContentResolver();
        final ContentValues deckValues = new ContentValues();
        deckValues.put(DeckDatabase.Field.Name, deck.getName());
        final Uri uri = contentResolver.insert(DeckProvider.DECK_CONTENT_URI, deckValues);
        if (uri == null) return;
        final long id = ContentUris.parseId(uri);
        deck.setId(id);
        insertCard(contentResolver, id, deck.getCardList());
    }

    public static void deleteDeck(@NonNull final Context context, @NonNull final Deck deck) {
        deleteDeck(context, deck.getId());
    }


    public static void deleteDeck(@NonNull final Context context, final long id) {
        if (id == Deck.NO_ID) return;
        final ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(ContentUris.withAppendedId(DeckProvider.DECK_CONTENT_URI,
                id), null, null);
        contentResolver.delete(ContentUris.withAppendedId(DeckProvider.DECK_RECORD_CONTENT_URI,
                id), null, null);
    }

    public static void updateDeckCount(@NonNull final Context context, final long id,
                                       @NonNull final String serial, final int count) {
        if (id == Deck.NO_ID) return;
        final ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(ContentUris.withAppendedId(DeckProvider.DECK_RECORD_CONTENT_URI,
                id), String.format("%s = ?", DeckDatabase.Field.Serial), new String[]{serial});
        if (count <= 0) return;
        final ContentValues cardValues = new ContentValues();
        cardValues.put(DeckDatabase.Field.DeckId, id);
        cardValues.put(DeckDatabase.Field.Serial, serial);
        cardValues.put(DeckDatabase.Field.Count, count);
        contentResolver.insert(DeckProvider.DECK_RECORD_CONTENT_URI, cardValues);
    }

    public static boolean addCardIfNotExist(@NonNull final Context context, final long id,
                                            @NonNull final String serial) {
        if (id == Deck.NO_ID) return false;
        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor cursor = contentResolver.query(ContentUris.withAppendedId(DeckProvider.DECK_RECORD_CONTENT_URI,
                id), null, String.format("%s = ?", DeckDatabase.Field.Serial), new String[]{serial}, null);
        if (cursor == null) return false;
        final int count = cursor.getCount();
        cursor.close();
        if (count >= 1)
            return true;
        final ContentValues cardValues = new ContentValues();
        cardValues.put(DeckDatabase.Field.DeckId, id);
        cardValues.put(DeckDatabase.Field.Serial, serial);
        cardValues.put(DeckDatabase.Field.Count, 1);
        contentResolver.insert(DeckProvider.DECK_RECORD_CONTENT_URI, cardValues);
        return true;
    }


    public static boolean addCard(@NonNull final Context context, final long id,
                                  @NonNull final String serial) {
        if (id == Deck.NO_ID) return false;
        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor cursor = contentResolver.query(ContentUris.withAppendedId(DeckProvider.DECK_RECORD_CONTENT_URI,
                id), new String[]{DeckDatabase.Field.Count}, String.format("%s = ?", DeckDatabase.Field.Serial), new String[]{serial}, null);
        if (cursor == null) return false;
        cursor.moveToFirst();
        final int count = cursor.getCount() == 0 ? 1 : cursor.getInt(0) + 1;
        cursor.close();
        final ContentValues cardValues = new ContentValues();
        cardValues.put(DeckDatabase.Field.DeckId, id);
        cardValues.put(DeckDatabase.Field.Serial, serial);
        cardValues.put(DeckDatabase.Field.Count, count);
        contentResolver.insert(DeckProvider.DECK_RECORD_CONTENT_URI, cardValues);
        return true;
    }


    public static void updateDeckName(@NonNull final Context context, final long id, @NonNull final String name) {
        if (id == Deck.NO_ID) return;
        final ContentResolver contentResolver = context.getContentResolver();
        final ContentValues deckValues = new ContentValues();
        deckValues.put(DeckDatabase.Field.Name, name);
        contentResolver.update(ContentUris.withAppendedId(DeckProvider.DECK_CONTENT_URI,
                id), deckValues, null, null);
    }

    private static void insertCard(@NonNull final ContentResolver contentResolver, final long id,
                                   @NonNull final Multiset<Card> cards) {
        final List<ContentValues> deckRecordValues = new ArrayList<>();
        for (Card card : cards.elementSet()) {
            final ContentValues cardValues = new ContentValues();
            cardValues.put(DeckDatabase.Field.DeckId, id);
            cardValues.put(DeckDatabase.Field.Serial, card.getSerial());
            cardValues.put(DeckDatabase.Field.Count, cards.count(card));
            deckRecordValues.add(cardValues);
        }
        contentResolver.bulkInsert(DeckProvider.DECK_RECORD_CONTENT_URI,
                deckRecordValues.toArray(new ContentValues[deckRecordValues.size()]));
    }


    @NonNull
    public static List<AbstractDeck> toDecks(@NonNull final Cursor cursor) {
        final List<AbstractDeck> abstractDecks = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                abstractDecks.add(buildDeck(cursor));
            } while (cursor.moveToNext());
        return abstractDecks;
    }

    @Nullable
    public static AbstractDeck toDeck(@NonNull final Cursor cursor) {
        if (cursor.moveToFirst())
            return buildDeck(cursor);
        else
            return null;
    }

    @NonNull
    public static List<DeckRecord> toDeckRecords(@NonNull final Cursor cursor) {
        final List<DeckRecord> deckRecords = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                deckRecords.add(new DeckRecord(
                        cursor.getString(cursor.getColumnIndexOrThrow(DeckDatabase.Field.Serial)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DeckDatabase.Field.Count))));
            } while (cursor.moveToNext());
        return deckRecords;
    }

    @NonNull
    private static AbstractDeck buildDeck(@NonNull final Cursor cursor) {
        return new AbstractDeck(cursor.getLong(cursor.getColumnIndexOrThrow(DeckDatabase.Field.Id)),
                cursor.getString(cursor.getColumnIndexOrThrow(DeckDatabase.Field.Name)));
    }
}
