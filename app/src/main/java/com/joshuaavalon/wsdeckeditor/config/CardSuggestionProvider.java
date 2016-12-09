package com.joshuaavalon.wsdeckeditor.config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.wsdeckeditor.sdk.card.ICardRepository;
import com.joshuaavalon.wsdeckeditor.view.search.KeywordSuggestion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardSuggestionProvider implements ISuggestionProvider {
    @NonNull
    private final AppDatabase database;
    @NonNull
    private final ICardRepository cardRepository;

    public CardSuggestionProvider(@NonNull final Context context,
                                  @NonNull final ICardRepository cardRepository) {
        database = new AppDatabase(context);
        this.cardRepository = cardRepository;
    }

    @Override
    public void clearHistory() {
        final SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        sqLiteDatabase.delete(AppScheme.Table.Search, null, null);
    }

    @NonNull
    @Override
    public List<SearchSuggestion> history(@NonNull final String query, final int limit) {
        final Set<String> result = new HashSet<>();
        result.addAll(findHistory(query + "%", limit));
        if (result.size() < limit)
            result.addAll(findHistory("%" + query + "%", 2 * limit));
        return toSuggestion(Lists.newArrayList(result), false);
    }

    @NonNull
    @Override
    public List<SearchSuggestion> history(int limit) {
        final SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
        final Cursor cursor = database.getReadableDatabase().query(true, AppScheme.Table.Search,
                new String[]{AppScheme.Field.Keyword}, null, null, null, null,
                AppScheme.Field.LastAccess + " DESC", String.valueOf(limit));
        final List<String> result = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        cursor.close();
        sqLiteDatabase.close();
        return toSuggestion(result, true);
    }

    private List<String> findHistory(@NonNull final String query, final int limit) {
        final SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
        final Cursor cursor = database.getReadableDatabase().query(true, AppScheme.Table.Search,
                new String[]{AppScheme.Field.Keyword}, String.format("%s LIKE ?", AppScheme.Field.Keyword),
                new String[]{query}, null, null, AppScheme.Field.LastAccess + " DESC", String.valueOf(limit));
        final List<String> result = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        cursor.close();
        sqLiteDatabase.close();
        return result;
    }

    @NonNull
    @Override
    public List<SearchSuggestion> suggestion(@NonNull final String query, final int limit) {
        final Set<String> result = new HashSet<>();
        result.addAll(cardRepository.keywords(query + "%", limit));
        if (result.size() < limit)
            result.addAll(cardRepository.keywords("%" + query + "%", 2 * limit));
        return toSuggestion(Lists.newArrayList(result), false);
    }

    @Override
    public void record(@NonNull SearchSuggestion history) {
        final SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(AppScheme.Field.Keyword, history.getBody());
        final long result = sqLiteDatabase.insertWithOnConflict(AppScheme.Table.Search, null, contentValues,
                SQLiteDatabase.CONFLICT_IGNORE);
        if (result < 0)
            sqLiteDatabase.execSQL(String.format("UPDATE %s SET %s=datetime('now') WHERE %s = '%s'",
                    AppScheme.Table.Search, AppScheme.Field.LastAccess, AppScheme.Field.Keyword, history.getBody()));
        sqLiteDatabase.close();
    }

    private List<SearchSuggestion> toSuggestion(@NonNull final List<String> result, final boolean isHistory) {
        return Lists.newArrayList(Iterables.transform(result,
                new Function<String, SearchSuggestion>() {
                    @Override
                    public SearchSuggestion apply(String input) {
                        final KeywordSuggestion suggestion = new KeywordSuggestion(input);
                        suggestion.setHistory(isHistory);
                        return suggestion;
                    }
                }));
    }
}
