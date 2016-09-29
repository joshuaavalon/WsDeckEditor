package com.joshuaavalon.wsdeckeditor.sdk.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

class DeckDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "deck.db";
    private static final int VERSION = 1;

    public DeckDatabase(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL)",
                        Table.Deck, Field.Id, Field.Name));
        sqLiteDatabase.execSQL(
                String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s TEXT NOT NULL)",
                        Table.DeckRecord, Field.Id, Field.Count, Field.DeckId, Field.Serial));
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase,
                          final int oldVersion,
                          final int newVersion) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", Table.Deck));
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", Table.DeckRecord));
        onCreate(sqLiteDatabase);
    }

    interface Table {
        String Deck = "deck";
        String DeckRecord = "deck_record";
    }

    interface Field {
        String Id = "Id";
        String Name = "Name";
        String Count = "Count";
        String DeckId = "DeckId";
        String Serial = "Serial";
    }
}
