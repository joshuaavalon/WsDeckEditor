package com.joshuaavalon.wsdeckeditor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.fluentquery.Query;

public class DeckDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "deck.db";
    private static final int VERSION = 1;
    private static final String DECK_TABLE = "deck";
    private static final String DECK_ID = "Id";
    private static final String DECK_NAME = "Name";
    private static final String DECK_RECORD_TABLE = "deck_record";
    private static final String DECK_RECORD_ID = "Id";
    private static final String DECK_RECORD_COUNT = "Count";
    private static final String DECK_RECORD_DECK_ID = "DeckId";
    private static final String DECK_RECORD_SERIAL = "Serial";

    public DeckDatabaseHelper(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL)",
                        DECK_TABLE, DECK_ID, DECK_NAME));
        sqLiteDatabase.execSQL(
                String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s TEXT NOT NULL)",
                        DECK_RECORD_TABLE, DECK_RECORD_ID, DECK_RECORD_COUNT,
                        DECK_RECORD_DECK_ID, DECK_RECORD_SERIAL));
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase,
                          final int oldVersion,
                          final int newVersion) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", DECK_TABLE));
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", DECK_RECORD_TABLE));
        onCreate(sqLiteDatabase);
    }

    @NonNull
    public LongSparseArray<String> getAllDecks() {
        final Query query = Query.select(DECK_ID, DECK_NAME).from(DECK_TABLE);
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = query.commit(db);
        final LongSparseArray<String> result = new LongSparseArray<>();
        if (cursor.moveToFirst()) {
            final int deckIdIndex = cursor.getColumnIndex(DECK_ID);
            final int deckNameIndex = cursor.getColumnIndex(DECK_NAME);
            do {
                result.put(cursor.getInt(deckIdIndex), cursor.getString(deckNameIndex));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return result;
    }

    public void deleteDeckById(final long id) {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(DECK_TABLE, DECK_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteDeckRecordById(final long deckId) {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(DECK_RECORD_TABLE, DECK_RECORD_DECK_ID + " = ?", new String[]{String.valueOf(deckId)});
        db.close();
    }

    @NonNull
    public Optional<String> getDeckNameById(final long id) {
        final Query query = Query.select(DECK_NAME).from(DECK_TABLE)
                .where(Condition.property(DECK_ID).equal(String.valueOf(id)));
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = query.commit(db);
        Optional<String> result;
        if (cursor.moveToFirst()) {
            final int deckNameIndex = cursor.getColumnIndex(DECK_NAME);
            result = Optional.of(cursor.getString(deckNameIndex));
            cursor.close();
        } else
            result = Optional.absent();
        db.close();
        return result;
    }

    @NonNull
    public Multiset<String> getDeckRecordsById(final long deckId) {
        final Query query = Query.select(DECK_RECORD_COUNT, DECK_RECORD_SERIAL)
                .from(DECK_RECORD_TABLE)
                .where(Condition.property(DECK_RECORD_DECK_ID).equal(String.valueOf(deckId)));
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = query.commit(db);
        final Multiset<String> result = HashMultiset.create();
        if (cursor.moveToFirst()) {
            final int countIndex = cursor.getColumnIndex(DECK_RECORD_COUNT);
            final int serialIndex = cursor.getColumnIndex(DECK_RECORD_SERIAL);
            do {
                result.setCount(cursor.getString(serialIndex), cursor.getInt(countIndex));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return result;
    }

    public long setDeck(@NonNull final String name) {
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(DECK_NAME, name);
        long id = db.insert(DECK_TABLE, null, values);
        db.close();
        return id;
    }

    public void setDeck(final long id, @NonNull final String name) {
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(DECK_NAME, name);
        db.update(DECK_TABLE, values, DECK_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void setDeckRecord(final long deckId, @NonNull final Multiset<String> serials) {
        final SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (String serial : serials.elementSet()) {
                final ContentValues values = new ContentValues();
                values.put(DECK_RECORD_DECK_ID, deckId);
                values.put(DECK_RECORD_COUNT, serials.count(serial));
                values.put(DECK_RECORD_SERIAL, serial);
                db.insert(DECK_RECORD_TABLE, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }
}
