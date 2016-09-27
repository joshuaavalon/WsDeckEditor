package com.joshuaavalon.wsdeckeditor.sdk.database;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class CardRepository {
    @NonNull
    public static Loader<Cursor> newImageLoader(@NonNull final Context context) {
        return new CursorLoader(context, CardProvider.CARD_CONTENT_URI, new String[]{"Distinct " +
                CardDatabase.Field.Image}, null, null, null);
    }

    @NonNull
    public static Loader<Cursor> newExpansionLoader(@NonNull final Context context) {
        return new CursorLoader(context, CardProvider.CARD_CONTENT_URI, new String[]{"Distinct " +
                CardDatabase.Field.Expansion}, null, null, null);
    }

    @NonNull
    public static Loader<Cursor> newVersionLoader(@NonNull final Context context) {
        return new CursorLoader(context, CardProvider.VERSION_CONTENT_URI, new String[]{CardDatabase.Field.Version},
                null, null, null);
    }
}
