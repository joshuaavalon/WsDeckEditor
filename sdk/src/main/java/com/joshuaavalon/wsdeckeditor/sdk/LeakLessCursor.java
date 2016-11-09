package com.joshuaavalon.wsdeckeditor.sdk;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class LeakLessCursor extends SQLiteCursor {

    public LeakLessCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        super(driver, editTable, query);
    }

    @Override
    public void close() {
        final SQLiteDatabase db = getDatabase();
        super.close();
        if (db != null)
            db.close();
    }
}