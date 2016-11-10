package com.joshuaavalon.wsdeckeditor.sdk.task;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.joshuaavalon.wsdeckeditor.sdk.data.CardDatabase;

import java.util.ArrayList;
import java.util.List;

public class StringLoadTask extends ResultTask<Context, List<String>> {
    @NonNull
    private final String table;
    @NonNull
    private final String column;

    protected StringLoadTask(@NonNull final String table, @NonNull final String column,
                             @Nullable final CallBack<List<String>> callBack) {
        super(callBack);
        this.table = table;
        this.column = column;
    }

    @Override
    protected List<String> doInBackground(Context... params) {
        final List<String> result = new ArrayList<>();
        if (params == null || params.length < 1 || params[0] == null) return result;
        final SQLiteDatabase database = new CardDatabase(params[0]).getReadableDatabase();
        final Cursor cursor = database.query(true, table,
                new String[]{column}, null, null, null, null, null, null);
        if (cursor.moveToFirst())
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        cursor.close();
        database.close();
        return result;
    }
}
