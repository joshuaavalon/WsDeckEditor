package com.joshuaavalon.wsdeckeditor.sdk.deck;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class DeckRepository implements IDeckRepository {
    @NonNull
    private final SQLiteOpenHelper helper;
    @NonNull
    private final ICardRepository cardRepository;

    DeckRepository(@NonNull final SQLiteOpenHelper helper, @NonNull ICardRepository cardRepository) {
        this.helper = helper;
        this.cardRepository = cardRepository;
    }

    @NonNull
    private static DeckMeta buildDeck(@NonNull final Cursor cursor) {
        return new DeckMeta(
                cursor.getLong(cursor.getColumnIndexOrThrow(DeckScheme.Field.Id)),
                cursor.getString(cursor.getColumnIndexOrThrow(DeckScheme.Field.Name)),
                cursor.getString(cursor.getColumnIndexOrThrow(DeckScheme.Field.Cover))
        );
    }

    @Override
    public void save(@NonNull final Deck deck) {
        final boolean isUpdate = deck.getId() != Deck.NO_ID;
        final SQLiteDatabase database = helper.getWritableDatabase();
        final ContentValues deckValues = new ContentValues();
        deckValues.put(DeckScheme.Field.Name, deck.getName());
        deckValues.put(DeckScheme.Field.Cover, deck.getCover());
        database.beginTransaction();
        if (isUpdate) {
            database.update(DeckScheme.Table.Deck, deckValues, DeckScheme.Field.Id + "=?",
                    new String[]{String.valueOf(deck.getId())});
            database.delete(DeckScheme.Table.DeckRecord, DeckScheme.Field.DeckId + "=?",
                    new String[]{String.valueOf(deck.getId())});
        } else
            deck.setId(database.insert(DeckScheme.Table.Deck, null, deckValues));
        final Multiset<Card> cards = deck.getCardList();
        for (Card card : cards.elementSet()) {
            final ContentValues cardValues = new ContentValues();
            cardValues.put(DeckScheme.Field.DeckId, deck.getId());
            cardValues.put(DeckScheme.Field.Serial, card.getSerial());
            cardValues.put(DeckScheme.Field.Count, cards.count(card));
            database.insert(DeckScheme.Table.DeckRecord, null, cardValues);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
    }

    @Override
    public void save(@NonNull final DeckMeta meta) {
        if (meta.getId() == Deck.NO_ID) return;
        final SQLiteDatabase database = helper.getWritableDatabase();
        final ContentValues deckValues = new ContentValues();
        deckValues.put(DeckScheme.Field.Name, meta.getName());
        deckValues.put(DeckScheme.Field.Cover, meta.getCover());
        database.update(DeckScheme.Table.Deck, deckValues, DeckScheme.Field.Id + "=?",
                new String[]{String.valueOf(meta.getId())});
        database.close();
    }

    @Override
    public void remove(@NonNull final Deck deck) {
        if (deck.getId() == Deck.NO_ID) return;
        final SQLiteDatabase database = helper.getWritableDatabase();
        database.delete(DeckScheme.Table.Deck, DeckScheme.Field.Id + "=?",
                new String[]{String.valueOf(deck.getId())});
        database.delete(DeckScheme.Table.DeckRecord, DeckScheme.Field.DeckId + "=?",
                new String[]{String.valueOf(deck.getId())});
        deck.setId(Deck.NO_ID);
        database.close();
    }

    @Override
    public void remove(final long id) {
        if (id == Deck.NO_ID) return;
        final SQLiteDatabase database = helper.getWritableDatabase();
        database.delete(DeckScheme.Table.Deck, DeckScheme.Field.Id + "=?",
                new String[]{String.valueOf(id)});
        database.delete(DeckScheme.Table.DeckRecord, DeckScheme.Field.DeckId + "=?",
                new String[]{String.valueOf(id)});
        database.close();
    }

    @Override
    public void add(final long id, @NonNull final String serial, final boolean requireNone) {
        final SQLiteDatabase database = helper.getReadableDatabase();
        final Cursor cursor = database.query(DeckScheme.Table.DeckRecord,
                new String[]{DeckScheme.Field.Count},
                String.format("%s = ? AND %s = ?", DeckScheme.Field.DeckId, DeckScheme.Field.Serial),
                new String[]{String.valueOf(id), serial}, null, null, null);
        final int count = cursor.moveToFirst() ?
                cursor.getInt(cursor.getColumnIndexOrThrow(DeckScheme.Field.Count)) : 0;
        cursor.close();
        database.close();
        if (requireNone && count > 0) return;
        update(id, serial, count + 1);
    }

    @Override
    public void update(final long id, @NonNull final String serial, final int count) {
        final SQLiteDatabase database = helper.getWritableDatabase();
        final Cursor cursor = database.query(DeckScheme.Table.DeckRecord,
                new String[]{DeckScheme.Field.Id},
                String.format("%s = ? AND %s = ?", DeckScheme.Field.DeckId, DeckScheme.Field.Serial),
                new String[]{String.valueOf(id), serial}, null, null, null);
        final long recordId = cursor.moveToFirst() ?
                cursor.getLong(cursor.getColumnIndexOrThrow(DeckScheme.Field.Id)) : -1;
        cursor.close();
        final ContentValues cardValues = new ContentValues();
        cardValues.put(DeckScheme.Field.DeckId, id);
        cardValues.put(DeckScheme.Field.Serial, serial);
        cardValues.put(DeckScheme.Field.Count, count);
        if (recordId == -1)
            database.insert(DeckScheme.Table.DeckRecord, null, cardValues);
        else
            database.update(DeckScheme.Table.DeckRecord, cardValues, DeckScheme.Field.Id + "=?",
                    new String[]{String.valueOf(recordId)});
        database.close();
    }

    @NonNull
    @Override
    public List<DeckMeta> meta() {
        final SQLiteDatabase database = helper.getReadableDatabase();
        final Cursor cursor = database.query(DeckScheme.Table.Deck, null, null, null, null, null, DeckScheme.Field.Id);
        final List<DeckMeta> deckMetas = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                deckMetas.add(buildDeck(cursor));
            } while (cursor.moveToNext());
        cursor.close();
        database.close();
        return deckMetas;
    }

    @Nullable
    @Override
    public DeckMeta metaOf(final long id) {
        final SQLiteDatabase database = helper.getReadableDatabase();
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DeckScheme.Table.Deck);
        queryBuilder.appendWhere(DeckScheme.Field.Id + "=" + id);
        final Cursor cursor = queryBuilder.query(database, null, null, null, null, null, null);
        DeckMeta deckMeta = null;
        if (cursor.moveToFirst())
            deckMeta = buildDeck(cursor);
        cursor.close();
        database.close();
        return deckMeta;
    }

    @NonNull
    private List<DeckRecord> deckRecords(final long id) {
        final SQLiteDatabase database = helper.getReadableDatabase();
        final SQLiteQueryBuilder deckRecordQueryBuilder = new SQLiteQueryBuilder();
        deckRecordQueryBuilder.setTables(DeckScheme.Table.DeckRecord);
        deckRecordQueryBuilder.appendWhere(DeckScheme.Field.DeckId + "=" + id);
        final Cursor deckRecordCursor = deckRecordQueryBuilder.query(database, null, null, null, null, null, null);
        final List<DeckRecord> deckRecords = new ArrayList<>();
        if (deckRecordCursor.moveToFirst())
            do {
                deckRecords.add(new DeckRecord(
                        deckRecordCursor.getString(deckRecordCursor.getColumnIndexOrThrow(DeckScheme.Field.Serial)),
                        deckRecordCursor.getInt(deckRecordCursor.getColumnIndexOrThrow(DeckScheme.Field.Count))));
            } while (deckRecordCursor.moveToNext());
        deckRecordCursor.close();
        database.close();
        return deckRecords;
    }

    @Nullable
    @Override
    public Deck deck(final long id) {
        final DeckMeta deckMeta = metaOf(id);
        if (deckMeta == null) return null;
        final List<DeckRecord> deckRecords = deckRecords(id);
        final List<String> serials = Lists.newArrayList(Iterables.transform(deckRecords,
                new Function<DeckRecord, String>() {
                    @Nullable
                    @Override
                    public String apply(DeckRecord input) {
                        return input.getSerial();
                    }
                }));
        final Deck deck = new Deck();
        deck.setId(deckMeta.getId());
        deck.setName(deckMeta.getName());
        deck.setCover(deckMeta.getCover());
        for (DeckRecord record : deckRecords) {
            for (Card card : cardRepository.findAll(serials)) {
                if (!Objects.equals(card.getSerial(), record.getSerial())) continue;
                deck.setCardCount(card, record.getCount());
                break;
            }
        }
        return deck;
    }

    @Override
    public int cardCount(long id) {
        final SQLiteDatabase database = helper.getReadableDatabase();
        final Cursor cursor = database.query(DeckScheme.Table.DeckRecord,
                new String[]{String.format("SUM(%s)", DeckScheme.Field.Count)},
                DeckScheme.Field.DeckId + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        int count = 0;
        if (cursor.moveToFirst())
            count = cursor.getInt(0);
        cursor.close();
        database.close();
        return count;
    }
}
