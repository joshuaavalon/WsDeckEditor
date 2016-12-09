package com.joshuaavalon.wsdeckeditor.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

class AppDatabase extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "app.db";

    public AppDatabase(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL UNIQUE, %s DATETIME DEFAULT CURRENT_TIMESTAMP)",
                        AppScheme.Table.Search, AppScheme.Field.Id, AppScheme.Field.Keyword, AppScheme.Field.LastAccess));
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase,
                          final int oldVersion,
                          final int newVersion) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", AppScheme.Table.Search));
        onCreate(sqLiteDatabase);
    }
}
