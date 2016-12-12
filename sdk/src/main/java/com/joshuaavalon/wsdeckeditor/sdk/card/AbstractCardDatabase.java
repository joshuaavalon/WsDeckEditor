package com.joshuaavalon.wsdeckeditor.sdk.card;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.io.InputStream;

/**
 * {@link AbstractCardDatabase} defines the necessary interface for a card database.
 */
abstract class AbstractCardDatabase extends SQLiteOpenHelper {
    public AbstractCardDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    abstract void copyDatabase(@NonNull InputStream in);
}
