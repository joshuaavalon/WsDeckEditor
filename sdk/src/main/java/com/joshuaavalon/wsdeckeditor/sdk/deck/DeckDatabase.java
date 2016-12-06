package com.joshuaavalon.wsdeckeditor.sdk.deck;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

public class DeckDatabase extends SQLiteOpenHelper {
    private static final int VERSION = 2;

    public DeckDatabase(@NonNull final Context context) {
        super(context, DeckFacade.DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT)",
                        DeckScheme.Table.Deck, DeckScheme.Field.Id, DeckScheme.Field.Name, DeckScheme.Field.Cover));
        sqLiteDatabase.execSQL(
                String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s TEXT NOT NULL)",
                        DeckScheme.Table.DeckRecord, DeckScheme.Field.Id, DeckScheme.Field.Count, DeckScheme.Field.DeckId, DeckScheme.Field.Serial));
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase,
                          final int oldVersion,
                          final int newVersion) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", DeckScheme.Table.Deck));
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", DeckScheme.Table.DeckRecord));
        onCreate(sqLiteDatabase);
    }
}
