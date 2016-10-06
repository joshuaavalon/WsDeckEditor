package com.joshuaavalon.wsdeckeditor.sdk.task;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.joshuaavalon.wsdeckeditor.sdk.Card;
import com.joshuaavalon.wsdeckeditor.sdk.data.CardDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeckListLoadTask extends CardLoadTask {
    @NonNull
    private final List<String> serials;

    public DeckListLoadTask(@Nullable CallBack<List<Card>> callBack, @NonNull final List<String> serials) {
        super(callBack);
        this.serials = serials;
    }

    @Override
    protected List<Card> doInBackground(Context... params) {
        final List<Card> result = new ArrayList<>();
        if (params == null || params.length < 1 || params[0] == null) return result;
        final String[] selectArgs = Iterables.toArray(serials, String.class);
        final String[] argsPlaceHolder = new String[selectArgs.length];
        Arrays.fill(argsPlaceHolder, "?");
        final String argsPart = Joiner.on(",").join(argsPlaceHolder);
        final SQLiteDatabase database = new CardDatabase(params[0]).getReadableDatabase();
        final Cursor cursor = database.query(CardDatabase.Table.Card, null,
                String.format("%s IN (%s)", CardDatabase.Field.Serial, argsPart),
                selectArgs, null, null, null);
        if (cursor.moveToFirst())
            do {
                result.add(buildCard(cursor));
            } while (cursor.moveToNext());
        cursor.close();
        return result;
    }
}
