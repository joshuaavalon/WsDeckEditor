package com.joshuaavalon.wsdeckeditor;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.joshuaavalon.wsdeckeditor.sdk.data.DeckDatabase;
import com.joshuaavalon.wsdeckeditor.sdk.data.DeckProvider;

public class DeckUtils {
    private static final int SELECT_DECK_LIMIT = 100;

    public static boolean checkDeckCards(@NonNull final Context context, final long id) {
        final Cursor cursor = context.getContentResolver().query(
                ContentUris.withAppendedId(DeckProvider.DECK_RECORD_CONTENT_URI, id),
                new String[]{String.format("SUM(%s)", DeckDatabase.Field.Count)},
                null,
                null,
                null);
        if (cursor == null) return false;
        cursor.moveToFirst();
        final int sum = cursor.getInt(0);
        cursor.close();
        return sum <= SELECT_DECK_LIMIT;
    }
}
